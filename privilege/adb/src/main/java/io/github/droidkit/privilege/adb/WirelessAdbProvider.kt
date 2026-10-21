package io.github.droidkit.privilege.adb

import android.content.Context
import android.os.Build
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.privileges.AdbEndpoint
import io.github.droidkit.core.privileges.AdbPairingState
import io.github.droidkit.core.privileges.AdbQrPayload
import io.github.droidkit.core.privileges.AdbSessionSetup
import io.github.droidkit.core.privileges.CapabilityProvider
import io.github.droidkit.core.privileges.ElevatedExecutor
import io.github.droidkit.core.privileges.ElevatedResult
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.privileges.ProviderStatusBus
import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Wireless-ADB provider. Two ways to a live session:
 *
 *  - Guided: [startPairingSearch] discovers the pairing
 *    service via mDNS, the code comes in through the pairing notification or
 *    the screen, pairing chains straight into connect-discovery + connect.
 *  - Manual: explicit host/port pair + connect (fallback when mDNS is blocked).
 *
 * Once a session is live it executes allow-listed commands via
 * [ElevatedOps] like any other provider.
 */
class WirelessAdbProvider(
    private val context: Context,
) : CapabilityProvider, ElevatedExecutor, AdbSessionSetup {

    data class SessionInfo(
        val host: String,
        val port: Int,
        val tls: Boolean,
        val banner: String,
    )

    private val keys = AdbKeys("droidkit@${Build.MODEL.replace(" ", "_")}")
    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val discovery = AdbDiscovery(context)

    /** Longest we search for a service before surfacing a timeout. */
    private val searchTimeoutMs = 120_000L

    @Volatile
    private var session: AdbConnection? = null

    @Volatile
    var sessionInfo: SessionInfo? = null
        private set

    @Volatile
    private var discoveredEndpoint: AdbEndpoint? = null

    @Volatile
    private var qrPayload: AdbQrPayload? = null

    private var discoveryJob: Job? = null
    private var timeoutJob: Job? = null

    private val _pairingState = MutableStateFlow<AdbPairingState>(AdbPairingState.Idle)
    override val pairingState: StateFlow<AdbPairingState> = _pairingState.asStateFlow()

    override val kind = ProviderKind.ADB
    override val capabilities = setOf(Capability.ADB_SHELL, Capability.ELEVATED_SHELL)

    override val status: ProviderStatus
        get() = if (session?.isConnected == true) {
            ProviderStatus.Ready
        } else {
            ProviderStatus.NeedsSetup(
                "Not connected — pair and connect from the Wireless ADB screen",
            )
        }

    override suspend fun isAvailable(): Boolean = session?.isConnected == true

    /** There is no system grant dialog for ADB — pairing IS the grant. */
    override suspend fun requestGrant(): Boolean = isAvailable()

    init {
        instance = this
        scope.launch {
            discovery.errors.collect { msg ->
                if (_pairingState.value is AdbPairingState.SearchingPairing) {
                    _pairingState.value = AdbPairingState.Failed(msg)
                }
            }
        }
    }

    // ---------- guided flow -------------------------------------------------

    override fun startPairingSearch(): Result<Unit> = runCatching {
        qrPayload = null
        // State before discovery — an early endpoint emit must see Searching.
        _pairingState.value = AdbPairingState.SearchingPairing
        beginDiscovery(AdbDiscovery.SERVICE_TYPE_PAIRING, serviceName = null)
        startService()
        armTimeout("No pairing service found — is “Pair device with pairing code” open?")
    }

    override fun stopPairingSearch() {
        cancelDiscovery()
        context.stopService(AdbPairingService.stopServiceIntent(context))
        _pairingState.value = AdbPairingState.Idle
    }

    override fun startQrPairing(payload: AdbQrPayload): Result<Unit> = runCatching {
        qrPayload = payload
        _pairingState.value = AdbPairingState.SearchingPairing
        beginDiscovery(AdbDiscovery.SERVICE_TYPE_PAIRING, serviceName = payload.serviceName)
        startService()
        armTimeout("No device advertised the QR pairing service “${payload.serviceName}”.")
    }

    override suspend fun pairWithDiscovered(code: String): Result<String> {
        val endpoint = discoveredEndpoint
            ?: return Result.failure(IllegalStateException("No pairing endpoint discovered yet"))
        _pairingState.value = AdbPairingState.Pairing
        val host = loopbackIfReachable(endpoint)
        val result = pair(host, endpoint.port, code)
        result.onSuccess { summary ->
            qrPayload = null
            _pairingState.value = AdbPairingState.Paired(summary)
            scope.launch { connectAfterPairing(endpoint) }
        }.onFailure {
            _pairingState.value = AdbPairingState.Failed(userSafePairError(it))
        }
        return result
    }

    override suspend fun connectDiscovered(): Result<String> {
        _pairingState.value = AdbPairingState.SearchingConnect
        val endpoint = discoverConnectEndpoint() ?: run {
            val msg = "No connect service found — is Wireless debugging on?"
            _pairingState.value = AdbPairingState.Failed(msg)
            return Result.failure(IllegalStateException(msg))
        }
        _pairingState.value = AdbPairingState.Connecting
        val host = loopbackIfReachable(endpoint)
        val result = connect(host, endpoint.port, useTls = true)
        _pairingState.value = result.fold(
            onSuccess = { AdbPairingState.Connected(it) },
            onFailure = { AdbPairingState.Failed(it.message ?: "connect failed") },
        )
        return result
    }

    /** After pairing, find the connect service and open the session. */
    private suspend fun connectAfterPairing(pairedEndpoint: AdbEndpoint) {
        _pairingState.value = AdbPairingState.SearchingConnect
        cancelDiscovery()
        val endpoint = discoverConnectEndpoint(preferredHost = pairedEndpoint.host)
        if (endpoint == null) {
            _pairingState.value = AdbPairingState.Failed(
                "Paired, but no connect service found — is Wireless debugging still on?",
            )
            return
        }
        _pairingState.value = AdbPairingState.Connecting
        val host = loopbackIfReachable(endpoint)
        val result = connect(host, endpoint.port, useTls = true)
        _pairingState.value = result.fold(
            onSuccess = { AdbPairingState.Connected(it) },
            onFailure = { AdbPairingState.Failed(it.message ?: "connect failed") },
        )
    }

    /**
     * For QR pairing there is no typed code — the pairing secret came out of
     * the QR itself. Collected from the endpoint flow in [beginDiscovery].
     */
    private suspend fun pairDiscoveredQr(endpoint: AdbEndpoint, payload: AdbQrPayload) {
        if (_pairingState.value !is AdbPairingState.SearchingPairing) return
        _pairingState.value = AdbPairingState.Pairing
        val result = pair(endpoint.host, endpoint.port, payload.secret)
        result.onSuccess { summary ->
            qrPayload = null
            _pairingState.value = AdbPairingState.Paired(summary)
            connectAfterPairing(endpoint)
        }.onFailure {
            _pairingState.value = AdbPairingState.Failed(userSafePairError(it))
        }
    }

    private fun beginDiscovery(serviceType: String, serviceName: String?) {
        cancelDiscovery()
        discoveryJob = scope.launch {
            discovery.endpoints.collect { endpoint ->
                discoveredEndpoint = endpoint
                val payload = qrPayload
                when {
                    payload != null ->
                        // separate job — pairDiscoveredQr cancels this discovery
                        scope.launch { pairDiscoveredQr(endpoint, payload) }
                    _pairingState.value == AdbPairingState.SearchingPairing ->
                        _pairingState.value = AdbPairingState.EndpointFound(endpoint)
                }
            }
        }
        discovery.start(serviceType, serviceName)
    }

    private suspend fun discoverConnectEndpoint(
        preferredHost: String? = null,
    ): AdbEndpoint? = withContext(Dispatchers.IO) {
        val found = MutableStateFlow<AdbEndpoint?>(null)
        val seen = java.util.concurrent.ConcurrentHashMap.newKeySet<AdbEndpoint>()
        val job = scope.launch {
            discovery.endpoints.collect { endpoint ->
                seen += endpoint
                // Prefer the connect service on the same host we just paired to;
                // accept any when we only want "the device" (code pairing).
                if (preferredHost == null || endpoint.host == preferredHost) {
                    found.value = endpoint
                }
            }
        }
        discovery.start(AdbDiscovery.SERVICE_TYPE_CONNECT, null)
        val deadline = System.currentTimeMillis() + searchTimeoutMs
        while (found.value == null && System.currentTimeMillis() < deadline) {
            delay(200)
        }
        job.cancel()
        cancelDiscovery()
        // Host forms can differ between pairing/connect adverts (v4 vs v6).
        // If exactly one connect service showed up, it's almost surely ours.
        found.value ?: seen.singleOrNull()
    }

    /**
     * Pairing against 127.0.0.1 when the service is on this device — the
     * pairing/connect sockets listen on loopback too.
     */
    private suspend fun loopbackIfReachable(endpoint: AdbEndpoint): String =
        withContext(Dispatchers.IO) {
            runCatching {
                Socket().use { it.connect(InetSocketAddress(LOOPBACK, endpoint.port), 800) }
            }.fold({ LOOPBACK }, { endpoint.host })
        }

    private fun startService() {
        runCatching {
            context.startForegroundService(AdbPairingService.startIntent(context))
        }
    }

    private fun armTimeout(message: String) {
        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(searchTimeoutMs)
            if (_pairingState.value is AdbPairingState.SearchingPairing) {
                stopPairingSearch()
                _pairingState.value = AdbPairingState.Failed(message)
            }
        }
    }

    private fun cancelDiscovery() {
        timeoutJob?.cancel()
        discoveryJob?.cancel()
        discoveryJob = null
        discovery.stop()
        discoveredEndpoint = null
    }

    private fun userSafePairError(t: Throwable): String = when {
        // Wrong code → SPAKE2 key mismatch → GCM decrypt fails.
        t.javaClass.simpleName.contains("AEADBadTag") ||
            t.javaClass.simpleName.contains("BadPadding") ||
            t.message?.contains("tag mismatch", ignoreCase = true) == true ->
            "Pairing rejected — check the code"
        else -> t.message ?: "pairing failed"
    }

    // ---------- manual flow (existing) --------------------------------------

    /** Runs the pairing handshake on the device's pairing port. */
    override suspend fun pair(host: String, port: Int, code: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching { AdbPairing.pair(host, port, code, keys).peerInfoSummary }
        }

    /** Connects to the device's adb service port (TLS for wireless debugging). */
    override suspend fun connect(host: String, port: Int, useTls: Boolean): Result<String> =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                runCatching {
                    val conn = AdbConnection(keys)
                    try {
                        conn.connect(host, port, useTls)
                    } catch (t: Throwable) {
                        conn.close()
                        throw t
                    }
                    session = conn
                    sessionInfo = SessionInfo(host, port, useTls, conn.systemBanner)
                    "Connected to $host:$port — ${conn.systemBanner}"
                }.onFailure {
                    session = null
                    sessionInfo = null
                }.also {
                    ProviderStatusBus.notifyChanged()
                }
            }
        }

    override suspend fun disconnect() {
        mutex.withLock {
            // AdbConnection.close() is @Synchronized — a shell still in flight
            // holds that monitor until its deadline (up to timeoutMs). Callers
            // run this on the main dispatcher, so the monitor wait must happen
            // off-thread or a stuck shell freezes the UI into an ANR.
            withContext(Dispatchers.IO) { session?.close() }
            session = null
            sessionInfo = null
        }
        if (_pairingState.value is AdbPairingState.Connected) {
            _pairingState.value = AdbPairingState.Idle
        }
        ProviderStatusBus.notifyChanged()
    }

    override fun sessionSummary(): String? =
        sessionInfo?.takeIf { session?.isConnected == true }
            ?.let { "${it.host}:${it.port} (${if (it.tls) "TLS" else "plain"}) — ${it.banner}" }

    override suspend fun runShell(command: String, timeoutMs: Long): ElevatedResult =
        mutex.withLock {
            val conn = session
            if (conn?.isConnected != true) {
                return ElevatedResult(
                    provider = kind, exitCode = -1,
                    stdout = "", stderr = "not connected — open Wireless ADB and connect first",
                )
            }
            try {
                val result = withContext(Dispatchers.IO) { conn.shell(command, timeoutMs) }
                ElevatedResult(kind, result.exitCode, result.stdout, "")
            } catch (t: Throwable) {
                // A dead socket ends the session — surface it.
                session = null
                sessionInfo = null
                ProviderStatusBus.notifyChanged()
                ElevatedResult(kind, -1, "", "adb session failed: ${t.message}")
            }
        }

    companion object {
        private const val LOOPBACK = "127.0.0.1"

        /** The DI singleton — set before any consumer can start the service. */
        @Volatile
        internal var instance: WirelessAdbProvider? = null
    }
}

package io.github.droidkit.privilege.adb

import android.content.Context
import android.os.Build
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.datastore.SettingsRepository
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
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val settings: SettingsRepository,
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

    private val _autoConnectEnabled = MutableStateFlow(true)
    override val autoConnectEnabled: StateFlow<Boolean> = _autoConnectEnabled.asStateFlow()

    /** Persisted: adbd accepted our key (pairing + connect proven once). */
    @Volatile
    private var pairedOnce = false

    /** A manual disconnect suppresses auto-connect until the next explicit connect. */
    @Volatile
    private var userDisconnected = false

    /** Throttles lazy reconnect attempts so a dead session fails fast. */
    @Volatile
    private var lastAutoAttemptMs = 0L

    private val autoMutex = Mutex()
    private val qrPairingInFlight = AtomicBoolean(false)

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
        scope.launch {
            settings.wirelessAdbAutoConnect.collect { _autoConnectEnabled.value = it }
        }
        // Shizuku-style start-with-app: if the device already trusts our key,
        // find the connect service and open the session without any UI.
        // Both prefs are read before deciding — the collector above may not
        // have emitted the persisted toggle yet.
        scope.launch {
            pairedOnce = settings.wirelessAdbPaired.first()
            _autoConnectEnabled.value = settings.wirelessAdbAutoConnect.first()
            autoConnect()
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
        // A late Stop tap must not clobber a session that just connected.
        if (_pairingState.value !is AdbPairingState.Connected) {
            _pairingState.value = AdbPairingState.Idle
        }
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
        return pairWithEndpoint(endpoint.host, endpoint.port, code)
    }

    override suspend fun pairWithEndpoint(host: String, port: Int, code: String): Result<String> {
        val endpoint = AdbEndpoint(host, port, serviceName = "manual")
        _pairingState.value = AdbPairingState.Pairing
        val connectHost = loopbackIfReachable(endpoint)
        val result = pair(connectHost, endpoint.port, code)
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

    // ---------- auto-connect (Shizuku-style start-with-app) --------------

    private fun canAutoConnect(): Boolean =
        pairedOnce && _autoConnectEnabled.value && !userDisconnected

    override suspend fun setAutoConnectEnabled(enabled: Boolean) {
        _autoConnectEnabled.value = enabled
        settings.setWirelessAdbAutoConnect(enabled)
        if (enabled) autoConnect()
    }

    /**
     * Bounded, silent connect attempt: discovers `_adb-tls-connect` briefly,
     * connects, and restores Idle on failure — a passive miss must not look
     * like a user action that failed. Skipped while a guided flow runs so it
     * can't clobber its discovery or state.
     */
    override suspend fun autoConnect(): Result<String> {
        session?.takeIf { it.isConnected }?.let {
            return Result.success(sessionSummary() ?: "connected")
        }
        if (!canAutoConnect()) {
            return Result.failure(IllegalStateException("auto-connect unavailable"))
        }
        when (_pairingState.value) {
            is AdbPairingState.SearchingPairing,
            is AdbPairingState.EndpointFound,
            is AdbPairingState.Pairing,
            is AdbPairingState.SearchingConnect,
            is AdbPairingState.Connecting,
            -> return Result.failure(IllegalStateException("a connect flow is already running"))
            else -> Unit
        }
        if (!autoMutex.tryLock()) {
            return Result.failure(IllegalStateException("auto-connect already running"))
        }
        try {
            lastAutoAttemptMs = System.currentTimeMillis()
            _pairingState.value = AdbPairingState.SearchingConnect
            val endpoint = discoverConnectEndpoint(timeoutMs = AUTO_DISCOVERY_TIMEOUT_MS)
            if (endpoint == null) {
                _pairingState.value = AdbPairingState.Idle
                return Result.failure(
                    IllegalStateException("no connect service advertised"),
                )
            }
            _pairingState.value = AdbPairingState.Connecting
            val host = loopbackIfReachable(endpoint)
            // Snapshot before connect(): it clears userDisconnected on success,
            // but a Disconnect tapped mid-attempt must still win.
            val suppressed = userDisconnected
            val result = connect(host, endpoint.port, useTls = true)
            if (suppressed || userDisconnected) {
                disconnect()
                _pairingState.value = AdbPairingState.Idle
                return Result.failure(IllegalStateException("disconnect requested"))
            }
            _pairingState.value = result.fold(
                onSuccess = { AdbPairingState.Connected(it) },
                onFailure = { AdbPairingState.Idle },
            )
            return result
        } finally {
            autoMutex.unlock()
        }
    }

    /**
     * Reconnect on demand when an elevated action finds the session dead —
     * throttled so tools fail fast while wireless debugging is off.
     */
    private suspend fun tryAutoReconnect() {
        if (!canAutoConnect()) return
        if (System.currentTimeMillis() - lastAutoAttemptMs < AUTO_RETRY_COOLDOWN_MS) return
        autoConnect()
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
        // Several mDNS adverts can resolve before the state leaves
        // SearchingPairing — only the first may enter the handshake.
        if (!qrPairingInFlight.compareAndSet(false, true)) return
        try {
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
        } finally {
            qrPairingInFlight.set(false)
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
        timeoutMs: Long = searchTimeoutMs,
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
        val deadline = System.currentTimeMillis() + timeoutMs
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
                    // Proven end-to-end: adbd trusts our key now — persist so
                    // later sessions auto-connect without re-pairing.
                    pairedOnce = true
                    userDisconnected = false
                    runCatching { settings.setWirelessAdbPaired(true) }
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
        userDisconnected = true
        cancelDiscovery()
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

    override suspend fun runShell(command: String, timeoutMs: Long): ElevatedResult {
        // Session dead but the device trusts us — try a bounded reconnect
        // before failing, so elevated tools work without a trip to this screen.
        if (session?.isConnected != true) tryAutoReconnect()
        return mutex.withLock {
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
    }

    companion object {
        private const val LOOPBACK = "127.0.0.1"

        /** Auto-connect discovery window — mDNS answers in ~1s on a healthy LAN. */
        private const val AUTO_DISCOVERY_TIMEOUT_MS = 12_000L

        /** Minimum gap between lazy reconnect attempts inside [runShell]. */
        private const val AUTO_RETRY_COOLDOWN_MS = 30_000L

        /** The DI singleton — set before any consumer can start the service. */
        @Volatile
        internal var instance: WirelessAdbProvider? = null
    }
}

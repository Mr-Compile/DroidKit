package io.github.droidkit.privilege.adb

import android.os.Build
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.privileges.AdbSessionSetup
import io.github.droidkit.core.privileges.CapabilityProvider
import io.github.droidkit.core.privileges.ElevatedExecutor
import io.github.droidkit.core.privileges.ElevatedResult
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.privileges.ProviderStatusBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Wireless-ADB provider. The session is established by the ADB Lab screen:
 * pair once (SPAKE2 over the pairing port), then connect (TLS upgrade on the
 * connect port). Once a session is live it executes allow-listed commands via
 * [ElevatedOps] like any other provider.
 */
class WirelessAdbProvider : CapabilityProvider, ElevatedExecutor, AdbSessionSetup {

    data class SessionInfo(
        val host: String,
        val port: Int,
        val tls: Boolean,
        val banner: String,
    )

    private val keys = AdbKeys("droidkit@${Build.MODEL.replace(" ", "_")}")
    private val mutex = Mutex()

    @Volatile
    private var session: AdbConnection? = null

    @Volatile
    var sessionInfo: SessionInfo? = null
        private set

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
            session?.close()
            session = null
            sessionInfo = null
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
}

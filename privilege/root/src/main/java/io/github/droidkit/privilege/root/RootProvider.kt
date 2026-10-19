package io.github.droidkit.privilege.root

import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.privileges.CapabilityProvider
import io.github.droidkit.core.privileges.ElevatedExecutor
import io.github.droidkit.core.privileges.ElevatedResult
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderSetup
import io.github.droidkit.core.privileges.ProviderStatus
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Optional root provider. Detects a `su` binary in the usual locations and
 * runs allow-listed commands through `su -c`. The first execution surfaces the
 * device owner's root-manager grant prompt; until then the provider reports
 * NeedsSetup rather than Ready.
 */
class RootProvider : CapabilityProvider, ElevatedExecutor, ProviderSetup {

    override val kind = ProviderKind.ROOT
    override val capabilities = setOf(Capability.ROOT, Capability.ELEVATED_SHELL)

    @Volatile
    private var grantConfirmed = false

    private val suPresent: Boolean
        get() = SU_PATHS.any { File(it).canExecute() }

    override val status: ProviderStatus
        get() = when {
            !suPresent -> ProviderStatus.Unavailable("No root — no su binary found on this device")
            grantConfirmed -> ProviderStatus.Ready
            else -> ProviderStatus.NeedsSetup("Root detected — grant DroidKit when your root manager asks")
        }

    /** Availability is only claimed once a `su` round-trip has succeeded. */
    override suspend fun isAvailable(): Boolean =
        grantConfirmed || (suPresent && confirmGrant())

    /** Forces a `su -c id` round-trip, which triggers the root-manager prompt. */
    override suspend fun requestGrant(): Boolean = confirmGrant()

    private suspend fun confirmGrant(): Boolean {
        val result = runShell("id", 10_000)
        val ok = result.ok && result.stdout.contains("uid=0")
        if (ok) grantConfirmed = true
        return ok
    }

    override suspend fun runShell(command: String, timeoutMs: Long): ElevatedResult =
        withContext(Dispatchers.IO) {
            val process = try {
                ProcessBuilder("su", "-c", command).start()
            } catch (t: Throwable) {
                return@withContext ElevatedResult(
                    provider = kind, exitCode = -1,
                    stdout = "", stderr = "failed to start su: ${t.message}",
                )
            }
            try {
                val stdout = ByteArrayOutputStream()
                val stderr = ByteArrayOutputStream()
                val outThread = Thread { process.inputStream.copyTo(stdout) }
                val errThread = Thread { process.errorStream.copyTo(stderr) }
                outThread.isDaemon = true
                errThread.isDaemon = true
                outThread.start()
                errThread.start()
                val finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
                if (!finished) {
                    runCatching { process.destroy() }
                    return@withContext ElevatedResult(
                        provider = kind, exitCode = -1,
                        stdout = "", stderr = "command timed out after ${timeoutMs}ms",
                    )
                }
                outThread.join(2_000)
                errThread.join(2_000)
                ElevatedResult(
                    provider = kind,
                    exitCode = process.exitValue(),
                    stdout = stdout.toString(),
                    stderr = stderr.toString(),
                )
            } finally {
                runCatching { process.destroy() }
            }
        }

    private companion object {
        val SU_PATHS = arrayOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/system/su", "/vendor/bin/su", "/su/bin/su",
        )
    }
}

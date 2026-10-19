package io.github.droidkit.privilege.shizuku

import android.content.pm.PackageManager
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.privileges.CapabilityProvider
import io.github.droidkit.core.privileges.ElevatedExecutor
import io.github.droidkit.core.privileges.ElevatedResult
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderSetup
import io.github.droidkit.core.privileges.ProviderStatus
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

/**
 * Shizuku-backed provider. The Shizuku service must be installed and running,
 * and DroidKit must have been granted access inside the Shizuku app. All state
 * is read live from the binder, so the status reflects the current reality.
 */
class ShizukuProvider : CapabilityProvider, ElevatedExecutor, ProviderSetup {

    override val kind = ProviderKind.SHIZUKU
    override val capabilities = setOf(Capability.SHIZUKU_SERVICE, Capability.ELEVATED_SHELL)

    private val binderAlive: Boolean
        get() = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    private val permissionGranted: Boolean
        get() = runCatching {
            binderAlive && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)

    override val status: ProviderStatus
        get() = when {
            !binderAlive ->
                ProviderStatus.Unavailable("Shizuku is not running — start the Shizuku service on this device")
            !permissionGranted ->
                ProviderStatus.NeedsSetup("DroidKit needs access inside Shizuku — grant it when prompted")
            else -> ProviderStatus.Ready
        }

    override suspend fun isAvailable(): Boolean = permissionGranted

    /** Ask Shizuku for permission; resolves when the user answers the dialog. */
    override suspend fun requestGrant(): Boolean = suspendCancellableCoroutine { cont ->
        if (!binderAlive) {
            cont.resume(false)
            return@suspendCancellableCoroutine
        }
        if (permissionGranted) {
            cont.resume(true)
            return@suspendCancellableCoroutine
        }
        val listener = object : Shizuku.OnRequestPermissionResultListener {
            override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                Shizuku.removeRequestPermissionResultListener(this)
                if (cont.isActive) {
                    cont.resume(grantResult == PackageManager.PERMISSION_GRANTED)
                }
            }
        }
        runCatching {
            Shizuku.addRequestPermissionResultListener(listener)
            Shizuku.requestPermission(REQUEST_CODE)
        }.onFailure {
            Shizuku.removeRequestPermissionResultListener(listener)
            if (cont.isActive) cont.resume(false)
        }
    }

    /**
     * Runs [command] under the shell identity via `Shizuku.newProcess`.
     * `newProcess` is a non-public API entry point used through reflection —
     * the documented way to run commands with Shizuku.
     */
    override suspend fun runShell(command: String, timeoutMs: Long): ElevatedResult =
        withContext(Dispatchers.IO) {
            val process = try {
                newShizukuProcess(arrayOf("sh", "-c", command))
            } catch (t: Throwable) {
                return@withContext ElevatedResult(
                    provider = kind,
                    exitCode = -1,
                    stdout = "",
                    stderr = "failed to start process through Shizuku: ${t.message}",
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

    private fun newShizukuProcess(args: Array<String>): Process {
        val method = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java,
        )
        method.isAccessible = true
        return method.invoke(null, args, null, null) as Process
    }

    private companion object {
        const val REQUEST_CODE = 0x44
    }
}

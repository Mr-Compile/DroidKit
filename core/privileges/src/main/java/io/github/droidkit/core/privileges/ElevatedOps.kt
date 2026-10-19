package io.github.droidkit.core.privileges

import io.github.droidkit.core.common.Capability
import javax.inject.Inject
import javax.inject.Singleton

/** AppOps modes accepted by `appops set`. */
enum class AppOpMode(val wire: String) {
    ALLOW("allow"), IGNORE("ignore"), DENY("deny"), DEFAULT("default")
}

/**
 * A typed, allow-listed elevated action. Features construct these — never raw
 * shell strings — and [ElevatedOps] routes them to whichever provider can
 * execute a shell command. Every parameter is validated before it reaches a
 * command line, so injection is impossible by construction.
 */
sealed interface ElevatedAction {
    /** The exact shell command string executed through `sh -c`. */
    fun command(): String

    companion object {
        private val PACKAGE_RE = Regex("^[A-Za-z0-9_.]+$")
        private val OP_RE = Regex("^[A-Z0-9_]+$")
        private val PROP_KEY_RE = Regex("^[A-Za-z0-9._\\[\\]-]{1,64}$")
        private val LOG_TAG_RE = Regex("^[A-Za-z0-9_.*-]{1,64}$")

        fun requirePackage(pkg: String): String {
            require(PACKAGE_RE.matches(pkg)) { "invalid package name" }
            return pkg
        }

        fun requireOp(op: String): String {
            require(OP_RE.matches(op)) { "invalid appop name" }
            return op
        }
    }

    /** Read AppOps entries for a package (`appops get`). */
    data class GetAppOps(val packageName: String) : ElevatedAction {
        override fun command() = "appops get ${requirePackage(packageName)}"
    }

    /** Change a single AppOp mode (`appops set`). */
    data class SetAppOp(val packageName: String, val op: String, val mode: AppOpMode) : ElevatedAction {
        override fun command() = "appops set ${requirePackage(packageName)} ${requireOp(op)} ${mode.wire}"
    }

    /** Enable or user-disable a package (`pm enable` / `pm disable-user`). */
    data class SetPackageEnabled(val packageName: String, val enabled: Boolean) : ElevatedAction {
        override fun command() =
            if (enabled) "pm enable ${requirePackage(packageName)}"
            else "pm disable-user --user 0 ${requirePackage(packageName)}"
    }

    /** Restore a package removed for the current user (`pm install-existing`). */
    data class InstallExisting(val packageName: String) : ElevatedAction {
        override fun command() = "pm install-existing ${requirePackage(packageName)}"
    }

    /** Wipe an app's data (`pm clear`). Irreversible — the caller must confirm. */
    data class ClearPackageData(val packageName: String) : ElevatedAction {
        override fun command() = "pm clear --user 0 ${requirePackage(packageName)}"
    }

    /** Uninstall for the current user only (`pm uninstall --user 0`). */
    data class UninstallForUser(val packageName: String) : ElevatedAction {
        override fun command() = "pm uninstall -k --user 0 ${requirePackage(packageName)}"
    }

    /** Read a single system property (`getprop`). */
    data class GetProp(val key: String) : ElevatedAction {
        init { require(PROP_KEY_RE.matches(key)) { "invalid property key" } }
        override fun command() = "getprop $key"
    }

    /** Dump the last [lines] of logcat (`logcat -d`). Authorized log access only. */
    data class ReadLog(val lines: Int = 200, val tag: String? = null) : ElevatedAction {
        init {
            require(lines in 1..5000) { "lines out of range" }
            require(tag == null || LOG_TAG_RE.matches(tag)) { "invalid tag" }
        }
        override fun command() =
            "logcat -d -t $lines" + (tag?.let { " -s $it:*" } ?: "")
    }

    /** List packages visible to the shell user (`pm list packages`). */
    data class ListPackages(val includeDisabled: Boolean = true) : ElevatedAction {
        override fun command() =
            "pm list packages" + if (includeDisabled) " -d -e" else ""
    }

    /** Identity of the shell user (`id`) — a read-only elevated proof. */
    data object WhoAmI : ElevatedAction {
        override fun command() = "id"
    }

    /** Reboot the device (`svc power reboot`). High-impact — confirm first. */
    data object Reboot : ElevatedAction {
        override fun command() = "svc power reboot"
    }
}

/** Outcome of an elevated action. */
data class ElevatedResult(
    val provider: ProviderKind,
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
) {
    val ok: Boolean get() = exitCode == 0
}

/**
 * Implemented by privilege providers that can execute shell commands.
 * Providers translate the command string only — the allow-listing and
 * parameter validation live in [ElevatedAction] above.
 */
interface ElevatedExecutor {
    val kind: ProviderKind

    /** Live check — true when the provider can run commands right now. */
    suspend fun isAvailable(): Boolean

    /** Execute [command] via `sh -c` in the provider's identity. */
    suspend fun runShell(command: String, timeoutMs: Long = 30_000): ElevatedResult
}

/** A provider that can be asked (by the UI) to complete its own setup/grant flow. */
interface ProviderSetup {
    val kind: ProviderKind

    /** Launch the provider's grant flow. Returns true if access was granted. */
    suspend fun requestGrant(): Boolean
}

/**
 * Setup surface for the wireless-ADB provider — the ADB Lab screen drives
 * pairing and connection through this interface only (feature code never
 * references the privilege module).
 */
interface AdbSessionSetup : ProviderSetup {
    /** Run the wireless-debugging pairing handshake (`adb pair` equivalent). */
    suspend fun pair(host: String, port: Int, code: String): Result<String>

    /** Open an ADB session on the device's connect port. */
    suspend fun connect(host: String, port: Int, useTls: Boolean): Result<String>

    /** Tear down the current session. */
    suspend fun disconnect()

    /** Human-readable summary of the live session, or null when not connected. */
    fun sessionSummary(): String?
}

/**
 * Routes [ElevatedAction]s to an available [ElevatedExecutor]. Features never
 * see which provider executed; selection prefers SHIZUKU, then ROOT, then ADB.
 */
@Singleton
class ElevatedOps @Inject constructor(
    executors: Set<@JvmSuppressWildcards ElevatedExecutor>,
    setups: Set<@JvmSuppressWildcards ProviderSetup>,
) {
    private val preferenceOrder = listOf(ProviderKind.SHIZUKU, ProviderKind.ROOT, ProviderKind.ADB)
    private val executorsByKind = executors.associateBy { it.kind }
    private val setupsByKind = setups.associateBy { it.kind }

    /** The first provider able to execute right now, in preference order. */
    suspend fun activeProvider(): ProviderKind? =
        preferenceOrder.firstOrNull { executorsByKind[it]?.isAvailable() == true }

    /** Execute [action] through the preferred available provider. */
    suspend fun execute(action: ElevatedAction, timeoutMs: Long = 30_000): ElevatedResult {
        val kind = activeProvider()
            ?: return ElevatedResult(ProviderKind.STANDARD, -1, "", "no elevated provider available")
        return executorsByKind.getValue(kind).runShell(action.command(), timeoutMs)
    }

    /** Ask a provider to run its grant/setup flow. Returns false if unsupported. */
    suspend fun requestGrant(kind: ProviderKind): Boolean =
        setupsByKind[kind]?.requestGrant() ?: false
}

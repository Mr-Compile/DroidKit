package io.github.droidkit.core.privileges

import io.github.droidkit.core.common.Capability
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

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

    /**
     * When non-null, only this provider kind may execute the action — e.g.
     * filesystem reads under `/data` that the shell identity cannot perform.
     * Null leaves provider choice to [ElevatedOps] preference order.
     */
    val requiredProvider: ProviderKind? get() = null

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

        /** Roots the storage browser may touch — shared storage only. */
        private val STORAGE_ROOTS = listOf("/sdcard/", "/storage/emulated/0/")
        private const val STORAGE_MAX_PATH = 512

        /**
         * Validate [path] for storage actions: absolute, inside shared
         * storage, no `..` traversal, no characters that could break the
         * single-quote shell quoting used by commands. Returns [path].
         */
        fun requireStoragePath(path: String): String {
            require(
                path.length in 1..STORAGE_MAX_PATH &&
                    "'" !in path && '\n' !in path && '\r' !in path,
            ) { "invalid storage path" }
            require(
                STORAGE_ROOTS.any { path.startsWith(it) } ||
                    path == "/sdcard" || path == "/storage/emulated/0",
            ) { "path outside shared storage" }
            require(path.split('/').none { it == ".." }) { "parent traversal not allowed" }
            return path
        }

        /** Single-quote a validated path for `sh -c` (paths never contain `'`). */
        fun quote(path: String): String = "'$path'"
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

    /**
     * Suspend or unsuspend a package (`pm suspend`/`pm unsuspend`, API 29+).
     * A suspended app cannot start activities, its notifications are hidden
     * and its launcher icon is greyed — reversible, unlike uninstall.
     */
    data class SuspendPackage(val packageName: String, val suspended: Boolean) : ElevatedAction {
        override fun command() =
            "pm ${if (suspended) "suspend" else "unsuspend"} --user 0 ${requirePackage(packageName)}"
    }

    /** Runtime permission state for a package (`dumpsys package` runtime section). */
    data class GetRuntimePermissions(val packageName: String) : ElevatedAction {
        override fun command() = "dumpsys package ${requirePackage(packageName)}"
    }

    /** Grant or revoke a runtime permission (`pm grant` / `pm revoke`). */
    data class SetRuntimePermission(
        val packageName: String,
        val permission: String,
        val granted: Boolean,
    ) : ElevatedAction {
        init {
            require(PACKAGE_RE.matches(permission)) { "invalid permission name" }
        }
        override fun command() =
            "pm ${if (granted) "grant" else "revoke"} ${requirePackage(packageName)} $permission"
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

    /**
     * Read fuel-gauge capacity nodes under `/sys/class/power_supply/` —
     * charge_full, charge_full_design, cycle_count and OEM state-of-health
     * nodes (soh / *asoc*). `grep -H` prints `path:value` per readable file;
     * absent or permission-denied nodes produce no output. Fixed command,
     * no parameters. Used for a *measured* battery-health reading on devices
     * whose fuel gauge exposes it.
     */
    data object ReadPowerSupply : ElevatedAction {
        override fun command() =
            "grep -H ." +
                " /sys/class/power_supply/*/charge_full" +
                " /sys/class/power_supply/*/charge_full_design" +
                " /sys/class/power_supply/*/cycle_count" +
                " /sys/class/power_supply/*/soh" +
                " /sys/class/power_supply/*/*asoc*" +
                " 2>/dev/null"
    }

    /**
     * Read saved Wi-Fi configuration — `WifiConfigStore.xml` on API 29+
     * (apexdata first, then the legacy `/data/misc/wifi` path) and
     * `wpa_supplicant.conf` on older builds. These files sit under `/data`
     * owned by the `wifi` group, so the shell identity cannot read them —
     * the action is pinned to ROOT. Fixed command, no parameters; `== path`
     * markers separate each file for the parser.
     */
    data object ReadWifiConfigStore : ElevatedAction {
        override val requiredProvider: ProviderKind = ProviderKind.ROOT
        override fun command() =
            "for f in /data/misc/apexdata/com.android.wifi/WifiConfigStore.xml" +
                " /data/misc/wifi/WifiConfigStore.xml" +
                " /data/misc/wifi/wpa_supplicant.conf; do" +
                " [ -r \"\$f\" ] && echo \"== \$f\" && cat \"\$f\"; done"
    }

    /**
     * `dumpsys battery` plus learned-capacity lines from `dumpsys
     * batterystats` — bounded with head. Read-only; whichever elevated
     * provider is available can run it.
     */
    data object DumpBatteryDetail : ElevatedAction {
        override fun command() =
            "dumpsys battery; dumpsys batterystats | grep -iE 'capacity|learned' | head -40"
    }

    /**
     * Learned capacity hints: `dumpsys batteryproperties` charge counter and
     * the "Learned battery capacity" line some builds print in `dumpsys
     * battery`. Read-only; units differ by source so the parser keeps the
     * raw value with its label.
     */
    data object ReadLearnedBatteryCapacity : ElevatedAction {
        override fun command() =
            "dumpsys batteryproperties 2>/dev/null | grep -iE 'capacity|counter';" +
                " dumpsys battery 2>/dev/null | grep -i learned | head -5"
    }

    /**
     * Curated slice of `dumpsys wifi` — link state, current SSID/BSSID, RSSI,
     * link speed, frequency, traffic counters. Bounded with head; the parser
     * treats every field as optional.
     */
    data object DumpWifiLink : ElevatedAction {
        override fun command() =
            "dumpsys wifi | grep -iE 'Wi-Fi generation|SSID|BSSID|RSSI|Link speed|frequency|mNetworkInfo|score|tx |rx ' | head -80"
    }

    /** Full `getprop` table — features display a curated allow-list only. */
    data object ReadSystemProps : ElevatedAction {
        override fun command() = "getprop"
    }

    /**
     * Thermal zone type + temperature under `/sys/class/thermal/`. Readable
     * by the shell identity on many devices, root-only on stricter SELinux
     * builds — either way the output is parsed honestly. `== zone` markers
     * delimit each zone.
     */
    data object ReadThermalZones : ElevatedAction {
        override fun command() =
            "for z in /sys/class/thermal/thermal_zone*; do" +
                " echo \"== \$z\"; cat \"\$z/type\" \"\$z/temp\" 2>/dev/null; done"
    }

    /** Full `/proc/cpuinfo` — includes fields masked from app-context reads. */
    data object ReadCpuInfo : ElevatedAction {
        override fun command() = "cat /proc/cpuinfo"
    }

    /** `/proc/meminfo` — memory totals apps cannot read on modern builds. */
    data object ReadMeminfo : ElevatedAction {
        override fun command() = "cat /proc/meminfo"
    }

    /** `/proc/partitions` — block-device name/size table. */
    data object ReadPartitions : ElevatedAction {
        override fun command() = "cat /proc/partitions"
    }

    /** Live SELinux mode — `getenforce` prints Enforcing/Permissive/Disabled. */
    data object GetSeLinuxStatus : ElevatedAction {
        override fun command() = "getenforce"
    }

    /**
     * Per-interface MAC addresses under `/sys/class/net` — hidden from app
     * identities since Android 6 (they read `02:00:00:00:00:00`). `== path`
     * markers delimit each interface for the parser.
     */
    data object ReadLinkAddresses : ElevatedAction {
        override fun command() =
            "for f in /sys/class/net/*/address; do" +
                " echo \"== \$f\"; cat \"\$f\" 2>/dev/null; done"
    }

    /**
     * Allow-listed `settings get` reads — owner-visible device fields the
     * platform keeps from third-party apps (device name, Bluetooth MAC,
     * developer flags). Fixed key set; `== ns.key` markers separate values.
     */
    data object ReadDeviceSettings : ElevatedAction {
        override fun command() =
            "for spec in 'global device_name' 'secure bluetooth_address'" +
                " 'global adb_enabled' 'global development_settings_enabled'; do" +
                " set -- \$spec; echo \"== \$1.\$2\";" +
                " settings get \"\$1\" \"\$2\" 2>/dev/null; done"
    }

    /**
     * Best-effort device identifiers from `dumpsys phone` — IMEI/MEID/ESN
     * when the build prints them. Often empty on modern builds; the parser
     * treats absence honestly.
     */
    data object DumpPhoneIdentity : ElevatedAction {
        override fun command() =
            "dumpsys phone | grep -iE 'imei|meid|esn|device ?id' | head -20"
    }

    /** Identity of the shell user (`id`) — a read-only elevated proof. */
    data object WhoAmI : ElevatedAction {
        override fun command() = "id"
    }

    /** Reboot the device (`svc power reboot`). High-impact — confirm first. */
    data object Reboot : ElevatedAction {
        override fun command() = "svc power reboot"
    }

    /**
     * List a directory on shared storage — one `type|size|mtime|name` line per
     * entry via `stat`, so parsing does not depend on `ls` column layout.
     * [path] must sit under a shared-storage root (validated at construction).
     * Works for `Android/data` and `Android/obb`, which the shell identity can
     * read even where SAF cannot grant them.
     */
    data class ListStorageDir(val path: String) : ElevatedAction {
        init { requireStoragePath(path) }
        override fun command() =
            "cd ${quote(path)} && for f in ./* ./.[!.]* ./..?*; do" +
                " [ -e \"\$f\" ] && stat -Lc '%F|%s|%Y|%n' \"\$f\" 2>/dev/null; done"
    }

    /**
     * Read the first [maxBytes] of a file (`head -c`) — text preview only.
     * Bounded to 64 KiB; callers surface binary content honestly.
     */
    data class ReadFileHead(val path: String, val maxBytes: Int = 8192) : ElevatedAction {
        init {
            requireStoragePath(path)
            require(maxBytes in 1..65_536) { "maxBytes out of range" }
        }
        override fun command() = "head -c $maxBytes ${quote(path)}"
    }

    /**
     * Name search under [path] (`find -iname`, depth-bounded). Results are
     * stat-formatted like [ListStorageDir] so the same parser applies.
     * [pattern] is glob-shaped and validated — no shell metacharacters.
     */
    data class SearchStorage(val path: String, val pattern: String) : ElevatedAction {
        init {
            requireStoragePath(path)
            require(SEARCH_RE.matches(pattern)) { "invalid search pattern" }
        }
        override fun command() =
            "find ${quote(path)} -maxdepth 5 -iname ${quote("*$pattern*")} -print 2>/dev/null" +
                " | head -200 | while read f; do stat -Lc '%F|%s|%Y|%n' \"\$f\" 2>/dev/null; done"

        companion object {
            private val SEARCH_RE = Regex("^[A-Za-z0-9._*? ~()-]{1,48}$")
        }
    }

    /**
     * Copy a file or directory from shared storage into the user-visible
     * copy-out destination `/sdcard/Download/DroidKit/` (`cp -r`; existing
     * files are overwritten). [path] is validated by [requireStoragePath];
     * the destination is fixed — callers never supply it.
     */
    data class CopyToDownload(val path: String) : ElevatedAction {
        init { requireStoragePath(path) }
        override fun command() =
            "mkdir -p ${quote(COPY_OUT_DIR)} &&" +
                " cp -r ${quote(path)} ${quote("$COPY_OUT_DIR/")} &&" +
                " echo \"== $COPY_OUT_DIR/\""

        companion object {
            const val COPY_OUT_DIR = "/sdcard/Download/DroidKit"
        }
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

    /** Live state of the guided (mDNS-assisted) pairing flow. */
    val pairingState: StateFlow<AdbPairingState>

    /**
     * Start discovering the pairing service (`_adb-tls-pairing._tcp`) and post
     * the pairing notification so the code can be entered without leaving the
     * developer-options dialog. Stops when paired, failed, or cancelled.
     */
    fun startPairingSearch(): Result<Unit>

    /** Stop discovery and dismiss the pairing notification/service. */
    fun stopPairingSearch()

    /**
     * Pair against the endpoint most recently discovered by
     * [startPairingSearch] — no host/port needed.
     */
    suspend fun pairWithDiscovered(code: String): Result<String>

    /**
     * Pair against a caller-supplied endpoint, keeping the same state flow
     * and auto-connect as [pairWithDiscovered]. Used when mDNS discovery
     * can't see the pairing advert (e.g. the notification reply path on
     * devices whose NSD can't resolve a locally-advertised service) and the
     * user typed the host/port straight off the pairing dialog.
     */
    suspend fun pairWithEndpoint(host: String, port: Int, code: String): Result<String>

    /**
     * Discover `_adb-tls-connect._tcp` and open the session on it. Works
     * without re-pairing when the device already trusts our ADB key.
     */
    suspend fun connectDiscovered(): Result<String>

    /**
     * QR-assisted pairing: keep discovering until a pairing service named
     * `payload.serviceName` appears, then auto-pair with `payload.secret`
     * and auto-connect. Covers both directions — a QR we display for another
     * device to scan, or a device-displayed QR we decoded from a screenshot.
     */
    fun startQrPairing(payload: AdbQrPayload): Result<Unit>
}

/** One recorded elevated execution for the audit log. */
data class ElevatedAuditEntry(
    val epochMs: Long,
    val provider: ProviderKind,
    val command: String,
    val exitCode: Int,
    val stderrSummary: String?,
)

/** Optional sink for elevated-action audit records. Bound from core:database. */
interface ElevatedAuditSink {
    suspend fun record(entry: ElevatedAuditEntry)
}

/**
 * Routes [ElevatedAction]s to an available [ElevatedExecutor]. Features never
 * see which provider executed; selection prefers ROOT, then ADB.
 */
@Singleton
class ElevatedOps @Inject constructor(
    executors: Set<@JvmSuppressWildcards ElevatedExecutor>,
    setups: Set<@JvmSuppressWildcards ProviderSetup>,
    private val auditSinks: Set<@JvmSuppressWildcards ElevatedAuditSink>,
) {
    private val preferenceOrder = listOf(ProviderKind.ROOT, ProviderKind.ADB)
    private val executorsByKind = executors.associateBy { it.kind }
    private val setupsByKind = setups.associateBy { it.kind }

    /** The first provider able to execute right now, in preference order. */
    suspend fun activeProvider(): ProviderKind? =
        preferenceOrder.firstOrNull { executorsByKind[it]?.isAvailable() == true }

    /** Live availability of one specific provider kind — false when absent or not ready. */
    suspend fun isAvailable(kind: ProviderKind): Boolean =
        executorsByKind[kind]?.isAvailable() == true

    /**
     * Execute [action] through the preferred available provider — or, when
     * the action pins [ElevatedAction.requiredProvider], only through that
     * kind. A pinned action never falls back to a weaker provider.
     */
    suspend fun execute(action: ElevatedAction, timeoutMs: Long = 30_000): ElevatedResult {
        val pinned = action.requiredProvider
        val kind = if (pinned == null) {
            activeProvider()
                ?: return ElevatedResult(ProviderKind.STANDARD, -1, "", "no elevated provider available")
        } else {
            if (executorsByKind[pinned]?.isAvailable() == true) pinned
            else return ElevatedResult(pinned, -1, "", "requires ${pinned.name} provider — not available")
        }
        val command = action.command()
        return executorsByKind.getValue(kind).runShell(command, timeoutMs).also { result ->
            auditSinks.forEach { sink ->
                runCatching {
                    sink.record(
                        ElevatedAuditEntry(
                            epochMs = System.currentTimeMillis(),
                            provider = kind,
                            command = command,
                            exitCode = result.exitCode,
                            stderrSummary = result.stderr.lineSequence()
                                .firstOrNull { it.isNotBlank() }
                                ?.take(200),
                        ),
                    )
                }
            }
        }
    }

    /** Ask a provider to run its grant/setup flow. Returns false if unsupported. */
    suspend fun requestGrant(kind: ProviderKind): Boolean =
        (setupsByKind[kind]?.requestGrant() ?: false).also {
            ProviderStatusBus.notifyChanged()
        }
}

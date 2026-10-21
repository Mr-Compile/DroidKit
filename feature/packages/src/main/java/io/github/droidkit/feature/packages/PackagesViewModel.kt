package io.github.droidkit.feature.packages

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.droidkit.core.privileges.ElevatedAction
import io.github.droidkit.core.privileges.ElevatedOps
import io.github.droidkit.core.privileges.ProviderKind
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class PackagesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val elevatedOps: ElevatedOps,
) : ViewModel() {

    data class PackageEntry(
        val packageName: String,
        val label: String,
        val enabled: Boolean,
    )

    sealed interface ElevatedState {
        data object Loading : ElevatedState
        data object Unavailable : ElevatedState
        data class Ready(val kind: ProviderKind) : ElevatedState
    }

    sealed interface Event {
        data class Message(val text: String) : Event
    }

    enum class Action { DISABLE, ENABLE, SUSPEND, UNSUSPEND, CLEAR_DATA, UNINSTALL, RESTORE }

    /** Batch-capable actions — reversible only; destructive ops stay per-package. */
    enum class BatchAction(val label: String) {
        DISABLE("Disable"), ENABLE("Enable"), SUSPEND("Suspend"), UNSUSPEND("Unsuspend"),
    }

    private val _elevated = MutableStateFlow<ElevatedState>(ElevatedState.Loading)
    val elevated: StateFlow<ElevatedState> = _elevated.asStateFlow()

    private val _packages = MutableStateFlow<List<PackageEntry>>(emptyList())
    val packages: StateFlow<List<PackageEntry>> = _packages.asStateFlow()

    val query = MutableStateFlow("")

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    /** Runtime permissions of the package currently open in the perms dialog. */
    private val _perms = MutableStateFlow<List<Pair<String, Boolean>>?>(null)
    val perms: StateFlow<List<Pair<String, Boolean>>?> = _perms.asStateFlow()

    /** Package names currently selected for a batch action. */
    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected.asStateFlow()

    fun toggleSelect(pkg: String) {
        _selected.value = _selected.value.let { if (pkg in it) it - pkg else it + pkg }
    }

    fun selectAll(pkgs: List<String>) {
        _selected.value = pkgs.toSet()
    }

    fun clearSelection() {
        _selected.value = emptySet()
    }

    init {
        viewModelScope.launch { refreshElevated() }
    }

    suspend fun refreshElevated() {
        _elevated.value = ElevatedState.Loading
        val kind = elevatedOps.activeProvider()
        _elevated.value = kind?.let { ElevatedState.Ready(it) } ?: ElevatedState.Unavailable
        if (kind != null && _packages.value.isEmpty()) loadPackages()
    }

    suspend fun requestAccess() {
        elevatedOps.requestGrant(ProviderKind.ROOT)
        refreshElevated()
    }

    private suspend fun loadPackages() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        _packages.value = runCatching {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
                .mapNotNull { info ->
                    info.applicationInfo?.let {
                        PackageEntry(
                            info.packageName,
                            it.loadLabel(pm).toString(),
                            it.enabled,
                        )
                    }
                }
                .sortedBy { it.label.lowercase() }
        }.getOrDefault(emptyList())
    }

    fun runAction(action: Action, pkg: String) {
        viewModelScope.launch {
            _busy.value = true
            val elevated = when (action) {
                Action.DISABLE -> ElevatedAction.SetPackageEnabled(pkg, enabled = false)
                Action.ENABLE -> ElevatedAction.SetPackageEnabled(pkg, enabled = true)
                Action.SUSPEND -> ElevatedAction.SuspendPackage(pkg, suspended = true)
                Action.UNSUSPEND -> ElevatedAction.SuspendPackage(pkg, suspended = false)
                Action.CLEAR_DATA -> ElevatedAction.ClearPackageData(pkg)
                Action.UNINSTALL -> ElevatedAction.UninstallForUser(pkg)
                Action.RESTORE -> ElevatedAction.InstallExisting(pkg)
            }
            val result = elevatedOps.execute(elevated)
            if (result.ok) {
                emit("${labelFor(action)} succeeded for $pkg")
            } else {
                val detail = result.stderr.ifEmpty { result.stdout }.lineSequence()
                    .firstOrNull { it.isNotBlank() } ?: "exit ${result.exitCode}"
                emit("${labelFor(action)} failed: $detail")
            }
            loadPackages()
            _busy.value = false
        }
    }

    /** Loads runtime permission state for [pkg] via elevated dumpsys. */
    fun loadPermissions(pkg: String) {
        viewModelScope.launch {
            _perms.value = null
            val result = elevatedOps.execute(ElevatedAction.GetRuntimePermissions(pkg))
            _perms.value = if (result.ok) {
                RuntimePermissionParser.parse(result.stdout)
            } else {
                emit("Could not read permissions for $pkg")
                emptyList()
            }
        }
    }

    /** Grant/revoke one runtime permission, then refresh the list. */
    fun setPermission(pkg: String, permission: String, grant: Boolean) {
        viewModelScope.launch {
            _busy.value = true
            val result = elevatedOps.execute(
                ElevatedAction.SetRuntimePermission(pkg, permission, grant),
            )
            emit(
                if (result.ok) "${if (grant) "Granted" else "Revoked"} $permission for $pkg"
                else "Failed: " + (result.stderr.ifEmpty { result.stdout }
                    .lineSequence().firstOrNull { it.isNotBlank() } ?: "exit ${result.exitCode}"),
            )
            _busy.value = false
            loadPermissions(pkg)
        }
    }

    /**
     * Run [action] on each package in [pkgs] sequentially, then emit one
     * summary naming any failures. Clears the selection afterwards.
     */
    fun runBatch(action: BatchAction, pkgs: List<String>) {
        if (pkgs.isEmpty()) return
        viewModelScope.launch {
            _busy.value = true
            val results = pkgs.map { pkg ->
                val elevated = when (action) {
                    BatchAction.DISABLE -> ElevatedAction.SetPackageEnabled(pkg, enabled = false)
                    BatchAction.ENABLE -> ElevatedAction.SetPackageEnabled(pkg, enabled = true)
                    BatchAction.SUSPEND -> ElevatedAction.SuspendPackage(pkg, suspended = true)
                    BatchAction.UNSUSPEND -> ElevatedAction.SuspendPackage(pkg, suspended = false)
                }
                pkg to elevatedOps.execute(elevated).ok
            }
            emit(PackagesInfo.batchSummary(action.label, results))
            _selected.value = emptySet()
            loadPackages()
            _busy.value = false
        }
    }

    fun clearPermissions() {
        _perms.value = null
    }

    fun consumeEvent(event: Event) {
        _events.value = _events.value - event
    }

    private fun emit(text: String) {
        _events.value = _events.value + Event.Message(text)
    }

    private fun labelFor(action: Action) = when (action) {
        Action.DISABLE -> "Disable"
        Action.ENABLE -> "Enable"
        Action.SUSPEND -> "Suspend"
        Action.UNSUSPEND -> "Unsuspend"
        Action.CLEAR_DATA -> "Clear data"
        Action.UNINSTALL -> "Uninstall for user"
        Action.RESTORE -> "Restore for user"
    }
}

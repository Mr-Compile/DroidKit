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

    data class PackageEntry(val packageName: String, val label: String)

    sealed interface ElevatedState {
        data object Loading : ElevatedState
        data object Unavailable : ElevatedState
        data class Ready(val kind: ProviderKind) : ElevatedState
    }

    sealed interface Event {
        data class Message(val text: String) : Event
    }

    enum class Action { DISABLE, ENABLE, CLEAR_DATA, UNINSTALL, RESTORE }

    private val _elevated = MutableStateFlow<ElevatedState>(ElevatedState.Loading)
    val elevated: StateFlow<ElevatedState> = _elevated.asStateFlow()

    private val _packages = MutableStateFlow<List<PackageEntry>>(emptyList())
    val packages: StateFlow<List<PackageEntry>> = _packages.asStateFlow()

    val query = MutableStateFlow("")

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

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
        for (kind in listOf(ProviderKind.SHIZUKU, ProviderKind.ROOT)) {
            if (elevatedOps.requestGrant(kind)) break
        }
        refreshElevated()
    }

    private suspend fun loadPackages() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        _packages.value = runCatching {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
                .mapNotNull { info ->
                    info.applicationInfo?.let {
                        PackageEntry(info.packageName, it.loadLabel(pm).toString())
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

    fun consumeEvent(event: Event) {
        _events.value = _events.value - event
    }

    private fun emit(text: String) {
        _events.value = _events.value + Event.Message(text)
    }

    private fun labelFor(action: Action) = when (action) {
        Action.DISABLE -> "Disable"
        Action.ENABLE -> "Enable"
        Action.CLEAR_DATA -> "Clear data"
        Action.UNINSTALL -> "Uninstall for user"
        Action.RESTORE -> "Restore for user"
    }
}

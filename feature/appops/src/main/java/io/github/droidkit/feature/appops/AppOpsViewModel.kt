package io.github.droidkit.feature.appops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.common.DLog
import io.github.droidkit.core.privileges.AppOpMode
import io.github.droidkit.core.privileges.ElevatedAction
import io.github.droidkit.core.privileges.ElevatedOps
import io.github.droidkit.core.privileges.ProviderKind
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AppOpsViewModel @Inject constructor(
    private val elevatedOps: ElevatedOps,
) : ViewModel() {

    val query = MutableStateFlow("")

    private val _provider = MutableStateFlow<ProviderKind?>(null)
    val provider: StateFlow<ProviderKind?> = _provider.asStateFlow()

    /** Identity proof — output of `id` run under the elevated context. */
    private val _identity = MutableStateFlow<String?>(null)
    val identity: StateFlow<String?> = _identity.asStateFlow()

    private val _packages = MutableStateFlow<List<String>?>(null)
    val packages: StateFlow<List<String>?> = _packages.asStateFlow()

    private val _selected = MutableStateFlow<String?>(null)
    val selected: StateFlow<String?> = _selected.asStateFlow()

    private val _ops = MutableStateFlow<List<AppOpEntry>?>(null)
    val ops: StateFlow<List<AppOpEntry>?> = _ops.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** One-line outcome of the last mutation, shown under the ops list. */
    private val _lastAction = MutableStateFlow<String?>(null)
    val lastAction: StateFlow<String?> = _lastAction.asStateFlow()

    init {
        viewModelScope.launch {
            val kind = elevatedOps.activeProvider()
            _provider.value = kind
            if (kind != null) {
                elevatedOps.execute(ElevatedAction.WhoAmI)
                    .takeIf { it.ok }
                    ?.let { _identity.value = it.stdout.trim() }
                loadPackages()
            }
        }
    }

    fun loadPackages() {
        viewModelScope.launch {
            _busy.value = true
            val result = elevatedOps.execute(ElevatedAction.ListPackages())
            _busy.value = false
            if (result.ok) {
                _packages.value = PackageListParser.parse(result.stdout)
                _error.value = null
            } else {
                _error.value = result.stderr.ifBlank { "pm list packages exited ${result.exitCode}" }
            }
        }
    }

    fun select(packageName: String) {
        _selected.value = packageName
        _lastAction.value = null
        viewModelScope.launch {
            _busy.value = true
            val result = elevatedOps.execute(ElevatedAction.GetAppOps(packageName))
            _busy.value = false
            if (result.ok) {
                _ops.value = AppOpsParser.parse(result.stdout)
            } else {
                _ops.value = null
                _error.value = result.stderr.ifBlank { "appops get exited ${result.exitCode}" }
            }
        }
    }

    fun back() {
        _selected.value = null
        _ops.value = null
        _lastAction.value = null
    }

    /** Mutates one op — the caller already confirmed the consequence dialog. */
    fun setMode(packageName: String, op: String, mode: AppOpMode) {
        viewModelScope.launch {
            _busy.value = true
            val result = elevatedOps.execute(ElevatedAction.SetAppOp(packageName, op, mode))
            _busy.value = false
            if (result.ok) {
                _lastAction.value = "$op → ${mode.wire}"
                select(packageName)
            } else {
                _lastAction.value = "Failed: " +
                    result.stderr.ifBlank { "appops set exited ${result.exitCode}" }
            }
            if (!result.ok) {
                DLog.w("AppOpsVM", "set $op=$mode for $packageName failed: ${result.stderr}")
            }
        }
    }
}

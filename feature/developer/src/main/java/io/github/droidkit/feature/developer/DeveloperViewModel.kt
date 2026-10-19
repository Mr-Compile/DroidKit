package io.github.droidkit.feature.developer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.privileges.ElevatedAction
import io.github.droidkit.core.privileges.ElevatedOps
import io.github.droidkit.core.privileges.ElevatedResult
import io.github.droidkit.core.privileges.ProviderKind
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DeveloperViewModel @Inject constructor(
    private val elevatedOps: ElevatedOps,
) : ViewModel() {

    sealed interface LogState {
        data object Idle : LogState
        data object Loading : LogState
        data object NoProvider : LogState
        data class Loaded(val provider: ProviderKind, val lines: List<String>) : LogState
        data class Failed(val provider: ProviderKind, val message: String) : LogState
    }

    private val _activeProvider = MutableStateFlow<ProviderKind?>(null)
    val activeProvider: StateFlow<ProviderKind?> = _activeProvider.asStateFlow()

    private val _logState = MutableStateFlow<LogState>(LogState.Idle)
    val logState: StateFlow<LogState> = _logState.asStateFlow()

    init {
        viewModelScope.launch { _activeProvider.value = elevatedOps.activeProvider() }
    }

    fun readLog(lines: Int, tag: String?) {
        _logState.value = LogState.Loading
        viewModelScope.launch {
            val provider = elevatedOps.activeProvider()
            _activeProvider.value = provider
            if (provider == null) {
                _logState.value = LogState.NoProvider
                return@launch
            }
            val result: ElevatedResult =
                elevatedOps.execute(ElevatedAction.ReadLog(lines, tag?.takeIf { it.isNotBlank() }))
            _logState.value = if (result.ok) {
                LogState.Loaded(provider, result.stdout.lines().filter { it.isNotBlank() })
            } else {
                LogState.Failed(provider, result.stderr.ifBlank { "exit ${result.exitCode}" })
            }
        }
    }
}

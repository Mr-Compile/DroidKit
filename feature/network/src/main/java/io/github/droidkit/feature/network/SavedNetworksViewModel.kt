package io.github.droidkit.feature.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.common.RecoveryAction
import io.github.droidkit.core.common.UiProblem
import io.github.droidkit.core.privileges.ElevatedAction
import io.github.droidkit.core.privileges.ElevatedOps
import io.github.droidkit.core.privileges.ProviderKind
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SavedNetworksViewModel @Inject constructor(
    private val elevatedOps: ElevatedOps,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Ready(val result: SavedWifiResult) : UiState
        data object Empty : UiState
        data class Failed(val problem: UiProblem) : UiState
        data object Dismissed : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = UiState.Loading
        viewModelScope.launch { _state.value = load() }
    }

    /**
     * Triggers the root manager's grant prompt, then re-reads. Only the
     * command string is audit-logged — stdout (which carries credentials)
     * never reaches the log or the audit sink.
     */
    fun requestRootGrant() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            elevatedOps.requestGrant(ProviderKind.ROOT)
            _state.value = load()
        }
    }

    fun dismiss(problem: UiProblem) { _state.value = UiState.Dismissed }

    fun onRecovery(action: RecoveryAction) {
        when (action) {
            RecoveryAction.RequestElevatedAccess -> requestRootGrant()
            RecoveryAction.Retry -> refresh()
            RecoveryAction.Dismiss -> _state.value = UiState.Dismissed
            else -> Unit
        }
    }

    private suspend fun load(): UiState {
        val result = elevatedOps.execute(ElevatedAction.ReadWifiConfigStore)
        return when {
            result.stderr.contains("requires ROOT") ->
                UiState.Failed(
                    UiProblem(
                        title = "Root access required",
                        message = "Saved Wi-Fi passwords live in a system store only " +
                            "root can read — Shizuku and wireless ADB cannot reach it. " +
                            "On a rooted device, grant DroidKit when your root manager asks.",
                        severity = UiProblem.Severity.INFO,
                        recoveries = listOf(
                            RecoveryAction.RequestElevatedAccess,
                            RecoveryAction.Retry,
                            RecoveryAction.Dismiss,
                        ),
                    ),
                )
            !result.ok ->
                UiState.Failed(
                    UiProblem(
                        title = "Could not read Wi-Fi store",
                        message = "The elevated read failed on this device.",
                        technicalDetails = result.stderr.take(500).ifBlank { null },
                        recoveries = listOf(RecoveryAction.Retry, RecoveryAction.Dismiss),
                    ),
                )
            else -> {
                val parsed = SavedWifiParser.parse(result.stdout)
                if (parsed.networks.isEmpty()) UiState.Empty else UiState.Ready(parsed)
            }
        }
    }
}

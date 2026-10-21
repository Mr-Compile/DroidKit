package io.github.droidkit.feature.settings.about

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolRegistry
import io.github.droidkit.core.system.ProcessExitProbe
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Explicit state for the Check for Updates row. */
sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data class Result(val result: UpdateCheckResult) : UpdateUiState
}

@HiltViewModel
class AboutViewModel @Inject constructor(
    @ApplicationContext context: Context,
    toolRegistry: ToolRegistry,
    private val updateChecker: UpdateChecker,
) : ViewModel() {

    private val appContext = context.applicationContext

    val appInfo: AppInfo = AppInfo.read(context)

    /** Real, registered tools only — the Features page can't advertise stubs. */
    val toolsByCategory: List<Pair<ToolCategory, List<Tool>>> = toolRegistry.tools
        .groupBy { it.category }
        .toList()
        .sortedBy { it.first.name }

    private val _updateState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val updateState: StateFlow<UpdateUiState> = _updateState.asStateFlow()

    /** How the previous process run ended — crash/freeze capture for support. */
    private val _lastExit = MutableStateFlow<ProcessExitProbe.ExitReport?>(null)
    val lastExit: StateFlow<ProcessExitProbe.ExitReport?> = _lastExit.asStateFlow()

    init {
        viewModelScope.launch {
            _lastExit.value = withContext(Dispatchers.IO) {
                ProcessExitProbe.report(appContext)
            }
        }
    }

    /** Guarded against re-entry while a check is running. */
    fun checkForUpdates() {
        if (_updateState.value == UpdateUiState.Checking) return
        viewModelScope.launch {
            _updateState.value = UpdateUiState.Checking
            _updateState.value = UpdateUiState.Result(
                updateChecker.check(appInfo.versionName),
            )
        }
    }
}

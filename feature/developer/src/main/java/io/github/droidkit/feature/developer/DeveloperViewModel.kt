package io.github.droidkit.feature.developer

import android.app.ActivityManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
    private val elevatedOps: ElevatedOps,
) : ViewModel() {

    sealed interface LogState {
        data object Idle : LogState
        data object Loading : LogState
        data object NoProvider : LogState
        data class Loaded(val provider: ProviderKind, val lines: List<String>) : LogState
        data class Failed(val provider: ProviderKind, val message: String) : LogState
    }

    sealed interface PropsState {
        data object Idle : PropsState
        data object Loading : PropsState
        data object NoProvider : PropsState
        data class Loaded(val provider: ProviderKind, val props: List<Pair<String, String>>) : PropsState
        data class Failed(val provider: ProviderKind, val message: String) : PropsState
    }

    /** One-shot memory snapshot from standard APIs — no elevated provider needed. */
    data class MemorySnapshot(
        val availMem: Long,
        val totalMem: Long,
        val lowMemThreshold: Long,
        val lowMemory: Boolean,
        val heapMax: Long,
        val heapUsed: Long,
    )

    private val _activeProvider = MutableStateFlow<ProviderKind?>(null)
    val activeProvider: StateFlow<ProviderKind?> = _activeProvider.asStateFlow()

    private val _logState = MutableStateFlow<LogState>(LogState.Idle)
    val logState: StateFlow<LogState> = _logState.asStateFlow()

    private val _propsState = MutableStateFlow<PropsState>(PropsState.Idle)
    val propsState: StateFlow<PropsState> = _propsState.asStateFlow()

    val propQuery = MutableStateFlow("")

    private val _memory = MutableStateFlow<MemorySnapshot?>(null)
    val memory: StateFlow<MemorySnapshot?> = _memory.asStateFlow()

    init {
        viewModelScope.launch { _activeProvider.value = elevatedOps.activeProvider() }
        refreshMemory()
    }

    /** Standard-API memory read — ActivityManager + Runtime heap. */
    fun refreshMemory() {
        val am = context.getSystemService(ActivityManager::class.java)
        val info = ActivityManager.MemoryInfo()
        runCatching { am?.getMemoryInfo(info) }
        val rt = Runtime.getRuntime()
        _memory.value = MemorySnapshot(
            availMem = info.availMem,
            totalMem = info.totalMem,
            lowMemThreshold = info.threshold,
            lowMemory = info.lowMemory,
            heapMax = rt.maxMemory(),
            heapUsed = rt.totalMemory() - rt.freeMemory(),
        )
    }

    /** Read the full getprop table through the elevated provider. */
    fun readProps() {
        _propsState.value = PropsState.Loading
        viewModelScope.launch {
            val provider = elevatedOps.activeProvider()
            _activeProvider.value = provider
            if (provider == null) {
                _propsState.value = PropsState.NoProvider
                return@launch
            }
            val result = elevatedOps.execute(ElevatedAction.ReadSystemProps)
            _propsState.value = if (result.ok) {
                PropsState.Loaded(provider, DevTools.parseProps(result.stdout))
            } else {
                PropsState.Failed(
                    provider,
                    result.stderr.ifBlank { "exit ${result.exitCode}" },
                )
            }
        }
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

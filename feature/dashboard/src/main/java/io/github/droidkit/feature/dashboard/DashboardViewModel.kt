package io.github.droidkit.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolRegistry
import io.github.droidkit.core.database.DiagnosticResultDao
import io.github.droidkit.core.database.DiagnosticSessionDao
import io.github.droidkit.core.database.InsightsCalculator
import io.github.droidkit.core.database.SessionHealth
import io.github.droidkit.core.datastore.SettingsRepository
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.system.DeviceSystemProbe
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    val probe: DeviceSystemProbe,
    private val capabilityManager: CapabilityManager,
    private val registry: ToolRegistry,
    val settings: SettingsRepository,
    sessionDao: DiagnosticSessionDao,
    resultDao: DiagnosticResultDao,
) : ViewModel() {

    val recentToolIds: Flow<List<String>> = settings.recentToolIds

    /** User-pinned tools — starred in the Tools tab surface here. */
    val favoriteToolIds: Flow<Set<String>> = settings.favoriteToolIds

    /** Latest completed session verdict — feeds the Insights card. */
    val latestInsight: Flow<SessionHealth?> = combine(
        sessionDao.observeAll(),
        resultDao.observeAll(),
    ) { sessions, results ->
        sessions.filter { it.overallStatus == "COMPLETED" }
            .maxByOrNull { it.startedAtEpochMs }
            ?.let { s ->
                InsightsCalculator.health(
                    s.id, s.startedAtEpochMs, s.preset,
                    results.filter { it.sessionId == s.id },
                )
            }
    }

    fun providerStatuses(): Map<ProviderKind, ProviderStatus> =
        capabilityManager.providerStatuses()

    fun quickTools(): List<Tool> =
        registry.tools.filter { capabilityManager.resolve(it) == ToolResolution.Ready }.take(6)

    fun resolve(tool: Tool): ToolResolution = capabilityManager.resolve(tool)

    fun toolById(id: String): Tool? = registry.byId(id)

    /** Pinned tools in registry order — stable across sessions. */
    fun pinned(ids: Set<String>): List<Tool> = registry.tools.filter { it.id in ids }

    /** Count the open for recents + Insights "most used". */
    fun recordOpen(tool: Tool) {
        viewModelScope.launch { settings.recordRecent(tool.id) }
    }
}

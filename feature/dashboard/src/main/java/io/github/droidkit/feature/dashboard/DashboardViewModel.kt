package io.github.droidkit.feature.dashboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolRegistry
import io.github.droidkit.core.datastore.SettingsRepository
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.system.DeviceSystemProbe
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class DashboardViewModel @Inject constructor(
    val probe: DeviceSystemProbe,
    private val capabilityManager: CapabilityManager,
    private val registry: ToolRegistry,
    val settings: SettingsRepository,
) : ViewModel() {

    val recentToolIds: Flow<List<String>> = settings.recentToolIds

    fun providerStatuses(): Map<ProviderKind, ProviderStatus> =
        capabilityManager.providerStatuses()

    fun quickTools(): List<Tool> =
        registry.tools.filter { capabilityManager.resolve(it) == ToolResolution.Ready }.take(6)

    fun resolve(tool: Tool): ToolResolution = capabilityManager.resolve(tool)

    fun toolById(id: String): Tool? = registry.byId(id)
}

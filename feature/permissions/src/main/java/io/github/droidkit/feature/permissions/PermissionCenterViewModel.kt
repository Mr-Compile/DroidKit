package io.github.droidkit.feature.permissions

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolRegistry
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class PermissionCenterViewModel @Inject constructor(
    toolRegistry: ToolRegistry,
    capabilityManager: CapabilityManager,
) : ViewModel() {

    /** permission name → tools that declare it in their requirements. */
    val permissionToTools: Map<String, List<Tool>> = toolRegistry.tools
        .flatMap { tool -> tool.requiredPermissions.map { it to tool } }
        .groupBy({ it.first }, { it.second })

    /** Live elevated-provider statuses for the "Advanced access" section. */
    val providerStatuses: StateFlow<Map<ProviderKind, ProviderStatus>> = capabilityManager.statuses
}

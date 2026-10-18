package io.github.droidkit.feature.permissions

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object PermissionsFeature : FeatureContribution {
    const val ROUTE = "permissions"

    override val tools = listOf(
        Tool(
            id = "permissions.inspector",
            title = "Permission Inspector",
            description = "Per-app granted/declared permissions and runtime state",
            category = ToolCategory.APPS,
            route = ROUTE,
            requiredCapabilities = setOf(Capability.SHIZUKU_SERVICE),
            phase = ToolPhase.V2,
        ),
    )
}

fun NavGraphBuilder.permissionsGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(PermissionsFeature.ROUTE) {
        FeatureStubScreen(
            title = "Permissions",
            subtitle = "Runtime grant state for other apps needs elevated access — lands with the Shizuku provider in Phase 7.",
            tools = PermissionsFeature.tools,
            resolve = resolve,
        )
    }
}

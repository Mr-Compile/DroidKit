package io.github.droidkit.feature.permissions

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolIconKeys
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution

object PermissionsFeature : FeatureContribution {
    const val ROUTE = "permissions"

    override val tools = listOf(
        Tool(
            id = "permissions.center",
            title = "Permission Center",
            description = "DroidKit's own declared and runtime permissions",
            category = ToolCategory.APPS,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            phase = ToolPhase.V1,
            keywords = setOf("runtime", "granted", "denied", "declared"),
            iconKey = ToolIconKeys.SECURITY,
        ),
        Tool(
            id = "permissions.inspector",
            title = "Permission Inspector",
            description = "Per-app granted/declared permissions and runtime state",
            category = ToolCategory.APPS,
            route = ROUTE,
            requiredCapabilities = setOf(Capability.SHIZUKU_SERVICE),
            phase = ToolPhase.V2,
            keywords = setOf("perms", "apps", "granted", "declared"),
            iconKey = ToolIconKeys.APP_PERMS,
        ),
    )
}

fun NavGraphBuilder.permissionsGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(PermissionsFeature.ROUTE) {
        PermissionCenterScreen(
            onOpenSettings = { nav.navigate("settings") { launchSingleTop = true } },
            onBack = { nav.navigateUp() },
        )
    }
}

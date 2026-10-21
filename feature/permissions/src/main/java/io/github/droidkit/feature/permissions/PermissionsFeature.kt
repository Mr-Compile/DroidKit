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
import io.github.droidkit.core.ui.navigation.navigateTopLevel

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
            requiredCapabilities = setOf(Capability.ELEVATED_SHELL),
            phase = ToolPhase.V2,
            keywords = setOf("perms", "apps", "granted", "declared"),
            iconKey = ToolIconKeys.APP_PERMS,
        ),
        Tool(
            id = "permissions.byapp",
            title = "Apps by Permission",
            description = "Which installed apps declare each dangerous permission",
            category = ToolCategory.APPS,
            route = "$ROUTE/byapp",
            safetyLevel = SafetyLevel.SAFE,
            phase = ToolPhase.V2,
            keywords = setOf("reverse", "who has", "dangerous", "audit"),
            iconKey = ToolIconKeys.APP_PERMS,
        ),
        Tool(
            id = "permissions.special",
            title = "Special Access Matrix",
            description = "Overlay, all-files, install-unknown and usage-access grants",
            category = ToolCategory.APPS,
            route = "$ROUTE/special",
            safetyLevel = SafetyLevel.SAFE,
            phase = ToolPhase.V2,
            keywords = setOf("overlay", "appops", "usage access", "special"),
            iconKey = ToolIconKeys.SECURITY,
        ),
    )
}

fun NavGraphBuilder.permissionsGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(PermissionsFeature.ROUTE) {
        PermissionCenterScreen(
            onOpenSettings = { nav.navigateTopLevel("settings") },
            onBack = { nav.navigateUp() },
        )
    }
    composable("${PermissionsFeature.ROUTE}/byapp") {
        AppsByPermissionScreen(onBack = { nav.navigateUp() })
    }
    composable("${PermissionsFeature.ROUTE}/special") {
        SpecialAccessScreen(onBack = { nav.navigateUp() })
    }
}

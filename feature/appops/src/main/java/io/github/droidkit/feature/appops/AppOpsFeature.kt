package io.github.droidkit.feature.appops

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution

object AppOpsFeature : FeatureContribution {
    const val ROUTE = "appops"

    override val tools = listOf(
        Tool(
            id = "appops.view",
            title = "AppOps Viewer",
            description = "Per-app AppOps modes (usage access, overlays, background)",
            category = ToolCategory.APPS,
            route = ROUTE,
            requiredCapabilities = setOf(Capability.ELEVATED_SHELL),
            phase = ToolPhase.V2,
        ),
        Tool(
            id = "appops.set",
            title = "AppOps Controls",
            description = "Change AppOps modes — modifies system permission state",
            category = ToolCategory.APPS,
            route = ROUTE,
            safetyLevel = SafetyLevel.SYSTEM_CHANGE,
            requiredCapabilities = setOf(Capability.ELEVATED_SHELL),
            phase = ToolPhase.V2,
        ),
    )
}

fun NavGraphBuilder.appOpsGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(AppOpsFeature.ROUTE) {
        AppOpsScreen(onOpenAdb = { nav.navigate("adb") })
    }
}

package io.github.droidkit.feature.dashboard

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolIconKeys
import io.github.droidkit.core.ui.navigation.navigateTopLevel
import io.github.droidkit.core.ui.navigation.openRoute

object DashboardFeature : FeatureContribution {
    const val ROUTE = "home"

    override val tools = listOf(
        Tool(
            id = "dashboard.overview",
            title = "Device Overview",
            description = "Summary of this device and its capabilities",
            category = ToolCategory.DEVICE,
            route = ROUTE,
            keywords = setOf("summary", "overview", "home", "dashboard"),
            iconKey = ToolIconKeys.DEVICE,
        ),
    )
}

fun NavGraphBuilder.dashboardGraph(nav: NavHostController) {
    composable(DashboardFeature.ROUTE) {
        DashboardScreen(
            // Top-level jumps mirror bottom-nav semantics (UI v6 plan 16).
            onNavigateToTools = { nav.navigateTopLevel("tools") },
            onNavigateToDiagnose = { nav.navigateTopLevel("diagnose") },
            onOpenTool = { route -> nav.openRoute(route) },
            onOpenInsights = { nav.openRoute("insights") },
        )
    }
}

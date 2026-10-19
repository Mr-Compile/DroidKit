package io.github.droidkit.feature.dashboard

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory

object DashboardFeature : FeatureContribution {
    const val ROUTE = "home"

    override val tools = listOf(
        Tool(
            id = "dashboard.overview",
            title = "Device Overview",
            description = "Summary of this device and its capabilities",
            category = ToolCategory.DEVICE,
            route = ROUTE,
        ),
    )
}

fun NavGraphBuilder.dashboardGraph(nav: NavHostController) {
    composable(DashboardFeature.ROUTE) {
        DashboardScreen(
            onNavigateToTools = { nav.navigate("tools") },
            onNavigateToDiagnose = { nav.navigate("diagnose") },
            onOpenTool = { route -> nav.navigate(route) },
            onOpenInsights = { nav.navigate("insights") },
        )
    }
}

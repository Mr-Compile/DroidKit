package io.github.droidkit.feature.insights

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolIconKeys

object InsightsFeature : FeatureContribution {
    const val ROUTE = "insights"

    override val tools = listOf(
        Tool(
            id = "analytics.insights",
            title = "Device Insights",
            description = "Health trends, diagnostic history and on-device usage stats",
            category = ToolCategory.REPORTS,
            route = ROUTE,
            keywords = setOf("analytics", "trends", "stats", "history", "most used"),
            iconKey = ToolIconKeys.INSIGHTS,
        ),
    )
}

fun NavGraphBuilder.insightsGraph(nav: NavHostController) {
    composable(InsightsFeature.ROUTE) {
        InsightsScreen(
            onOpenTool = { route -> nav.navigate(route) { launchSingleTop = true } },
            onStartDiagnostic = {
                nav.navigate("diagnose") {
                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onBack = { nav.navigateUp() },
        )
    }
}

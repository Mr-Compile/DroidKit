package io.github.droidkit.feature.insights

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory

object InsightsFeature : FeatureContribution {
    const val ROUTE = "insights"

    override val tools = listOf(
        Tool(
            id = "analytics.insights",
            title = "Device Insights",
            description = "Health trends, diagnostic history and on-device usage stats",
            category = ToolCategory.REPORTS,
            route = ROUTE,
        ),
    )
}

fun NavGraphBuilder.insightsGraph(nav: NavHostController) {
    composable(InsightsFeature.ROUTE) {
        InsightsScreen(
            onOpenTool = { route -> nav.navigate(route) },
            onStartDiagnostic = { nav.navigate("diagnose") },
        )
    }
}

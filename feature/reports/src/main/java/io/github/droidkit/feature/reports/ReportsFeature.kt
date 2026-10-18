package io.github.droidkit.feature.reports

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolPhase

object ReportsFeature : FeatureContribution {
    const val ROUTE = "reports"

    override val tools = listOf(
        Tool(
            id = "reports.export",
            title = "Export Report",
            description = "Share a diagnostic report as text",
            category = ToolCategory.REPORTS,
            route = ROUTE,
            safetyLevel = SafetyLevel.MINOR,
            phase = ToolPhase.V1,
        ),
    )
}

fun NavGraphBuilder.reportsGraph(nav: NavHostController) {
    composable(ReportsFeature.ROUTE) { ReportsScreen() }
}

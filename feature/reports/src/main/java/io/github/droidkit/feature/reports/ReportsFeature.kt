package io.github.droidkit.feature.reports

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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

    const val COMPARE_ROUTE = "reports/compare"

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
        Tool(
            id = "reports.compare",
            title = "Compare Sessions",
            description = "Before/after diff of two diagnostic sessions — prove a repair",
            category = ToolCategory.REPORTS,
            route = COMPARE_ROUTE,
            safetyLevel = SafetyLevel.SAFE,
        ),
    )
}

fun NavGraphBuilder.reportsGraph(nav: NavHostController) {
    composable(ReportsFeature.ROUTE) {
        ReportsScreen(
            onOpenDetail = { id -> nav.navigate("reports/detail/$id") },
            onCompare = { nav.navigate(ReportsFeature.COMPARE_ROUTE) },
        )
    }
    composable(ReportsFeature.COMPARE_ROUTE) {
        SessionCompareScreen()
    }
    composable("reports/detail/{sessionId}") { backStackEntry ->
        val sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty()
        val parentEntry = nav.getBackStackEntry(ReportsFeature.ROUTE)
        ReportDetailScreen(
            sessionId = sessionId,
            onDeleted = { nav.popBackStack() },
            viewModel = hiltViewModel(parentEntry),
        )
    }
}

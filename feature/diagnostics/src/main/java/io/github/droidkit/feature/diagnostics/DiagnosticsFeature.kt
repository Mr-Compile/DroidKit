package io.github.droidkit.feature.diagnostics

import androidx.compose.material3.Text
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory

object DiagnosticsFeature : FeatureContribution {
    const val ROUTE = "diagnose"
    const val GUIDED_ROUTE = "diagnose/guided"
    const val REVIEW_ROUTE = "diagnose/review"
    const val TEST_ROUTE = "diagnose/test/{testId}"

    fun testRoute(testId: String) = "diagnose/test/$testId"

    override val tools = listOf(
        Tool(
            id = "diagnostics.full",
            title = "Full Diagnostic",
            description = "Guided end-to-end hardware check with a saved report",
            category = ToolCategory.DIAGNOSTICS,
            route = GUIDED_ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredCapabilities = setOf(Capability.STANDARD_API),
        ),
        Tool(
            id = "diagnostics.quick",
            title = "Quick Tests",
            description = "Jump straight to a single hardware test",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
        ),
    )
}

fun NavGraphBuilder.diagnosticsGraph(nav: NavHostController) {
    composable(DiagnosticsFeature.ROUTE) {
        DiagnosticsScreen(
            onStartGuided = { nav.navigate(DiagnosticsFeature.GUIDED_ROUTE) },
            onOpenTool = { route -> nav.navigate(route) },
            onOpenReports = { nav.navigate("reports") },
        )
    }
    composable(DiagnosticsFeature.GUIDED_ROUTE) {
        val parentEntry = nav.getBackStackEntry(DiagnosticsFeature.ROUTE)
        GuidedSessionScreen(
            onFinished = {
                nav.navigate(DiagnosticsFeature.REVIEW_ROUTE) {
                    popUpTo(DiagnosticsFeature.GUIDED_ROUTE) { inclusive = true }
                }
            },
            viewModel = hiltViewModel(parentEntry),
        )
    }
    composable(DiagnosticsFeature.REVIEW_ROUTE) {
        val parentEntry = nav.getBackStackEntry(DiagnosticsFeature.ROUTE)
        SessionReviewScreen(
            onRetry = { nav.navigate(DiagnosticsFeature.GUIDED_ROUTE) },
            onSaved = {
                nav.navigate(DiagnosticsFeature.ROUTE) {
                    popUpTo(DiagnosticsFeature.ROUTE) { inclusive = true }
                }
            },
            viewModel = hiltViewModel(parentEntry),
        )
    }
    composable(DiagnosticsFeature.TEST_ROUTE) { backStackEntry ->
        val testId = backStackEntry.arguments?.getString("testId").orEmpty()
        val test = PhoneDoctorTests.findById(testId)
        if (test == null) {
            Text("Unknown test: $testId")
        } else {
            PhoneDoctorTestScreen(test = test) { _, _ ->
                nav.popBackStack()
            }
        }
    }
}

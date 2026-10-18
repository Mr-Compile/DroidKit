package io.github.droidkit.feature.diagnostics

import androidx.compose.material3.Text
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

    override val tools = listOf(
        Tool(
            id = "diagnostics.full",
            title = "Full Diagnostic",
            description = "Guided end-to-end hardware check with a saved report",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
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
            onOpenTool = { route -> nav.navigate(route) },
            onOpenReports = { nav.navigate("reports") },
        )
    }
    composable("${DiagnosticsFeature.ROUTE}/test/{testId}") { backStackEntry ->
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

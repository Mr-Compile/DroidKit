package io.github.droidkit.feature.developer

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolPhase

object DeveloperFeature : FeatureContribution {
    const val ROUTE = "developer"

    override val tools = listOf(
        Tool(
            id = "developer.intents",
            title = "Intent Launcher",
            description = "Jump to Developer options, system settings screens and custom intents",
            category = ToolCategory.DEVELOPER,
            route = ROUTE,
            safetyLevel = SafetyLevel.MINOR,
        ),
        Tool(
            id = "developer.display",
            title = "Display Metrics",
            description = "Resolution, density, refresh rate and HDR capabilities",
            category = ToolCategory.DEVELOPER,
            route = ROUTE,
        ),
        Tool(
            id = "developer.logs",
            title = "Log Viewer",
            description = "Read device logs where authorized",
            category = ToolCategory.DEVELOPER,
            route = ROUTE,
            safetyLevel = SafetyLevel.ADVANCED,
            requiredCapabilities = setOf(Capability.ADB_SHELL),
            phase = ToolPhase.V2,
        ),
    )
}

fun NavGraphBuilder.developerGraph(nav: NavHostController) {
    composable(DeveloperFeature.ROUTE) {
        DeveloperLabScreen()
    }
}

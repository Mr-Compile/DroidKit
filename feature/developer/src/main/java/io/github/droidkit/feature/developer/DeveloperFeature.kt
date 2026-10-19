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
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object DeveloperFeature : FeatureContribution {
    const val ROUTE = "developer"

    override val tools = listOf(
        Tool(
            id = "developer.shortcuts",
            title = "Developer Shortcuts",
            description = "Jump to Developer options, USB debugging and system settings",
            category = ToolCategory.DEVELOPER,
            route = ROUTE,
            safetyLevel = SafetyLevel.MINOR,
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

fun NavGraphBuilder.developerGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(DeveloperFeature.ROUTE) {
        FeatureStubScreen(
            title = "Developer Lab",
            subtitle = "Shortcuts, dumpsys helpers and authorized log access — Phase 10.",
            tools = DeveloperFeature.tools,
            resolve = resolve,
        )
    }
}

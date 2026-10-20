package io.github.droidkit.feature.developer

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolIconKeys
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
            keywords = setOf("intent", "launch", "developer options", "activity"),
            iconKey = ToolIconKeys.INTENT,
        ),
        Tool(
            id = "developer.display",
            title = "Display Metrics",
            description = "Resolution, density, refresh rate and HDR capabilities",
            category = ToolCategory.DEVELOPER,
            route = ROUTE,
            keywords = setOf("resolution", "density", "dpi", "refresh", "hdr"),
            iconKey = ToolIconKeys.METRICS,
        ),
        Tool(
            id = "developer.deeplink",
            title = "Deep-Link Tester",
            description = "Resolve and launch arbitrary URI intents",
            category = ToolCategory.DEVELOPER,
            route = ROUTE,
            safetyLevel = SafetyLevel.MINOR,
            keywords = setOf("uri", "url", "link", "resolve"),
            iconKey = ToolIconKeys.LINK,
        ),
        Tool(
            id = "developer.logs",
            title = "Log Viewer",
            description = "Read device logs where authorized",
            category = ToolCategory.DEVELOPER,
            route = ROUTE,
            safetyLevel = SafetyLevel.ADVANCED,
            requiredCapabilities = setOf(Capability.ELEVATED_SHELL),
            phase = ToolPhase.V2,
            keywords = setOf("logcat", "logs", "read"),
            iconKey = ToolIconKeys.ARTICLE,
        ),
    )
}

fun NavGraphBuilder.developerGraph(nav: NavHostController) {
    composable(DeveloperFeature.ROUTE) {
        DeveloperLabScreen(onBack = { nav.navigateUp() })
    }
}

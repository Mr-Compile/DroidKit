package io.github.droidkit.feature.display

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object DisplayFeature : FeatureContribution {
    const val ROUTE = "display"

    override val tools = listOf(
        Tool(
            id = "display.deadpixel",
            title = "Dead Pixel Test",
            description = "Full-screen color slides to spot dead pixels",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
        ),
        Tool(
            id = "display.touch",
            title = "Touch Test",
            description = "Count simultaneous touch points",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
        ),
    )
}

fun NavGraphBuilder.displayGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(DisplayFeature.ROUTE) {
        FeatureStubScreen(
            title = "Display & Touch",
            subtitle = "Interactive display and touch tests ship in Phase 3 (Phone Doctor).",
            tools = DisplayFeature.tools,
            resolve = resolve,
        )
    }
}

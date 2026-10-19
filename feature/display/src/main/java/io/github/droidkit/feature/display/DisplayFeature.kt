package io.github.droidkit.feature.display

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.ui.LabEntry
import io.github.droidkit.core.ui.LabIndexScreen

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
        Tool(
            id = "display.modes",
            title = "Display Modes",
            description = "Current and supported display modes, refresh rates, HDR",
            category = ToolCategory.DIAGNOSTICS,
            route = "$ROUTE/modes",
        ),
        Tool(
            id = "display.motion",
            title = "Motion Pattern",
            description = "Moving test pattern to observe judder and refresh behavior",
            category = ToolCategory.DIAGNOSTICS,
            route = "$ROUTE/motion",
        ),
    )
}

fun NavGraphBuilder.displayGraph(nav: NavHostController) {
    composable(DisplayFeature.ROUTE) {
        LabIndexScreen(
            title = "Display & Touch",
            subtitle = "Standalone display and touch checks from Phone Doctor. " +
                "Results here are not saved — run Full Diagnostic to persist them.",
            entries = listOf(
                LabEntry(
                    testId = "display.color",
                    title = "Display colors",
                    blurb = "Cycle solid colors and check coverage, banding and tint.",
                ),
                LabEntry(
                    testId = "display.dead_pixel",
                    title = "Dead-pixel check",
                    blurb = "Inspect the whole panel for stuck or dead pixels.",
                ),
                LabEntry(
                    testId = "touch.grid",
                    title = "Touch grid",
                    blurb = "Fill every grid cell to expose dead touch zones.",
                ),
                LabEntry(
                    testId = "touch.multitouch",
                    title = "Multitouch",
                    blurb = "Count simultaneous touch points with several fingers.",
                ),
            ),
            onOpenTest = { testId -> nav.navigate("diagnose/test/$testId") },
        )
    }
    composable("${DisplayFeature.ROUTE}/modes") {
        DisplayModesScreen()
    }
    composable("${DisplayFeature.ROUTE}/motion") {
        MotionPatternScreen()
    }
}

package io.github.droidkit.feature.sensors

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object SensorsFeature : FeatureContribution {
    const val ROUTE = "sensors"

    override val tools = listOf(
        Tool(
            id = "sensors.live",
            title = "Live Sensor Test",
            description = "Accelerometer, gyroscope, compass, proximity readouts",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
        ),
        Tool(
            id = "sensors.inventory",
            title = "Sensor Inventory",
            description = "Full sensor list with vendor and power info",
            category = ToolCategory.DEVICE,
            route = ROUTE,
        ),
    )
}

fun NavGraphBuilder.sensorsGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(SensorsFeature.ROUTE) {
        FeatureStubScreen(
            title = "Sensors",
            subtitle = "Guided sensor tests ship in Phase 3 (Phone Doctor).",
            tools = SensorsFeature.tools,
            resolve = resolve,
        )
    }
}

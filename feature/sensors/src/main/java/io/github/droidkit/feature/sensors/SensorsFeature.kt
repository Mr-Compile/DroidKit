package io.github.droidkit.feature.sensors

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolIconKeys

object SensorsFeature : FeatureContribution {
    const val ROUTE = "sensors"

    override val tools = listOf(
        Tool(
            id = "sensors.lab",
            title = "Sensor Lab",
            description = "Full sensor inventory with vendor, power and range details",
            category = ToolCategory.DEVICE,
            route = ROUTE,
            keywords = setOf("inventory", "accelerometer", "gyroscope", "vendor", "range"),
            iconKey = ToolIconKeys.SENSORS,
        ),
        Tool(
            id = "sensors.live",
            title = "Live Sensor Monitor",
            description = "Stream live readings from supported sensors",
            category = ToolCategory.DIAGNOSTICS,
            route = "$ROUTE/live",
            keywords = setOf("live", "stream", "monitor", "readings"),
            iconKey = ToolIconKeys.LIVE,
        ),
        Tool(
            id = "sensors.check",
            title = "Sensor Check",
            description = "Batch-verify every live-capable sensor produces events",
            category = ToolCategory.DIAGNOSTICS,
            route = "$ROUTE/check",
            keywords = setOf("check", "verify", "batch", "test", "responding"),
            iconKey = ToolIconKeys.FACT_CHECK,
        ),
        Tool(
            id = "sensors.orientation",
            title = "Orientation Helper",
            description = "Azimuth/pitch/roll from the rotation-vector sensor",
            category = ToolCategory.DEVICE,
            route = "$ROUTE/orientation",
            keywords = setOf("compass", "heading", "tilt", "azimuth", "pitch", "roll"),
            iconKey = ToolIconKeys.COMPASS,
        ),
    )
}

fun NavGraphBuilder.sensorsGraph(nav: NavHostController) {
    composable(SensorsFeature.ROUTE) { SensorsScreen(nav) }
    composable("${SensorsFeature.ROUTE}/orientation") {
        OrientationScreen(onBack = { nav.navigateUp() })
    }
    composable("${SensorsFeature.ROUTE}/live") { LiveSensorPickerScreen(nav) }
    composable("${SensorsFeature.ROUTE}/check") {
        SensorCheckScreen(onBack = { nav.navigateUp() })
    }
    composable(
        route = "${SensorsFeature.ROUTE}/live/{type}",
        arguments = listOf(navArgument("type") { type = NavType.IntType }),
    ) { backStackEntry ->
        LiveSensorScreen(
            sensorType = backStackEntry.arguments?.getInt("type") ?: -1,
            onBack = { nav.navigateUp() },
        )
    }
}

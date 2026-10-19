package io.github.droidkit.feature.sensors

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory

object SensorsFeature : FeatureContribution {
    const val ROUTE = "sensors"

    override val tools = listOf(
        Tool(
            id = "sensors.lab",
            title = "Sensor Lab",
            description = "Full sensor inventory with vendor, power and range details",
            category = ToolCategory.DEVICE,
            route = ROUTE,
            keywords = setOf("inventory", "accelerometer", "gyroscope"),
        ),
        Tool(
            id = "sensors.live",
            title = "Live Sensor Monitor",
            description = "Stream live readings from supported sensors",
            category = ToolCategory.DIAGNOSTICS,
            route = "$ROUTE/live/{type}",
            keywords = setOf("live", "stream", "monitor"),
        ),
        Tool(
            id = "sensors.orientation",
            title = "Orientation Helper",
            description = "Azimuth/pitch/roll from the rotation-vector sensor",
            category = ToolCategory.DEVICE,
            route = "$ROUTE/orientation",
            keywords = setOf("compass", "heading", "tilt"),
        ),
    )
}

fun NavGraphBuilder.sensorsGraph(nav: NavHostController) {
    composable(SensorsFeature.ROUTE) { SensorsScreen(nav) }
    composable("${SensorsFeature.ROUTE}/orientation") { OrientationScreen() }
    composable(
        route = "${SensorsFeature.ROUTE}/live/{type}",
        arguments = listOf(navArgument("type") { type = NavType.IntType }),
    ) { backStackEntry ->
        LiveSensorScreen(sensorType = backStackEntry.arguments?.getInt("type") ?: -1)
    }
}

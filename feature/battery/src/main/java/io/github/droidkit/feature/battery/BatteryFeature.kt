package io.github.droidkit.feature.battery

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory

object BatteryFeature : FeatureContribution {
    const val ROUTE = "battery"

    override val tools = listOf(
        Tool(
            id = "battery.lab",
            title = "Battery Lab",
            description = "Charge level, status, temperature, voltage, power draw",
            category = ToolCategory.DEVICE,
            route = ROUTE,
            keywords = setOf("charge", "health", "temperature", "voltage"),
        ),
    )
}

fun NavGraphBuilder.batteryGraph(nav: NavHostController) {
    composable(BatteryFeature.ROUTE) { BatteryScreen() }
}

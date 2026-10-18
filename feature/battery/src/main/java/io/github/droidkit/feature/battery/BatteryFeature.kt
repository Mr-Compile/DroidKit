package io.github.droidkit.feature.battery

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object BatteryFeature : FeatureContribution {
    const val ROUTE = "battery"

    override val tools = listOf(
        Tool(
            id = "battery.overview",
            title = "Battery Health",
            description = "Charge cycles, capacity and temperature where exposed",
            category = ToolCategory.DEVICE,
            route = ROUTE,
        ),
        Tool(
            id = "battery.drain",
            title = "Drain Monitor",
            description = "Observe discharge rate during a guided test",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
        ),
    )
}

fun NavGraphBuilder.batteryGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(BatteryFeature.ROUTE) {
        FeatureStubScreen(
            title = "Battery Lab",
            subtitle = "Health estimates and drain monitoring ship in Phase 4 (Device Intelligence).",
            tools = BatteryFeature.tools,
            resolve = resolve,
        )
    }
}

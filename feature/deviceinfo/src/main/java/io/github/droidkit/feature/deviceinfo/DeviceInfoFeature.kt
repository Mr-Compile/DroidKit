package io.github.droidkit.feature.deviceinfo

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory

object DeviceInfoFeature : FeatureContribution {
    const val ROUTE = "deviceinfo"

    override val tools = listOf(
        Tool(
            id = "deviceinfo.overview",
            title = "Device Inspector",
            description = "Build, CPU/ABI, memory, storage, display, battery, sensors, cameras",
            category = ToolCategory.DEVICE,
            route = ROUTE,
        ),
    )
}

fun NavGraphBuilder.deviceInfoGraph(nav: NavHostController) {
    composable(DeviceInfoFeature.ROUTE) { DeviceInfoScreen() }
}

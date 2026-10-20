package io.github.droidkit.feature.deviceinfo

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolIconKeys

object DeviceInfoFeature : FeatureContribution {
    const val ROUTE = "deviceinfo"

    override val tools = listOf(
        Tool(
            id = "deviceinfo.overview",
            title = "Device Inspector",
            description = "Identity, OS, hardware, memory, storage, display, battery, capabilities",
            category = ToolCategory.DEVICE,
            route = ROUTE,
            keywords = setOf("phone info", "build", "hardware", "specs", "model"),
            iconKey = ToolIconKeys.INSPECTOR,
        ),
        Tool(
            id = "deviceinfo.storage",
            title = "Storage Lab",
            description = "Volumes, capacity, free space, storage warnings",
            category = ToolCategory.DEVICE,
            route = "$ROUTE/storage",
            keywords = setOf("disk", "space", "memory", "volumes", "free"),
            iconKey = ToolIconKeys.STORAGE,
        ),
    )
}

fun NavGraphBuilder.deviceInfoGraph(nav: NavHostController) {
    composable(DeviceInfoFeature.ROUTE) { DeviceInfoScreen(nav) }
    composable("${DeviceInfoFeature.ROUTE}/storage") {
        StorageLabScreen(onBack = { nav.navigateUp() })
    }
}

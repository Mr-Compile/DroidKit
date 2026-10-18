package io.github.droidkit.feature.usb

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object UsbFeature : FeatureContribution {
    const val ROUTE = "usb"

    override val tools = listOf(
        Tool(
            id = "usb.host",
            title = "USB Host Check",
            description = "Detect USB host capability and connected devices",
            category = ToolCategory.HARDWARE,
            route = ROUTE,
            requiredFeatures = setOf("android.hardware.usb.host"),
        ),
        Tool(
            id = "usb.descriptors",
            title = "USB Descriptors",
            description = "VID/PID, interfaces and endpoints for attached devices",
            category = ToolCategory.HARDWARE,
            route = ROUTE,
            requiredFeatures = setOf("android.hardware.usb.host"),
        ),
    )
}

fun NavGraphBuilder.usbGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(UsbFeature.ROUTE) {
        FeatureStubScreen(
            title = "USB Lab",
            subtitle = "USB host detection, permission flow and descriptor reads ship in Phase 6.",
            tools = UsbFeature.tools,
            resolve = resolve,
        )
    }
}

package io.github.droidkit.feature.usb

import android.net.Uri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolIconKeys
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution

object UsbFeature : FeatureContribution {
    const val ROUTE = "usb"
    const val DETAIL_ROUTE = "usb/device/{deviceName}"

    fun detailRoute(deviceName: String) = "usb/device/${Uri.encode(deviceName)}"

    override val tools = listOf(
        Tool(
            id = "usb.host",
            title = "USB Host Check",
            description = "Detect USB host capability and connected devices",
            category = ToolCategory.HARDWARE,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredFeatures = setOf("android.hardware.usb.host"),
            phase = ToolPhase.V1,
            keywords = setOf("usb", "otg", "host", "device", "connected"),
            iconKey = ToolIconKeys.USB,
        ),
        Tool(
            id = "usb.descriptors",
            title = "USB Descriptors",
            description = "VID/PID, interfaces and endpoints for attached devices",
            category = ToolCategory.HARDWARE,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredFeatures = setOf("android.hardware.usb.host"),
            phase = ToolPhase.V1,
            keywords = setOf("vid", "pid", "endpoints", "interfaces", "descriptors", "accessory"),
            iconKey = ToolIconKeys.USB_DETAIL,
        ),
    )
}

fun NavGraphBuilder.usbGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(UsbFeature.ROUTE) {
        UsbScreen(
            onOpenDetail = { name -> nav.navigate(UsbFeature.detailRoute(name)) { launchSingleTop = true } },
            onBack = { nav.navigateUp() },
        )
    }
    composable(UsbFeature.DETAIL_ROUTE) { backStackEntry ->
        val deviceName = backStackEntry.arguments?.getString("deviceName").orEmpty()
        val parentEntry = nav.getBackStackEntry(UsbFeature.ROUTE)
        UsbDetailScreen(
            deviceName = deviceName,
            viewModel = hiltViewModel(parentEntry),
            onBack = { nav.navigateUp() },
        )
    }
}

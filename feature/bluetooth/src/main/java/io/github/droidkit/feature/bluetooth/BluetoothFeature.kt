package io.github.droidkit.feature.bluetooth

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object BluetoothFeature : FeatureContribution {
    const val ROUTE = "bluetooth"

    override val tools = listOf(
        Tool(
            id = "bluetooth.status",
            title = "Bluetooth Status",
            description = "Adapter state, name, paired devices",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.BLUETOOTH_CONNECT"),
            minSdk = 31,
        ),
        Tool(
            id = "bluetooth.scan",
            title = "BLE Scan",
            description = "Scan for nearby BLE devices",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.BLUETOOTH_CONNECT"),
            requiredFeatures = setOf("android.hardware.bluetooth"),
            minSdk = 31,
        ),
    )
}

fun NavGraphBuilder.bluetoothGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(BluetoothFeature.ROUTE) {
        FeatureStubScreen(
            title = "Bluetooth",
            subtitle = "Bluetooth tools ship with the connectivity pass.",
            tools = BluetoothFeature.tools,
            resolve = resolve,
        )
    }
}

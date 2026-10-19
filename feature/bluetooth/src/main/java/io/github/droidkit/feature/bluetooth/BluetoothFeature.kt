package io.github.droidkit.feature.bluetooth

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution

object BluetoothFeature : FeatureContribution {
    const val ROUTE = "bluetooth"

    override val tools = listOf(
        Tool(
            id = "bluetooth.status",
            title = "Bluetooth Status",
            description = "Adapter state, classic/LE capability, bonded devices",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredPermissions = listOf("android.permission.BLUETOOTH_CONNECT"),
            requiredFeatures = setOf("android.hardware.bluetooth"),
            minSdk = 31,
            phase = ToolPhase.V1,
        ),
        Tool(
            id = "bluetooth.scan",
            title = "BLE Scan",
            description = "Scan for nearby BLE devices (post-V1)",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredPermissions = listOf("android.permission.BLUETOOTH_SCAN"),
            requiredFeatures = setOf("android.hardware.bluetooth_le"),
            minSdk = 31,
            phase = ToolPhase.V2,
        ),
    )
}

fun NavGraphBuilder.bluetoothGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(BluetoothFeature.ROUTE) {
        BluetoothScreen()
    }
}

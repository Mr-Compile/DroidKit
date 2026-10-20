package io.github.droidkit.feature.bluetooth

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
            keywords = setOf("bt", "adapter", "bonded", "paired"),
            iconKey = ToolIconKeys.BLUETOOTH,
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
            keywords = setOf("ble", "scan", "nearby", "low energy"),
            iconKey = ToolIconKeys.BLE,
        ),
        Tool(
            id = "bluetooth.gatt",
            title = "GATT Explorer",
            description = "Connect to a discovered device and inspect services (read-only)",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            safetyLevel = SafetyLevel.MINOR,
            requiredPermissions = listOf("android.permission.BLUETOOTH_CONNECT"),
            requiredFeatures = setOf("android.hardware.bluetooth_le"),
            minSdk = 31,
            phase = ToolPhase.V2,
            keywords = setOf("gatt", "services", "characteristics", "connect"),
            iconKey = ToolIconKeys.GATT,
        ),
    )

    fun gattRoute(address: String) = "$ROUTE/gatt/$address"
}

fun NavGraphBuilder.bluetoothGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(BluetoothFeature.ROUTE) {
        BluetoothScreen(
            onOpenDevice = { address -> nav.navigate(BluetoothFeature.gattRoute(address)) { launchSingleTop = true } },
            onBack = { nav.navigateUp() },
        )
    }
    composable("${BluetoothFeature.ROUTE}/gatt/{address}") {
        GattExplorerScreen(onBack = { nav.navigateUp() })
    }
}

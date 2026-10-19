package io.github.droidkit.feature.network

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object NetworkFeature : FeatureContribution {
    const val ROUTE = "network"

    override val tools = listOf(
        Tool(
            id = "network.wifi",
            title = "Wi-Fi Info",
            description = "SSID, signal, link speed, IP details",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            requiredPermissions = listOf(
                "android.permission.ACCESS_WIFI_STATE",
                "android.permission.ACCESS_FINE_LOCATION",
            ),
        ),
        Tool(
            id = "network.interfaces",
            title = "Network Interfaces",
            description = "Cellular, VPN and interface state",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.ACCESS_NETWORK_STATE"),
        ),
    )
}

fun NavGraphBuilder.networkGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(NetworkFeature.ROUTE) {
        FeatureStubScreen(
            title = "Network",
            subtitle = "Wi-Fi, cellular and interface tools. Identifier access is OS/version/permission dependent.",
            tools = NetworkFeature.tools,
            resolve = resolve,
        )
    }
}

package io.github.droidkit.feature.network

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution

object NetworkFeature : FeatureContribution {
    const val ROUTE = "network"

    private val NETWORK_PERMS = listOf(
        "android.permission.INTERNET",
        "android.permission.ACCESS_NETWORK_STATE",
    )

    override val tools = listOf(
        Tool(
            id = "network.inspector",
            title = "Connection Inspector",
            description = "Active transports, capabilities and link properties",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredPermissions = NETWORK_PERMS,
            phase = ToolPhase.V1,
        ),
        Tool(
            id = "network.ping",
            title = "Ping",
            description = "ICMP reachability probe with latency samples",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredPermissions = NETWORK_PERMS,
            phase = ToolPhase.V1,
        ),
        Tool(
            id = "network.dns",
            title = "DNS Lookup",
            description = "Resolve hostnames via the device resolver",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredPermissions = NETWORK_PERMS,
            phase = ToolPhase.V1,
        ),
        Tool(
            id = "network.diagnostics",
            title = "Connectivity Diagnostics",
            description = "Per-layer checks: network, internet, DNS, reachability",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredPermissions = NETWORK_PERMS,
            phase = ToolPhase.V1,
        ),
    )
}

fun NavGraphBuilder.networkGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(NetworkFeature.ROUTE) {
        NetworkScreen()
    }
}

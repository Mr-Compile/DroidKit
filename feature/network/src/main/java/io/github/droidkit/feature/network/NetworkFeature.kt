package io.github.droidkit.feature.network

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolIconKeys
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
            keywords = setOf("connection", "transport", "wifi", "cellular", "ip"),
            iconKey = ToolIconKeys.NETWORK,
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
            keywords = setOf("latency", "icmp", "reachability", "echo"),
            iconKey = ToolIconKeys.PING,
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
            keywords = setOf("resolve", "hostname", "lookup", "resolver"),
            iconKey = ToolIconKeys.DNS,
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
            keywords = setOf("internet", "layers", "checks", "troubleshoot"),
            iconKey = ToolIconKeys.TROUBLESHOOT,
        ),
        Tool(
            id = "network.ports",
            title = "Port Reachability",
            description = "TCP connect probes on user-specified host and ports",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredPermissions = NETWORK_PERMS,
            phase = ToolPhase.V1,
            keywords = setOf("tcp", "port", "connect", "open"),
            iconKey = ToolIconKeys.PORTS,
        ),
        Tool(
            id = "network.interfaces",
            title = "Interface Detail",
            description = "Every network interface: MTU, flags, addresses",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredPermissions = NETWORK_PERMS,
            keywords = setOf("mtu", "interface", "address", "flags", "lan"),
            iconKey = ToolIconKeys.LAN,
        ),
        Tool(
            id = "network.discovery",
            title = "Service Discovery",
            description = "Bounded mDNS/NSD browse of local services",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            safetyLevel = SafetyLevel.MINOR,
            requiredPermissions = NETWORK_PERMS + "android.permission.CHANGE_WIFI_MULTICAST_STATE",
            keywords = setOf("mdns", "nsd", "browse", "services", "local"),
            iconKey = ToolIconKeys.RADAR,
        ),
        Tool(
            id = "network.wol",
            title = "Wake-on-LAN",
            description = "Broadcast a magic packet to wake a configured host",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            safetyLevel = SafetyLevel.MINOR,
            requiredPermissions = NETWORK_PERMS,
            keywords = setOf("wol", "wake", "magic packet", "power on", "lan"),
            iconKey = ToolIconKeys.WIFI,
        ),
        Tool(
            id = "network.lanscan",
            title = "LAN Sweep",
            description = "Time-boxed reachability sweep of the local subnet",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredPermissions = NETWORK_PERMS,
            keywords = setOf("lan", "sweep", "hosts", "subnet", "discover"),
            iconKey = ToolIconKeys.LAN,
        ),
        Tool(
            id = "network.saved",
            title = "Saved Wi-Fi Networks",
            description = "Saved SSIDs and passwords read from the system store — root only",
            category = ToolCategory.CONNECTIVITY,
            route = "$ROUTE/saved",
            safetyLevel = SafetyLevel.ADVANCED,
            requiredCapabilities = setOf(Capability.ROOT),
            phase = ToolPhase.V2,
            keywords = setOf("password", "saved", "ssid", "psk", "credentials", "share"),
            iconKey = ToolIconKeys.KEY,
        ),
    )
}

fun NavGraphBuilder.networkGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(NetworkFeature.ROUTE) {
        NetworkScreen(
            onBack = { nav.navigateUp() },
            onOpenSavedNetworks = { nav.navigate("${NetworkFeature.ROUTE}/saved") },
        )
    }
    composable("${NetworkFeature.ROUTE}/saved") {
        SavedNetworksScreen(onBack = { nav.navigateUp() })
    }
}

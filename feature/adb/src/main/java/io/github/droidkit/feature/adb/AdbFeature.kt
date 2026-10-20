package io.github.droidkit.feature.adb

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

object AdbFeature : FeatureContribution {
    const val ROUTE = "adb"

    override val tools = listOf(
        Tool(
            id = "adb.pairing",
            title = "Wireless ADB Pairing",
            description = "Pair with the device's own wireless debugging service",
            category = ToolCategory.DEVELOPER,
            route = ROUTE,
            safetyLevel = SafetyLevel.SYSTEM_CHANGE,
            requiredCapabilities = setOf(Capability.ADB_SHELL),
            phase = ToolPhase.V2,
            keywords = setOf("wireless", "pair", "debug", "wifi adb"),
            iconKey = ToolIconKeys.LINK,
        ),
        Tool(
            id = "adb.actions",
            title = "ADB Device Actions",
            description = "Device info, authorized logs and scoped power actions over ADB",
            category = ToolCategory.DEVELOPER,
            route = ROUTE,
            safetyLevel = SafetyLevel.ADVANCED,
            requiredCapabilities = setOf(Capability.ADB_SHELL),
            phase = ToolPhase.V2,
            keywords = setOf("shell", "command", "logs", "power"),
            iconKey = ToolIconKeys.TERMINAL,
        ),
    )
}

fun NavGraphBuilder.adbGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(AdbFeature.ROUTE) {
        AdbScreen(onBack = { nav.navigateUp() })
    }
}

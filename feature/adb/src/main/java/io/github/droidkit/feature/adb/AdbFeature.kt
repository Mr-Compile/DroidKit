package io.github.droidkit.feature.adb

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

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
        ),
        Tool(
            id = "adb.shell",
            title = "ADB Shell",
            description = "Run shell commands through the wireless ADB provider",
            category = ToolCategory.DEVELOPER,
            route = ROUTE,
            safetyLevel = SafetyLevel.ADVANCED,
            requiredCapabilities = setOf(Capability.ADB_SHELL),
            phase = ToolPhase.V2,
        ),
    )
}

fun NavGraphBuilder.adbGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(AdbFeature.ROUTE) {
        FeatureStubScreen(
            title = "Wireless ADB",
            subtitle = "Pairing/session provider — post-V1 journey J19.",
            tools = AdbFeature.tools,
            resolve = resolve,
        )
    }
}

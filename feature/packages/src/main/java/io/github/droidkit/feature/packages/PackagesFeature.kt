package io.github.droidkit.feature.packages

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

object PackagesFeature : FeatureContribution {
    const val ROUTE = "packages"

    override val tools = listOf(
        Tool(
            id = "packages.disable",
            title = "Disable Package",
            description = "Enable/disable packages — bloatware control",
            category = ToolCategory.APPS,
            route = ROUTE,
            safetyLevel = SafetyLevel.HIGH_IMPACT,
            requiredCapabilities = setOf(Capability.ADB_SHELL),
            phase = ToolPhase.V2,
        ),
        Tool(
            id = "packages.clear",
            title = "Clear App Data",
            description = "Wipe an app's data — irreversible",
            category = ToolCategory.APPS,
            route = ROUTE,
            safetyLevel = SafetyLevel.HIGH_IMPACT,
            requiredCapabilities = setOf(Capability.ADB_SHELL),
            phase = ToolPhase.V2,
        ),
        Tool(
            id = "packages.uninstall",
            title = "Uninstall for User",
            description = "Uninstall package for current user profile",
            category = ToolCategory.APPS,
            route = ROUTE,
            safetyLevel = SafetyLevel.HIGH_IMPACT,
            requiredCapabilities = setOf(Capability.ADB_SHELL),
            phase = ToolPhase.V2,
        ),
    )
}

fun NavGraphBuilder.packagesGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(PackagesFeature.ROUTE) {
        FeatureStubScreen(
            title = "Package Controls",
            subtitle = "High-impact package operations gated on wireless ADB — post-V1 journeys J18/J19. " +
                "Each action will show its exact consequence before running.",
            tools = PackagesFeature.tools,
            resolve = resolve,
        )
    }
}

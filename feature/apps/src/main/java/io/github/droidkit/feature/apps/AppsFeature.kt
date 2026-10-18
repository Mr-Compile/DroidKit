package io.github.droidkit.feature.apps

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object AppsFeature : FeatureContribution {
    const val ROUTE = "apps"

    override val tools = listOf(
        Tool(
            id = "apps.installed",
            title = "Installed Apps",
            description = "User and system apps with package metadata",
            category = ToolCategory.APPS,
            route = ROUTE,
        ),
        Tool(
            id = "apps.apk",
            title = "APK Inspector",
            description = "Metadata and components of installed packages",
            category = ToolCategory.APPS,
            route = ROUTE,
        ),
    )
}

fun NavGraphBuilder.appsGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(AppsFeature.ROUTE) {
        FeatureStubScreen(
            title = "App Inspector",
            subtitle = "Package visibility on modern Android limits what's listed — full inspector ships in Phase 5.",
            tools = AppsFeature.tools,
            resolve = resolve,
        )
    }
}

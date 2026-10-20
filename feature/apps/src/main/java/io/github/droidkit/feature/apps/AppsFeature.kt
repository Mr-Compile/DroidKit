package io.github.droidkit.feature.apps

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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

object AppsFeature : FeatureContribution {
    const val ROUTE = "apps"
    const val DETAIL_ROUTE = "apps/detail/{packageName}"

    fun detailRoute(packageName: String) = "apps/detail/$packageName"

    override val tools = listOf(
        Tool(
            id = "apps.installed",
            title = "App Inspector",
            description = "Visible installed apps with package metadata",
            category = ToolCategory.APPS,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            phase = ToolPhase.V1,
            keywords = setOf("packages", "apk", "installed", "apps"),
            iconKey = ToolIconKeys.APPS,
        ),
        Tool(
            id = "apps.components",
            title = "Component Explorer",
            description = "Activities, services, receivers and providers per app",
            category = ToolCategory.APPS,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            phase = ToolPhase.V1,
            keywords = setOf("activities", "services", "receivers", "providers"),
            iconKey = ToolIconKeys.COMPONENTS,
        ),
        Tool(
            id = "apps.permissions",
            title = "Permission Inspector",
            description = "Requested permissions grouped by grant state",
            category = ToolCategory.APPS,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            phase = ToolPhase.V1,
            keywords = setOf("perms", "granted", "denied", "requested"),
            iconKey = ToolIconKeys.APP_PERMS,
        ),
    )
}

fun NavGraphBuilder.appsGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(AppsFeature.ROUTE) {
        AppsScreen(
            onOpenDetail = { pkg -> nav.navigate(AppsFeature.detailRoute(pkg)) { launchSingleTop = true } },
            onBack = { nav.navigateUp() },
        )
    }
    composable(AppsFeature.DETAIL_ROUTE) { backStackEntry ->
        val packageName = backStackEntry.arguments?.getString("packageName").orEmpty()
        val parentEntry = nav.getBackStackEntry(AppsFeature.ROUTE)
        AppDetailScreen(
            packageName = packageName,
            viewModel = hiltViewModel(parentEntry),
            onBack = { nav.navigateUp() },
        )
    }
}

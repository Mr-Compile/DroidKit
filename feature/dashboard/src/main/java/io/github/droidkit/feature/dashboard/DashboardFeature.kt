package io.github.droidkit.feature.dashboard

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.ui.navigation.navigateTopLevel
import io.github.droidkit.core.ui.navigation.openRoute

object DashboardFeature : FeatureContribution {
    const val ROUTE = "home"

    // The dashboard is a top-level tab, not a launchable tool — the old
    // "dashboard.overview" entry masqueraded as a device screen while only
    // switching tabs to Home. Device info lives at deviceinfo.overview.
    override val tools = emptyList<Tool>()
}

fun NavGraphBuilder.dashboardGraph(nav: NavHostController) {
    composable(DashboardFeature.ROUTE) {
        DashboardScreen(
            // Top-level jumps mirror bottom-nav semantics (UI v6 plan 16).
            onNavigateToTools = { nav.navigateTopLevel("tools") },
            onNavigateToDiagnose = { nav.navigateTopLevel("diagnose") },
            onOpenTool = { route -> nav.openRoute(route) },
            onOpenInsights = { nav.openRoute("insights") },
            onOpenDeviceInfo = { nav.openRoute("deviceinfo") },
        )
    }
}

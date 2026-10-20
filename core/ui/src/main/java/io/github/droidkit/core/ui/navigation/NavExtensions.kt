package io.github.droidkit.core.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * Bottom-bar tab routes (UI v6 plan 16). A tool or search entry whose launch
 * route points at one of these must switch tabs, not push a duplicate —
 * pushing e.g. "home" leaves a second Home instance on the stack and reads
 * as a dead navigation.
 */
val TOP_LEVEL_ROUTES: Set<String> = setOf("home", "tools", "diagnose", "settings")

/** Bottom-bar semantics: one instance per tab, state preserved. */
fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Open a tool/search-entry route: tab-switch for top-level routes, push otherwise. */
fun NavHostController.openRoute(route: String) {
    if (route in TOP_LEVEL_ROUTES) navigateTopLevel(route)
    else navigate(route) { launchSingleTop = true }
}

package io.github.droidkit.core.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The app's top-level destinations — the only surfaces that get primary nav
 * chrome (bottom bar at compact sizes, rail at medium/expanded). Single source
 * for `NavigationSuiteScaffold` items and [TOP_LEVEL_ROUTES].
 */
data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val TOP_LEVEL_DESTINATIONS: List<TopLevelDestination> = listOf(
    TopLevelDestination("home", "Home", Icons.Filled.Home),
    TopLevelDestination("tools", "Tools", Icons.Filled.Build),
    TopLevelDestination("diagnose", "Diagnose", Icons.Filled.Info),
    TopLevelDestination("settings", "Settings", Icons.Filled.Settings),
)

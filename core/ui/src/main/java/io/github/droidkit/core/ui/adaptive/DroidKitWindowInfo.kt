package io.github.droidkit.core.ui.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import io.github.droidkit.core.ui.navigation.NavChrome
import io.github.droidkit.core.ui.navigation.navChromeFor

/** Width-class breakpoints (dp) shared with the nav-suite rule. */
const val WIDTH_MEDIUM_BREAKPOINT_DP = 600
const val WIDTH_EXPANDED_BREAKPOINT_DP = 840

/**
 * Window-derived layout facts for screens (plan 2026-10-20-responsive-adaptive-ui).
 * Values come from the current window configuration so they stay correct under
 * multi-window, foldable, and rotation changes.
 */
data class DroidKitWindowInfo(
    val widthDp: Int,
    val heightDp: Int,
    val navChrome: NavChrome,
) {
    val isCompactWidth: Boolean get() = widthDp < WIDTH_MEDIUM_BREAKPOINT_DP
    val isMediumWidth: Boolean get() =
        widthDp >= WIDTH_MEDIUM_BREAKPOINT_DP && widthDp < WIDTH_EXPANDED_BREAKPOINT_DP
    val isExpandedWidth: Boolean get() = widthDp >= WIDTH_EXPANDED_BREAKPOINT_DP
    val isLandscape: Boolean get() = widthDp > heightDp
    val showsRail: Boolean get() = navChrome == NavChrome.Rail
}

@Composable
fun rememberDroidKitWindowInfo(): DroidKitWindowInfo {
    val posture = currentWindowAdaptiveInfo().windowPosture
    val config = LocalConfiguration.current
    return DroidKitWindowInfo(
        widthDp = config.screenWidthDp,
        heightDp = config.screenHeightDp,
        navChrome = navChromeFor(
            widthDp = config.screenWidthDp,
            heightDp = config.screenHeightDp,
            tabletop = posture.isTabletop,
        ),
    )
}

package io.github.droidkit.core.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Width/height breakpoints mirrored from the suite's default rule for tests. */
private const val WIDTH_MEDIUM_DP = 600
private const val HEIGHT_MEDIUM_DP = 480

enum class NavChrome { BottomBar, Rail }

/**
 * The NavigationSuiteScaffold default mapping, as a pure function for tests and
 * window-info consumers: bottom bar when width or height is compact (a phone in
 * landscape keeps its bottom bar) or in tabletop posture; rail otherwise.
 */
fun navChromeFor(widthDp: Int, heightDp: Int, tabletop: Boolean = false): NavChrome =
    if (!tabletop && widthDp >= WIDTH_MEDIUM_DP && heightDp >= HEIGHT_MEDIUM_DP) {
        NavChrome.Rail
    } else {
        NavChrome.BottomBar
    }

/**
 * Adaptive nav chrome (plan 2026-10-20-responsive-adaptive-ui): the suite picks
 * NavigationBar vs NavigationRail itself from the window size class. Chrome only
 * renders on top-level destinations — secondary screens keep the Up affordance,
 * matching the pre-adaptive shell's rule.
 */
@Composable
fun DroidKitNavigationSuite(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (currentRoute !in TOP_LEVEL_ROUTES) {
        content()
        return
    }
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TOP_LEVEL_DESTINATIONS.forEach { dest ->
                item(
                    selected = currentRoute == dest.route,
                    onClick = { onNavigate(dest.route) },
                    icon = { Icon(dest.icon, contentDescription = dest.label) },
                    label = { Text(dest.label) },
                )
            }
        },
        modifier = modifier,
        content = content,
    )
}

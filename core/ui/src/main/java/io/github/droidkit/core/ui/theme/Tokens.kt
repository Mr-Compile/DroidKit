package io.github.droidkit.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * V5 semantic tokens (docs/03_design_system/uiux_v5/foundation/02_TOKENS_COMPONENTS.md).
 * Status is never conveyed by color alone — consumers pair these with a label or icon.
 */
object DroidKitStatusColors {
    val Success = Color(0xFF3CE88A)
    val Warning = Color(0xFFF6C453)
    val Info = Color(0xFF14B8CC)
    val Unsupported = Color(0xFF8B95A3)
    val SetupRequired = Color(0xFFB08CFF)
}

/** V5 spacing scale: 4/8/12/16/20/24/32/40/48. */
object DroidKitSpacing {
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Lg = 16.dp
    val Xl = 20.dp
    val Xxl = 24.dp
    val Xxxl = 32.dp
    val Section = 40.dp
    val Hero = 48.dp
}

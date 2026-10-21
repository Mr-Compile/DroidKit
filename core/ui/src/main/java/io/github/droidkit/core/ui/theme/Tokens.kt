package io.github.droidkit.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.ToolCategory

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

/**
 * Per-category glyph accents for tool tiles (UI v6). Hues are fixed hexes —
 * not colorScheme roles — so they stay stable under Material You dynamic
 * color; each ships a dark/light variant for contrast on the neutral tonal
 * tile. Categories encode grouping only, never status: PASS/FAIL surfaces
 * keep using [DroidKitStatusColors] with an icon + label.
 */
object DroidKitCategoryColors {
    private val dark = mapOf(
        ToolCategory.DEVICE to Color(0xFF3CE88A),
        ToolCategory.DIAGNOSTICS to Color(0xFF14B8CC),
        ToolCategory.CONNECTIVITY to Color(0xFF64B5F6),
        ToolCategory.HARDWARE to Color(0xFFF6C453),
        ToolCategory.APPS to Color(0xFFB08CFF),
        ToolCategory.DEVELOPER to Color(0xFFFF8A66),
        ToolCategory.REPORTS to Color(0xFFF38BA8),
    )

    private val light = mapOf(
        ToolCategory.DEVICE to Color(0xFF0E9E6B),
        ToolCategory.DIAGNOSTICS to Color(0xFF0B7C8C),
        ToolCategory.CONNECTIVITY to Color(0xFF2D6BCF),
        ToolCategory.HARDWARE to Color(0xFF9A6A00),
        ToolCategory.APPS to Color(0xFF7A4FD0),
        ToolCategory.DEVELOPER to Color(0xFFC2481F),
        ToolCategory.REPORTS to Color(0xFFB23A6B),
    )

    fun accent(category: ToolCategory, darkTheme: Boolean): Color =
        (if (darkTheme) dark else light).getValue(category)
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

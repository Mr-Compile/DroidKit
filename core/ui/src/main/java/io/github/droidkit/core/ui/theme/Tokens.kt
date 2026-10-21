package io.github.droidkit.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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

/**
 * Per-category glyph accents for Phone Doctor test rows — same fixed-hex
 * scheme as [DroidKitCategoryColors], keyed by PhoneDoctorTest.category.
 * Unknown categories resolve to null so the tile keeps its default tint;
 * PhoneDoctorCatalogTest asserts full coverage of the catalog.
 */
object DroidKitTestColors {
    private val dark = mapOf(
        "Display" to Color(0xFF3CE88A),
        "Touch" to Color(0xFF7CDBF2),
        "Haptics" to Color(0xFFFF8A66),
        "Sensors" to Color(0xFF14B8CC),
        "Audio" to Color(0xFFB08CFF),
        "Camera" to Color(0xFFF38BA8),
        "Connectivity" to Color(0xFF64B5F6),
        "Power" to Color(0xFFF6C453),
        "Buttons" to Color(0xFF9CCC65),
    )

    private val light = mapOf(
        "Display" to Color(0xFF0E9E6B),
        "Touch" to Color(0xFF0E8FA3),
        "Haptics" to Color(0xFFC2481F),
        "Sensors" to Color(0xFF0B7C8C),
        "Audio" to Color(0xFF7A4FD0),
        "Camera" to Color(0xFFB23A6B),
        "Connectivity" to Color(0xFF2D6BCF),
        "Power" to Color(0xFF9A6A00),
        "Buttons" to Color(0xFF558B2F),
    )

    fun accent(category: String, darkTheme: Boolean): Color? =
        (if (darkTheme) dark else light)[category]
}

/** Dark/light resolved against the applied scheme — follows the manual theme override, not the OS. */
@Composable
private fun schemeIsDark(): Boolean {
    val bg = MaterialTheme.colorScheme.background
    return bg.red * 0.2126f + bg.green * 0.7152f + bg.blue * 0.0722f < 0.5f
}

/** Glyph accent for a tool tile from its [ToolCategory]. */
@Composable
fun categoryAccent(category: ToolCategory): Color =
    DroidKitCategoryColors.accent(category, schemeIsDark())

/** Glyph accent for a diagnostic test tile from its category label; null keeps the default tint. */
@Composable
fun testAccent(category: String): Color? =
    DroidKitTestColors.accent(category, schemeIsDark())

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

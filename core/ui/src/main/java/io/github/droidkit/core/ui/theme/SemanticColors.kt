package io.github.droidkit.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * "Dark Instrument" semantic palette — the theme layer behind
 * `mockups/improved-index.html` (v5 spec `docs/03_design_system/uiux_v5/`).
 * Field names mirror the CSS custom properties on `.phone[data-theme]` so the
 * mockup stays a 1:1 reference: `--ink`→ink, `--panel`→panel,
 * `--panel2`→panel2, `--raise`→raise, `--line`→lineBrand,
 * `--line-n`→lineNeutral, `--field`→field, `--faint`→faint.
 */
@Immutable
data class DroidKitSemanticColors(
    /** Primary brand accent (mockup --brand / --ok). */
    val brandPrimary: Color,
    /** Secondary teal accent (mockup --brand2). */
    val brandSecondary: Color,
    val brandGradientStart: Color,
    val brandGradientEnd: Color,
    /** Pre-multiplied glow accent for halo/shadow work (mockup --glow). */
    val brandGlow: Color,
    /** App background (mockup --ink / --surface). */
    val ink: Color,
    /** Card surface (mockup --panel / --card). */
    val panel: Color,
    /** Raised card / hover surface (mockup --panel2 / --card2). */
    val panel2: Color,
    /** Highest surface — dialogs, sheets, toasts, stat tiles (mockup --raise / --tonal). */
    val raise: Color,
    /** Brand hairline border (mockup --line). */
    val lineBrand: Color,
    /** Neutral hairline border (mockup --line-n). */
    val lineNeutral: Color,
    /** Stronger control border (mockup --outline). */
    val outline: Color,
    /** Input field background (mockup --field). */
    val field: Color,
    /** Text on the brand gradient (mockup --onbrand). */
    val onBrand: Color,
    val textPrimary: Color,
    /** Secondary text (mockup --sub). */
    val textSecondary: Color,
    /** Faintest text — micro-labels, meta lines (mockup --faint). */
    val faint: Color,
    /** Disabled/interaction-off text and fill. */
    val textDisabled: Color,
    val statusSuccess: Color,
    val statusWarning: Color,
    val statusError: Color,
    val statusInfo: Color,
    val statusUnsupported: Color,
    val statusSetupRequired: Color,
    /** Translucent brand hover/focus wash (mockup rgba(60,232,138,.07)). */
    val brandWash: Color,
    /** Stronger brand wash for pressed/hover borders (mockup rgba(60,232,138,.09)). */
    val brandWashStrong: Color,
    /** Selected-chip tint (mockup .fchip.sel rgba(60,232,138,.07)). */
    val brandTintSelected: Color,
    /** Icon-tile gradient endpoints (mockup .tic background). */
    val tileTintStart: Color,
    val tileTintEnd: Color,
    /** Hero spec-tile fill (mockup .tile — translucent dark on dark, white on light). */
    val heroTile: Color,
    /** AI chat bubble surface (mockup .msg.ai). */
    val aiBubble: Color,
    /** Modal scrim (mockup .scrim rgba(2,6,6,.62)). */
    val scrim: Color,
) {
    val brandGradient: Brush
        get() = Brush.linearGradient(listOf(brandGradientStart, brandGradientEnd))

    /** Icon-tile gradient wash (mockup .tic background, 135deg). */
    val tileGradient: Brush
        get() = Brush.linearGradient(listOf(tileTintStart, tileTintEnd))
}

val DarkDroidKitColors = DroidKitSemanticColors(
    brandPrimary = Color(0xFF3CE88A),
    brandSecondary = Color(0xFF14B8CC),
    brandGradientStart = Color(0xFF3CE88A),
    brandGradientEnd = Color(0xFF14B8CC),
    brandGlow = Color(0x473CE88A),
    ink = Color(0xFF040808),
    panel = Color(0xFF0B1517),
    panel2 = Color(0xFF0F1D20),
    raise = Color(0xFF15262B),
    lineBrand = Color(0x293CE88A),
    lineNeutral = Color(0x14FFFFFF),
    outline = Color(0x29FFFFFF),
    field = Color(0xFF0A1417),
    onBrand = Color(0xFF04150D),
    textPrimary = Color(0xFFEAF2F0),
    textSecondary = Color(0xFF8FA5A7),
    faint = Color(0xFF6E8385),
    textDisabled = Color(0xFF4A5F5C),
    statusSuccess = Color(0xFF3CE88A),
    statusWarning = Color(0xFFF6C453),
    statusError = Color(0xFFFF7A6E),
    statusInfo = Color(0xFF14B8CC),
    statusUnsupported = Color(0xFF8B95A3),
    statusSetupRequired = Color(0xFFB08CFF),
    brandWash = Color(0x123CE88A),
    brandWashStrong = Color(0x173CE88A),
    brandTintSelected = Color(0x123CE88A),
    tileTintStart = Color(0x213CE88A),
    tileTintEnd = Color(0x0D14B8CC),
    heroTile = Color(0x61040A0A),
    aiBubble = Color(0x0BFFFFFF),
    scrim = Color(0x9E020606),
)

val LightDroidKitColors = DroidKitSemanticColors(
    brandPrimary = Color(0xFF0E9E6B),
    brandSecondary = Color(0xFF0E8FA3),
    brandGradientStart = Color(0xFF0E9E6B),
    brandGradientEnd = Color(0xFF0E8FA3),
    brandGlow = Color(0x2E0E9E6B),
    ink = Color(0xFFEFF5F3),
    panel = Color(0xFFFFFFFF),
    panel2 = Color(0xFFE9F1EE),
    raise = Color(0xFFDFEAE6),
    lineBrand = Color(0x3D0E9E6B),
    lineNeutral = Color(0x1A0C1416),
    outline = Color(0x2E0C1416),
    field = Color(0xFFE7F0ED),
    onBrand = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF0C1416),
    textSecondary = Color(0xFF54666A),
    faint = Color(0xFF7A8B8D),
    textDisabled = Color(0xFF9AAEAB),
    statusSuccess = Color(0xFF0E9E6B),
    statusWarning = Color(0xFF9A6B06),
    statusError = Color(0xFFC93C2F),
    statusInfo = Color(0xFF0E8FA3),
    statusUnsupported = Color(0xFF6B7680),
    statusSetupRequired = Color(0xFF7A5FD0),
    brandWash = Color(0x120E9E6B),
    brandWashStrong = Color(0x170E9E6B),
    brandTintSelected = Color(0x120E9E6B),
    tileTintStart = Color(0x210E9E6B),
    tileTintEnd = Color(0x0D0E8FA3),
    heroTile = Color(0x8CFFFFFF),
    aiBubble = Color(0xB3FFFFFF),
    scrim = Color(0x9E020606),
)

val LocalDroidKitColors = staticCompositionLocalOf { DarkDroidKitColors }

/** Resolved semantic palette for the current theme. */
@Composable
fun droidKitColors(): DroidKitSemanticColors = LocalDroidKitColors.current

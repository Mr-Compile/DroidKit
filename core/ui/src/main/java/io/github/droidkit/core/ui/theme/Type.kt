package io.github.droidkit.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * "Dark Instrument" type ramp — values mirror the mockup CSS 1:1
 * (`mockups/improved-index.html`): tight-tracking titles, letterspaced
 * uppercase micro-labels, monospace technical values.
 *
 * Mapping onto M3 slots:
 * - titleLarge  → #appbar .title (19px w650 ls -.2)
 * - titleMedium → tool/section titles (15–16px w600–650)
 * - titleSmall  → .t-small card headers (14px w650 ls -.1)
 * - bodyLarge   → emphasized content text (.t-med 15px)
 * - bodyMedium  → .t-body (13.5px lh1.45)
 * - bodySmall   → .t-mini supporting text (12px)
 * - labelLarge  → .btn / .btn-outline (14px w600–700 ls .3)
 * - labelMedium → .badge (11px w600 ls .4)
 * - labelSmall  → chips, fine print (11px w500–600)
 */
val DroidKitTypography = Typography(
    headlineSmall = TextStyle(
        fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp,
    ),
    titleLarge = TextStyle(
        fontSize = 19.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontSize = 15.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.1).sp,
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.1).sp,
    ),
    bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(
        fontSize = 13.5.sp, fontWeight = FontWeight.Normal, lineHeight = 19.5.sp,
    ),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp,
    ),
    labelMedium = TextStyle(
        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp,
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.4.sp,
    ),
)

/**
 * Type roles M3 has no slot for — the micro-label vocabulary and technical
 * values that define the Dark Instrument look. Access via
 * `DroidKitTheme.extraTypography` / `LocalDroidKitTypography.current`.
 */
@Immutable
data class DroidKitExtraTypography(
    /** Splash/hero brand type. */
    val brandDisplay: TextStyle = TextStyle(
        fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp,
    ),
    /** .sec-head — uppercase group header with a gradient tick. */
    val sectionLabel: TextStyle = TextStyle(
        fontSize = 10.5.sp, fontWeight = FontWeight.Bold,
        letterSpacing = 2.2.sp,
    ),
    /** .tl / .toolmeta / .provk / .qcat — tiny uppercase metadata. */
    val microLabel: TextStyle = TextStyle(
        fontSize = 9.5.sp, fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
    ),
    /** .flabel — input labels. */
    val fieldLabel: TextStyle = TextStyle(
        fontSize = 10.sp, fontWeight = FontWeight.Bold,
        letterSpacing = 1.6.sp,
    ),
    /** .res — PASS/FAIL/WARN/SKIP verdict pills. */
    val verdictPill: TextStyle = TextStyle(
        fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.1.sp,
    ),
    /** .toolstat — "12 tools · 9 ready now" counters. */
    val counterLabel: TextStyle = TextStyle(
        fontSize = 10.5.sp, fontWeight = FontWeight.Bold,
        letterSpacing = 1.3.sp,
    ),
    /** Bottom-nav item labels (.bnav). */
    val navLabel: TextStyle = TextStyle(
        fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
    ),
    /** .inforow .v — right-aligned monospace technical value. */
    val technicalValue: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 12.5.sp, fontWeight = FontWeight.Normal,
    ),
    /** Larger monospace blocks (getprop dumps, hex). */
    val technicalData: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp, fontWeight = FontWeight.Normal,
    ),
    /** .stat .n / donut percentage — tabular display figures. */
    val statNumber: TextStyle = TextStyle(
        fontSize = 19.sp, fontWeight = FontWeight.Bold,
    ),
)

val LocalDroidKitTypography = staticCompositionLocalOf { DroidKitExtraTypography() }

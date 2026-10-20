package io.github.droidkit.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Brand tokens for the DroidKit mark (Candidate 9 — futuristic glowing
 * Android head). Source of truth and usage rules:
 * docs/03_design_system/branding/README.md.
 */
object DroidKitBrand {
    val Green = Color(0xFF3CE88A)
    val Teal = Color(0xFF14B8CC)
    val GreenOnLight = Color(0xFF0E9E6B)
    val Glow = Color(0x593CE88A)
    val GlassTop = Color(0xFF1A2C31)
    val GlassBottom = Color(0xFF0A1214)
    val Ink = Color(0xFF0C1416)
    val SplashInk = Color(0xFF040808)

    val gradient: Brush
        get() = Brush.linearGradient(listOf(Green, Teal))

    val glass: Brush
        get() = Brush.verticalGradient(listOf(GlassTop, GlassBottom))
}

/**
 * The DroidKit mark as a Compose vector: a glass Android head — dark domed
 * skull with a gradient rim and sheen, two antennae, and glowing green eyes
 * (soft halo + solid core). 108-unit viewport, same geometry as
 * docs/03_design_system/branding/droidkit-mark.svg.
 */
val DroidKitMark: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "DroidKitMark",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 108f,
        viewportHeight = 108f,
    ).addPath(
        pathData = addPathNodes("M39 54 L29 37 M69 54 L79 37"),
        stroke = DroidKitBrand.gradient,
        strokeLineWidth = 6f,
        strokeLineCap = StrokeCap.Round,
    ).addPath(
        pathData = addPathNodes("M24 80 A30 30 0 0 1 84 80 Z"),
        fill = DroidKitBrand.glass,
        stroke = DroidKitBrand.gradient,
        strokeAlpha = 0.9f,
        strokeLineWidth = 1.6f,
    ).addPath(
        pathData = addPathNodes("M30 58 a24 10 0 1 0 48 0 a24 10 0 1 0 -48 0 Z"),
        fill = SolidColor(Color.White.copy(alpha = 0.07f)),
    ).addPath(
        pathData = addPathNodes(
            "M32 64 a10 10 0 1 0 20 0 a10 10 0 1 0 -20 0 Z " +
                "M56 64 a10 10 0 1 0 20 0 a10 10 0 1 0 -20 0 Z",
        ),
        fill = SolidColor(DroidKitBrand.Green.copy(alpha = 0.25f)),
    ).addPath(
        pathData = addPathNodes(
            "M37.5 64 a4.5 4.5 0 1 0 9 0 a4.5 4.5 0 1 0 -9 0 Z " +
                "M61.5 64 a4.5 4.5 0 1 0 9 0 a4.5 4.5 0 1 0 -9 0 Z",
        ),
        fill = SolidColor(DroidKitBrand.Green),
    ).build()
}

package io.github.droidkit.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Brand tokens for the DroidKit mark (Candidate 4 — abstract/geometric).
 * Source of truth and usage rules: docs/03_design_system/branding/README.md.
 */
object DroidKitBrand {
    val Green = Color(0xFF3CE88A)
    val Teal = Color(0xFF14B8CC)
    val GreenOnLight = Color(0xFF0E9E6B)
    val Ink = Color(0xFF0C1416)

    val gradient: Brush
        get() = Brush.linearGradient(listOf(Green, Teal))
}

/**
 * The DroidKit mark as a Compose vector: a rounded hexagonal ring, open at
 * the bottom vertex, around a vertical capsule "tool bit". 108-unit
 * viewport, same geometry as docs/03_design_system/branding/droidkit-mark.svg.
 */
val DroidKitMark: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "DroidKitMark",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 108f,
        viewportHeight = 108f,
    ).addPath(
        pathData = addPathNodes(
            "M47.07 80 L28.02 69 L28.02 39 L54 24 L79.98 39 L79.98 69 L60.93 80",
        ),
        stroke = DroidKitBrand.gradient,
        strokeLineWidth = 7f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Round,
    ).addPath(
        pathData = addPathNodes(
            "M49.5 44.5 Q49.5 40 54 40 Q58.5 40 58.5 44.5 " +
                "L58.5 65.5 Q58.5 70 54 70 Q49.5 70 49.5 65.5 Z",
        ),
        fill = DroidKitBrand.gradient,
    ).build()
}

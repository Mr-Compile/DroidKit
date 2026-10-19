package io.github.droidkit.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
    val CapsuleGreen = Color(0xFF86F595)
    val Ink = Color(0xFF0C1416)

    val gradient: Brush
        get() = Brush.linearGradient(listOf(Green, Teal))
}

/**
 * The DroidKit mark as a Compose vector: a bold hexagonal band split into a
 * large left C-piece (wrapping the top vertex, left side, and bottom vertex)
 * and a right bracket, open at the upper-right edge and the bottom vertex,
 * around a light-green vertical capsule "tool bit". 108-unit viewport, same
 * geometry as docs/03_design_system/branding/droidkit-mark.svg.
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
            "M54 82 L27 66.5 L27 42 L52.5 26 L56.5 28",
        ),
        stroke = DroidKitBrand.gradient,
        strokeLineWidth = 14f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).addPath(
        pathData = addPathNodes(
            "M74.5 37.6 L81.5 42 L81.5 65.5 L64.6 75.6",
        ),
        stroke = DroidKitBrand.gradient,
        strokeLineWidth = 14f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).addPath(
        pathData = addPathNodes(
            "M46.5 42.5 Q46.5 35 54 35 Q61.5 35 61.5 42.5 " +
                "L61.5 65.5 Q61.5 73 54 73 Q46.5 73 46.5 65.5 Z",
        ),
        fill = SolidColor(DroidKitBrand.CapsuleGreen),
    ).build()
}

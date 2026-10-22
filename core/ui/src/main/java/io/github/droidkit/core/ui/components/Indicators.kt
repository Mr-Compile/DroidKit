package io.github.droidkit.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * Glowing status dot (mockup `.vdot`): 7dp disc with a colored halo. Always
 * paired with adjacent text — color is decoration, never the only signal.
 */
@Composable
fun StatusDot(color: Color, modifier: Modifier = Modifier, glow: Boolean = true) {
    Box(
        modifier = modifier
            .size(7.dp)
            .then(
                if (glow) {
                    Modifier.shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        clip = false,
                        ambientColor = color.copy(alpha = 0.55f),
                        spotColor = color.copy(alpha = 0.55f),
                    )
                } else {
                    Modifier
                },
            )
            .background(color, CircleShape),
    )
}

/** Small inline loader (mockup `.spin`) — 12dp ring, brand arc. */
@Composable
fun InlineSpinner(modifier: Modifier = Modifier) {
    val c = DroidKitTheme.colors
    CircularProgressIndicator(
        modifier = modifier.size(12.dp),
        color = c.brandPrimary,
        trackColor = c.lineNeutral,
        strokeWidth = 2.dp,
    )
}

/** Shimmer block (mockup `.skeleton`) for content that is still loading. */
@Composable
fun SkeletonBox(modifier: Modifier = Modifier, height: Dp = 12.dp) {
    val c = DroidKitTheme.colors
    val transition = rememberInfiniteTransition(label = "skeleton")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "skeletonShift",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(6.dp))
            .drawBehind {
                val w = size.width
                val brush = Brush.linearGradient(
                    colors = listOf(c.raise, c.panel2, c.raise),
                    start = Offset(shift * 2 * w - w, 0f),
                    end = Offset(shift * 2 * w, size.height),
                )
                drawRect(brush)
            },
    )
}

/**
 * Determinate/indeterminate progress line (mockup `.progress-line`): 3dp
 * `raise` track filled by the brand gradient with a soft glow. Null
 * [progress] renders an indeterminate sweep.
 */
@Composable
fun DroidKitProgressLine(progress: Float?, modifier: Modifier = Modifier) {
    val c = DroidKitTheme.colors
    val fraction = progress?.coerceIn(0f, 1f)
    val transition = rememberInfiniteTransition(label = "progressLine")
    val sweep by transition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "progressSweep",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(c.raise),
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(3.dp)) {
            if (fraction != null) {
                drawRect(
                    brush = c.brandGradient,
                    size = size.copy(width = size.width * fraction),
                )
            } else {
                val segW = size.width * 0.3f
                drawRect(
                    brush = c.brandGradient,
                    topLeft = Offset(sweep * size.width, 0f),
                    size = size.copy(width = segW),
                )
            }
        }
    }
}

/** 1dp hairline divider (mockup hr/`border-top` separators). */
@Composable
fun DroidKitHairline(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DroidKitTheme.colors.lineNeutral),
    )
}

/**
 * Dashed border (mockup `.gatecard`, `.empty`) — no M3 primitive exists, so
 * this draws a dashed stroke inside the bounds.
 */
fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    width: Dp = 1.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp,
): Modifier = drawBehind {
    val strokeWidth = width.toPx()
    val half = strokeWidth / 2
    drawRoundRect(
        color = color,
        topLeft = Offset(half, half),
        size = size.copy(width = size.width - strokeWidth, height = size.height - strokeWidth),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            ),
        ),
    )
}

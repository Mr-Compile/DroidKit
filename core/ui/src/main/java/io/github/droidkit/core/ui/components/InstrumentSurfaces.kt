package io.github.droidkit.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * Hero card (mockup `.hero`): `panel2` fill with a gradient hairline border
 * and a radial brand glow bleeding in from the top-right. Used for the Home
 * device card and other "focus moment" surfaces.
 */
@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = DroidKitTheme.colors
    val shape = RoundedCornerShape(14.dp)
    val gradientBorder = BorderStroke(
        1.dp,
        Brush.linearGradient(
            listOf(
                c.brandPrimary.copy(alpha = 0.50f),
                c.brandSecondary.copy(alpha = 0.15f),
                c.brandPrimary.copy(alpha = 0.08f),
            ),
        ),
    )
    val inner: @Composable () -> Unit = {
        Box {
            // radial brand glow, top-right (mockup .hero::before)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(c.brandGlow, Color.Transparent),
                        center = Offset(size.width * 0.92f, -size.height * 0.35f),
                        radius = size.minDimension * 1.15f,
                    ),
                    alpha = 0.5f,
                )
            }
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 14.dp)) {
                content()
            }
        }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = c.panel2,
            border = gradientBorder,
            content = inner,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = c.panel2,
            border = gradientBorder,
            content = inner,
        )
    }
}

/** `.hero` spec tile — micro-label over a tabular value. */
@Composable
fun SpecTile(label: String, value: String, modifier: Modifier = Modifier) {
    val c = DroidKitTheme.colors
    Column(
        modifier = modifier
            .background(c.heroTile, RoundedCornerShape(10.dp))
            .border(1.dp, c.lineNeutral, RoundedCornerShape(10.dp))
            .padding(horizontal = 11.dp, vertical = 9.dp),
    ) {
        Text(
            label.uppercase(),
            style = DroidKitTheme.extraTypography.microLabel,
            color = c.faint,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = c.textPrimary,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

/** `.stat` insights tile — large number over a micro-label. */
@Composable
fun StatTile(number: String, label: String, modifier: Modifier = Modifier) {
    val c = DroidKitTheme.colors
    Column(
        modifier = modifier
            .background(c.raise, RoundedCornerShape(10.dp))
            .border(1.dp, c.lineNeutral, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(number, style = DroidKitTheme.extraTypography.statNumber, color = c.textPrimary)
        Text(
            label.uppercase(),
            style = DroidKitTheme.extraTypography.microLabel.copy(fontSize = 10.sp),
            color = c.faint,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/**
 * Gate / restriction card (mockup `.gatecard`): dashed brand hairline, tinted
 * icon tile, title + description. Use for permission-required,
 * setup-required, unsupported and similar states.
 */
@Composable
fun GateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color = DroidKitTheme.colors.statusWarning,
    action: (@Composable () -> Unit)? = null,
) {
    val c = DroidKitTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(c.panel.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
            .dashedBorder(c.lineBrand, cornerRadius = 14.dp)
            .padding(16.dp),
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(accent.copy(alpha = 0.10f), RoundedCornerShape(11.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
        }
        Text(
            title,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.5.sp),
            color = c.textPrimary,
            modifier = Modifier.padding(top = if (icon != null) 10.dp else 0.dp),
        )
        Text(
            message,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.5.sp,
                lineHeight = 18.75.sp,
            ),
            color = c.textSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = if (action != null) 12.dp else 0.dp),
        )
        action?.invoke()
    }
}

/**
 * AI surface card (mockup `.card.ai`): teal hairline + faint aurora wash —
 * reserved for AI-generated content so it's visually distinct from device
 * truth.
 */
@Composable
fun AiSurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = DroidKitTheme.colors
    val shape = RoundedCornerShape(12.dp)
    val aiBg = Brush.linearGradient(
        listOf(
            c.brandPrimary.copy(alpha = 0.06f),
            c.brandSecondary.copy(alpha = 0.05f),
        ),
    )
    val inner: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .background(aiBg)
                .padding(16.dp),
        ) { content() }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = c.panel,
            border = BorderStroke(1.dp, c.brandSecondary.copy(alpha = 0.30f)),
            content = inner,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = c.panel,
            border = BorderStroke(1.dp, c.brandSecondary.copy(alpha = 0.30f)),
            content = inner,
        )
    }
}

/**
 * Mini category icon tile (mockup `.qic`): 28dp rounded tile, category-tinted
 * at 10% fill / 28% border — used in grouped quick-test rows.
 */
@Composable
fun MiniTileIcon(
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .background(accent.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = accent, modifier = Modifier.size(16.dp))
    }
}

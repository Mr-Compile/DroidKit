package io.github.droidkit.feature.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.database.SessionHealth
import io.github.droidkit.core.database.StoragePoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun fmtDay(epochMs: Long): String =
    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epochMs))

/** Result-state ring for one session — PASS/FAIL/everything-else. */
@Composable
fun VerdictDonut(
    pass: Int,
    fail: Int,
    skip: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val passColor = MaterialTheme.colorScheme.primary
    val failColor = MaterialTheme.colorScheme.error
    val skipColor = MaterialTheme.colorScheme.outline
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val pct = if (total == 0) 0 else (pass * 100 / total)
    Box(modifier.size(104.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 14.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = trackColor, startAngle = 0f, sweepAngle = 360f,
                useCenter = false, topLeft = topLeft, size = arcSize,
                style = Stroke(stroke),
            )
            var angle = -90f
            listOf(
                pass.toFloat() to passColor,
                fail.toFloat() to failColor,
                skip.toFloat() to skipColor,
            ).forEach { (count, color) ->
                val frac = if (total == 0) 0f else count / total
                if (frac > 0f) {
                    val sweep = frac * 360f
                    drawArc(
                        color = color, startAngle = angle, sweepAngle = sweep,
                        useCenter = false, topLeft = topLeft, size = arcSize,
                        style = Stroke(stroke),
                    )
                    angle += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$pct%", style = MaterialTheme.typography.titleLarge)
            Text("passed", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** One bar per session — height = share of PASS; red when the session had a failure. */
@Composable
fun PassRateBars(
    points: List<SessionHealth>,
    modifier: Modifier = Modifier,
) {
    val okColor = MaterialTheme.colorScheme.primary
    val failColor = MaterialTheme.colorScheme.error
    val lineColor = MaterialTheme.colorScheme.outline
    Column(modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(96.dp)) {
            val baseline = size.height - 4.dp.toPx()
            drawLine(
                color = lineColor, strokeWidth = 1.dp.toPx(),
                start = Offset(8.dp.toPx(), baseline),
                end = Offset(size.width - 8.dp.toPx(), baseline),
            )
            val n = points.size
            val gap = (size.width - 20.dp.toPx()) / n
            val bw = minOf(36.dp.toPx(), gap - 10.dp.toPx())
            val maxH = baseline - 10.dp.toPx()
            points.forEachIndexed { i, p ->
                val bh = maxOf(3.dp.toPx(), p.passRate * maxH)
                val x = 10.dp.toPx() + i * gap + (gap - bw) / 2
                drawRoundRect(
                    color = if (p.hasFailure) failColor else okColor,
                    topLeft = Offset(x, baseline - bh),
                    size = Size(bw, bh),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            points.forEach { p ->
                Text(
                    fmtDay(p.startedAtEpochMs),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Free-space polyline across storage snapshots. */
@Composable
fun TrendSparkline(
    points: List<StoragePoint>,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.fillMaxWidth().height(44.dp)) {
        if (points.size < 2) return@Canvas
        val min = points.minOf { it.freeBytes }
        val span = (points.maxOf { it.freeBytes } - min).coerceAtLeast(1L).toFloat()
        val padX = 10.dp.toPx()
        val padY = 8.dp.toPx()
        val stepX = (size.width - 2 * padX) / (points.size - 1)
        val offsets = points.mapIndexed { i, p ->
            Offset(
                x = padX + i * stepX,
                y = padY + (1f - (p.freeBytes - min) / span) * (size.height - 2 * padY),
            )
        }
        val path = Path().apply {
            moveTo(offsets.first().x, offsets.first().y)
            offsets.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, color = color, style = Stroke(2.dp.toPx()))
        drawCircle(color = color, radius = 3.5.dp.toPx(), center = offsets.last())
    }
}

/** Small counter tile for the activity grid. */
@Composable
fun StatTile(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text("$value", style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Thin severity/usage bar. */
@Composable
fun UsageBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .padding(top = 5.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(2.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {}
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f)),
            shape = RoundedCornerShape(2.dp),
            color = color,
        ) {}
    }
}

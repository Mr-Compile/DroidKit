package io.github.droidkit.feature.display

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import kotlin.math.roundToInt

/**
 * Motion/refresh observation pattern (post-V1 display lab). A translating box
 * and scrolling bars make dropped frames, judder, and refresh-rate modes
 * visible to the naked eye — it reports nothing, the technician observes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotionPatternScreen(onBack: () -> Unit) {
    var speedMs by remember { mutableStateOf(2000) }
    var pattern by remember { mutableStateOf(Pattern.BOUNCE) }

    val transition = rememberInfiniteTransition(label = "motion")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = speedMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "progress",
    )

    Scaffold(topBar = { DroidKitTopAppBar("Motion pattern", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Watch the pattern while switching refresh modes or inspecting " +
                    "for judder/dropped frames. This is a visual aid — it does not " +
                    "measure frame timing.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pattern.entries.forEach { p ->
                    FilterChip(
                        selected = pattern == p,
                        onClick = { pattern = p },
                        label = { Text(p.label) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(4000 to "Slow", 2000 to "Medium", 800 to "Fast").forEach { (ms, label) ->
                    FilterChip(
                        selected = speedMs == ms,
                        onClick = { speedMs = ms },
                        label = { Text(label) },
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                val boxDp = 48.dp
                val boxPx = with(LocalDensity.current) { boxDp.toPx() }
                val widthPx = constraints.maxWidth.toFloat()
                val heightPx = constraints.maxHeight.toFloat()

                when (pattern) {
                    Pattern.BOUNCE -> {
                        // Box sweeps horizontally and returns — judder shows as stepping.
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        ((widthPx - boxPx) * progress).roundToInt(),
                                        ((heightPx - boxPx) / 2f).roundToInt(),
                                    )
                                }
                                .size(boxDp)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                    Pattern.VERTICAL -> {
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        ((widthPx - boxPx) / 2f).roundToInt(),
                                        ((heightPx - boxPx) * progress).roundToInt(),
                                    )
                                }
                                .size(boxDp)
                                .background(MaterialTheme.colorScheme.tertiary),
                        )
                    }
                    Pattern.BARS -> {
                        // Fine scrolling stripes — exposes ghosting/overdrive artifacts.
                        val barPx = with(LocalDensity.current) { 6.dp.toPx() }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    val offset = progress * barPx * 2
                                    var y = -barPx * 2 + offset
                                    var dark = true
                                    while (y < size.height) {
                                        if (dark) {
                                            drawRect(
                                                color = Color.Black.copy(alpha = 0.6f),
                                                topLeft = Offset(0f, y),
                                                size = Size(size.width, barPx),
                                            )
                                        }
                                        dark = !dark
                                        y += barPx
                                    }
                                },
                        )
                    }
                }
            }
            Text(
                "Progress: ${"%.2f".format(progress)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class Pattern(val label: String) {
    BOUNCE("Bounce"),
    VERTICAL("Vertical"),
    BARS("Scrolling bars"),
}

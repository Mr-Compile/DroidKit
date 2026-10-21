package io.github.droidkit.feature.display

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import kotlin.math.min

/**
 * Static pattern suite for panel inspection — banding, uniformity, contrast
 * and geometry. Full-screen mode hides all chrome so the panel is unobstructed;
 * system back or the on-screen button exits. Observer aid only, like the
 * motion pattern — it reports no measurements.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternsScreen(onBack: () -> Unit) {
    var pattern by remember { mutableStateOf(DisplayPattern.GRADIENT) }
    var fullscreen by remember { mutableStateOf(false) }

    BackHandler(enabled = fullscreen) { fullscreen = false }

    if (fullscreen) {
        Box(Modifier.fillMaxSize()) {
            PatternCanvas(pattern, Modifier.fillMaxSize())
            Button(
                onClick = { fullscreen = false },
                modifier = Modifier.padding(16.dp),
            ) { Text("Exit full screen") }
        }
        return
    }

    Scaffold(topBar = { DroidKitTopAppBar("Test patterns", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Static patterns for inspecting banding, uniformity, contrast " +
                    "and geometry. Open full screen to remove all UI. This is a " +
                    "visual aid — it does not measure the panel.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DisplayPattern.entries.take(4).forEach { p ->
                    FilterChip(
                        selected = pattern == p,
                        onClick = { pattern = p },
                        label = { Text(p.label) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DisplayPattern.entries.drop(4).forEach { p ->
                    FilterChip(
                        selected = pattern == p,
                        onClick = { pattern = p },
                        label = { Text(p.label) },
                    )
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f)) {
                PatternCanvas(pattern, Modifier.fillMaxSize())
            }
            Button(onClick = { fullscreen = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Full screen")
            }
        }
    }
}

private enum class DisplayPattern(val label: String) {
    GRADIENT("Gradient"),
    GRAY_STEPS("Gray steps"),
    CHECKER("Checker"),
    BARS("Color bars"),
    CROSSHATCH("Grid"),
    WHITE("White"),
    BLACK("Black"),
}

@Composable
private fun PatternCanvas(pattern: DisplayPattern, modifier: Modifier = Modifier) {
    when (pattern) {
        DisplayPattern.WHITE -> Canvas(modifier) { drawRect(Color.White) }
        DisplayPattern.BLACK -> Canvas(modifier) { drawRect(Color.Black) }
        DisplayPattern.GRADIENT -> Canvas(modifier) {
            // Smooth vertical ramp — banding shows as discrete steps.
            drawRect(Brush.verticalGradient(listOf(Color.Black, Color.White)))
        }
        DisplayPattern.GRAY_STEPS -> Canvas(modifier) {
            // Discrete 16-step ramp — exposes crushed shadows / clipped whites.
            val stepW = size.width / 16
            for (i in 0 until 16) {
                val v = i / 15f
                drawRect(
                    Color(v, v, v),
                    topLeft = Offset(i * stepW, 0f),
                    size = Size(stepW + 1f, size.height),
                )
            }
        }
        DisplayPattern.CHECKER -> Canvas(modifier) {
            // 50/50 checker — contrast and brightness uniformity.
            val cell = min(size.width, size.height) / 8f
            var y = 0f
            var row = 0
            while (y < size.height) {
                var x = 0f
                var col = 0
                while (x < size.width) {
                    if ((row + col) % 2 == 0) {
                        drawRect(
                            Color.White,
                            topLeft = Offset(x, y),
                            size = Size(min(cell, size.width - x), min(cell, size.height - y)),
                        )
                    }
                    x += cell
                    col++
                }
                y += cell
                row++
            }
        }
        DisplayPattern.BARS -> Canvas(modifier) {
            // SMPTE-style vertical bars — channel swaps and tint issues.
            val colors = listOf(
                Color(0.75f, 0.75f, 0.75f), Color(0.75f, 0.75f, 0f),
                Color(0f, 0.75f, 0.75f), Color(0f, 0.75f, 0f),
                Color(0.75f, 0f, 0.75f), Color(0.75f, 0f, 0f), Color(0f, 0f, 0.75f),
            )
            val barW = size.width / colors.size
            colors.forEachIndexed { i, c ->
                drawRect(c, topLeft = Offset(i * barW, 0f), size = Size(barW + 1f, size.height))
            }
        }
        DisplayPattern.CROSSHATCH -> Canvas(modifier) {
            // Grid — geometry distortion and dead-zone spotting.
            drawRect(Color.Black)
            val step = min(size.width, size.height) / 10f
            var x = step
            while (x < size.width) {
                drawLine(Color.White.copy(alpha = 0.7f), Offset(x, 0f), Offset(x, size.height), 2f)
                x += step
            }
            var y = step
            while (y < size.height) {
                drawLine(Color.White.copy(alpha = 0.7f), Offset(0f, y), Offset(size.width, y), 2f)
                y += step
            }
        }
    }
}

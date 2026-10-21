package io.github.droidkit.feature.diagnostics.tests

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.feature.diagnostics.PhoneDoctorTest
import kotlin.math.floor

private const val COLS = 8
private const val ROWS = 12
private const val TOTAL = COLS * ROWS
private const val PASS_THRESHOLD = 0.9f

@Composable
fun TouchGridScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
) {
    var covered by remember { mutableStateOf(setOf<Int>()) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    fun cellAt(offset: Offset): Int? {
        if (canvasSize.width <= 0f || canvasSize.height <= 0f) return null
        val col = floor(offset.x / (canvasSize.width / COLS)).toInt().coerceIn(0, COLS - 1)
        val row = floor(offset.y / (canvasSize.height / ROWS)).toInt().coerceIn(0, ROWS - 1)
        return row * COLS + col
    }

    fun mark(offset: Offset) {
        cellAt(offset)?.let { cell ->
            if (cell !in covered) covered = covered + cell
        }
    }

    val coverage = covered.size.toFloat() / TOTAL

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            "${test.title} — ${covered.size}/$TOTAL cells covered",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(16.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { mark(it) })
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { mark(it) },
                        onDrag = { change, _ -> mark(change.position) },
                    )
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                canvasSize = size
                val cellW = size.width / COLS
                val cellH = size.height / ROWS

                covered.forEach { cell ->
                    val col = cell % COLS
                    val row = cell / COLS
                    drawRect(
                        color = Color(0xFF4CAF50).copy(alpha = 0.4f),
                        topLeft = Offset(col * cellW, row * cellH),
                        size = Size(cellW, cellH),
                    )
                }
                for (c in 0..COLS) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.5f),
                        start = Offset(c * cellW, 0f),
                        end = Offset(c * cellW, size.height),
                        strokeWidth = 1f,
                    )
                }
                for (r in 0..ROWS) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.5f),
                        start = Offset(0f, r * cellH),
                        end = Offset(size.width, r * cellH),
                        strokeWidth = 1f,
                    )
                }
            }
        }
        TestResultBar(
            onResult = { state, note ->
                val coverageNote = "Coverage ${covered.size}/$TOTAL (${(coverage * 100).toInt()}%)"
                onResult(state, listOfNotNull(note, coverageNote).joinToString("; "))
            },
            passEnabled = coverage >= PASS_THRESHOLD,
        )
    }
}

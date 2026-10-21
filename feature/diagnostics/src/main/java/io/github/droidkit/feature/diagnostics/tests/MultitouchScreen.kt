package io.github.droidkit.feature.diagnostics.tests

import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.feature.diagnostics.PhoneDoctorTest

private val pointerColors = listOf(
    Color(0xFFE91E63),
    Color(0xFF3F51B5),
    Color(0xFF009688),
    Color(0xFFFF9800),
    Color(0xFF9C27B0),
    Color(0xFF4CAF50),
    Color(0xFFFFC107),
    Color(0xFF00BCD4),
    Color(0xFF795548),
    Color(0xFF607D8B),
)

@Composable
fun MultitouchScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
) {
    var activePointers by remember { mutableStateOf(mapOf<Int, Offset>()) }
    var maxCount by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            "${test.title} — active: ${activePointers.size}, max seen: $maxCount",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(16.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val next = mutableMapOf<Int, Offset>()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    next[change.id.value.toInt()] = change.position
                                }
                                if (change.positionChanged()) change.consume()
                            }
                            activePointers = next
                            if (next.size > maxCount) maxCount = next.size
                        }
                    }
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                activePointers.entries.forEach { (id, pos) ->
                    val color = pointerColors[id % pointerColors.size]
                    drawCircle(color = color.copy(alpha = 0.35f), radius = 90f, center = pos)
                    drawCircle(color = color, radius = 90f, center = pos, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
                }
            }
        }
        TestResultBar(
            onResult = { state, note ->
                val countNote = "Max simultaneous pointers: $maxCount"
                onResult(state, listOfNotNull(note, countNote).joinToString("; "))
            },
        )
    }
}

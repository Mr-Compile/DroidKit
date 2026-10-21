package io.github.droidkit.feature.diagnostics.tests

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.feature.diagnostics.PhoneDoctorTest

private val colors = listOf(
    Color(0xFFFF0000),
    Color(0xFF00FF00),
    Color(0xFF0000FF),
    Color.White,
    Color.Black,
)

private val colorNames = listOf("Red", "Green", "Blue", "White", "Black")

/** Fullscreen color cycle used by `display.color` and `display.dead_pixel`. */
@Composable
fun DisplayColorScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
) {
    var colorIndex by rememberSaveable { mutableIntStateOf(0) }
    var controlsVisible by rememberSaveable { mutableStateOf(true) }
    val isDeadPixel = test.id == "display.dead_pixel"

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors[colorIndex])
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            when {
                                controlsVisible -> controlsVisible = false
                                colorIndex < colors.lastIndex -> colorIndex++
                                else -> controlsVisible = true
                            }
                        },
                    )
                },
        )

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Card(modifier = Modifier.statusBarsPadding().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(test.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        test.instructions,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        "Showing ${colorNames[colorIndex]} (${colorIndex + 1}/${colors.size}) — " +
                            if (isDeadPixel) "inspect every pixel before tapping" else "tap to switch color",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        "Tap screen to cycle colors and hide this card",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
        ) {
            TestResultBar(
                onResult = { state, note ->
                    val detail = listOfNotNull(
                        note,
                        "inspected through ${colorNames[colorIndex]} " +
                            "(${colorIndex + 1}/${colors.size})",
                    ).joinToString("; ")
                    onResult(state, detail)
                },
            )
        }
    }
}

package io.github.droidkit.feature.diagnostics.tests

import android.view.KeyEvent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.feature.diagnostics.PhoneDoctorTest

/** Label for a capturable key — null for keys apps cannot see. */
fun volumeKeyLabel(keyCode: Int): String? = when (keyCode) {
    KeyEvent.KEYCODE_VOLUME_UP -> "Volume up"
    KeyEvent.KEYCODE_VOLUME_DOWN -> "Volume down"
    else -> null
}

/**
 * Captures hardware volume-key presses on a focused surface. Volume keys are
 * consumed while this test runs (the system volume will not change). Power
 * and other protected keys never reach apps — the test states that honestly.
 */
@Composable
fun VolumeButtonsScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var upSeen by remember { mutableStateOf(false) }
    var downSeen by remember { mutableStateOf(false) }
    var pressCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .focusRequester(focusRequester)
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action != KeyEvent.ACTION_DOWN) return@onKeyEvent false
                when (event.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_VOLUME_UP -> {
                        upSeen = true; pressCount++; true
                    }
                    KeyEvent.KEYCODE_VOLUME_DOWN -> {
                        downSeen = true; pressCount++; true
                    }
                    else -> false
                }
            }
            .focusable(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(test.title, style = MaterialTheme.typography.titleMedium)
            Text(
                test.instructions,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row { Text("Volume up", Modifier.weight(1f)); Text(if (upSeen) "Pressed" else "Waiting…") }
                Row { Text("Volume down", Modifier.weight(1f)); Text(if (downSeen) "Pressed" else "Waiting…") }
                Text(
                    "Volume presses are consumed while this test is open — the volume " +
                        "level will not change. Power, Bixby and other protected keys " +
                        "never reach apps and cannot be tested this way.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        TestResultBar(
            onResult = { state, note ->
                val detail = listOfNotNull(
                    note,
                    "up=$upSeen down=$downSeen presses=$pressCount".takeIf { pressCount > 0 },
                ).joinToString("; ")
                onResult(state, detail.ifBlank { null })
            },
        )
    }
}

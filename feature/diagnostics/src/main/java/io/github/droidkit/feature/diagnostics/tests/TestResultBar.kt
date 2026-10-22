package io.github.droidkit.feature.diagnostics.tests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.core.ui.components.DroidKitButton
import io.github.droidkit.core.ui.components.DroidKitHairline
import io.github.droidkit.core.ui.components.DroidKitOutlinedButton
import io.github.droidkit.core.ui.components.DroidKitTextField
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * Manual-judgment bar shared by every Phone Doctor test (mockup `.testbar`):
 * panel surface with a top hairline. Tests never auto-pass: the technician
 * taps Pass or Fail.
 */
@Composable
fun TestResultBar(
    onResult: (TestResultState, note: String?) -> Unit,
    modifier: Modifier = Modifier,
    passEnabled: Boolean = true,
) {
    var note by rememberSaveable { mutableStateOf("") }
    var showNote by rememberSaveable { mutableStateOf(false) }
    val c = DroidKitTheme.colors

    Surface(modifier = modifier.fillMaxWidth(), color = c.panel) {
        Column {
            DroidKitHairline()
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                if (showNote) {
                    DroidKitTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = { Text("Note (optional)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true,
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showNote = true }) { Text("Note") }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DroidKitOutlinedButton(
                        onClick = { onResult(TestResultState.FAIL, note.ifBlank { null }) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Fail", color = c.statusError)
                    }
                    TextButton(onClick = { onResult(TestResultState.NOT_TESTED, null) }) {
                        Text("Skip")
                    }
                    DroidKitButton(
                        onClick = { onResult(TestResultState.PASS, note.ifBlank { null }) },
                        enabled = passEnabled,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Pass")
                    }
                }
            }
        }
    }
}

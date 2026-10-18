package io.github.droidkit.feature.diagnostics.tests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.TestResultState

/**
 * Manual-judgment bar shared by every Phone Doctor test.
 * Tests never auto-pass: the technician taps Pass or Fail.
 */
@Composable
fun TestResultBar(
    onResult: (TestResultState, note: String?) -> Unit,
    modifier: Modifier = Modifier,
    passEnabled: Boolean = true,
) {
    var note by rememberSaveable { mutableStateOf("") }
    var showNote by rememberSaveable { mutableStateOf(false) }

    Surface(modifier = modifier.fillMaxWidth(), tonalElevation = 3.dp) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (showNote) {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { onResult(TestResultState.FAIL, note.ifBlank { null }) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Fail", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = { showNote = !showNote }) {
                    Text(if (showNote) "Hide note" else "Note")
                }
                TextButton(onClick = { onResult(TestResultState.NOT_TESTED, null) }) {
                    Text("Skip")
                }
                Button(
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

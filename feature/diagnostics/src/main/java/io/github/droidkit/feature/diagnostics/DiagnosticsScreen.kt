package io.github.droidkit.feature.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.SectionHeader
import java.text.DateFormat
import java.util.Date

private val quickTests = listOf(
    "Display & touch" to "display",
    "Audio" to "audio",
    "Camera & flash" to "camera",
    "Sensors" to "sensors",
    "Battery & charging" to "battery",
    "Network" to "network",
    "Bluetooth" to "bluetooth",
    "USB" to "usb",
    "NFC" to "nfc",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onOpenTool: (String) -> Unit,
    onOpenReports: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val sessions by viewModel.sessions.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Diagnose") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Button(onClick = { viewModel.startSession() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Start Full Diagnostic")
                }
            }
            item {
                OutlinedButton(onClick = onOpenReports, modifier = Modifier.fillMaxWidth()) {
                    Text("Saved reports")
                }
            }

            item { SectionHeader("Quick tests") }
            items(quickTests, key = { it.second }) { (label, route) ->
                Card(onClick = { onOpenTool(route) }, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            item { SectionHeader("History") }
            if (sessions.isEmpty()) {
                item { EmptyState("No diagnostic sessions yet.") }
            } else {
                items(sessions, key = { it.id }) { session ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                DateFormat.getDateTimeInstance().format(Date(session.startedAtEpochMs)),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(session.overallStatus, style = MaterialTheme.typography.bodySmall)
                            Text("v${session.appVersion}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

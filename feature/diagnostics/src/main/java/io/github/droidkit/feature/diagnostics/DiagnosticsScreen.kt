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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.SectionHeader
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onStartGuided: () -> Unit,
    onOpenTool: (String) -> Unit,
    onOpenReports: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val sessions by viewModel.sessions.collectAsState()
    var preset by remember { mutableStateOf(DiagnosticPreset.FULL) }
    var showIntake by remember { mutableStateOf(false) }

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
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    DiagnosticPreset.entries.forEachIndexed { i, p ->
                        SegmentedButton(
                            selected = preset == p,
                            onClick = { preset = p },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = i,
                                count = DiagnosticPreset.entries.size,
                            ),
                        ) { Text("${p.label} (${p.tests.size})") }
                    }
                }
            }
            item {
                Button(onClick = { showIntake = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Start ${preset.label}")
                }
            }
            item {
                OutlinedButton(onClick = onOpenReports, modifier = Modifier.fillMaxWidth()) {
                    Text("Saved reports")
                }
            }

            item { SectionHeader("Quick tests") }
            PhoneDoctorTests.ALL.groupBy { it.category }.forEach { (category, tests) ->
                item {
                    Text(
                        category,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                items(tests, key = { it.id }) { test ->
                    Card(
                        onClick = { onOpenTool(DiagnosticsFeature.testRoute(test.id)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Text(test.title, style = MaterialTheme.typography.bodyLarge)
                        }
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
                            Text(
                                listOfNotNull(
                                    session.preset,
                                    session.customerLabel,
                                    session.deviceLabel,
                                    "v${session.appVersion}",
                                ).joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showIntake) {
        var customer by remember { mutableStateOf("") }
        var device by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showIntake = false },
            title = { Text("Session intake (optional)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customer,
                        onValueChange = { customer = it },
                        label = { Text("Customer / ticket ref") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = device,
                        onValueChange = { device = it },
                        label = { Text("Device label (e.g. shelf #)") },
                        singleLine = true,
                    )
                    Text(
                        "Labels stay on this device and appear on the report.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.armSession(preset, customer, device)
                        showIntake = false
                        onStartGuided()
                    },
                ) { Text("Start") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.armSession(preset, null, null)
                        showIntake = false
                        onStartGuided()
                    },
                ) { Text("Skip") }
            },
        )
    }
}

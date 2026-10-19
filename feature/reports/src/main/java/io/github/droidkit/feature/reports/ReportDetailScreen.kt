package io.github.droidkit.feature.reports

import android.content.Intent
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import io.github.droidkit.core.database.DeviceSnapshotEntity
import io.github.droidkit.core.database.DiagnosticResultEntity
import io.github.droidkit.core.database.DiagnosticSessionEntity
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.components.SectionHeader
import java.io.File
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.launch

private data class DetailState(
    val session: DiagnosticSessionEntity,
    val snapshot: DeviceSnapshotEntity?,
    val results: List<DiagnosticResultEntity>,
)

/** Fields the report contains — shown in the export preview before sharing. */
private val sensitivePreview = listOf(
    "Session ID, date and app version",
    "Device manufacturer, model, Android release, security patch",
    "RAM, storage, display and provider capability summary",
    "Every test result, state and technician note",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    sessionId: String,
    onDeleted: () -> Unit,
    viewModel: ReportsViewModel,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var detail by remember { mutableStateOf<DetailState?>(null) }
    var showExport by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId) {
        val session = viewModel.session(sessionId) ?: return@LaunchedEffect
        detail = DetailState(
            session = session,
            snapshot = viewModel.snapshotFor(session),
            results = viewModel.resultsFor(sessionId),
        )
    }

    val d = detail
    val report = remember(d) {
        d?.let { ReportBuilder.build(it.session, it.snapshot, it.results) }
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Report detail") }) },
    ) { padding ->
        if (d == null) {
            Text(
                "Loading report…",
                modifier = Modifier.padding(padding).padding(16.dp),
            )
            return@Scaffold
        }
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            DateFormat.getDateTimeInstance().format(Date(d.session.startedAtEpochMs)),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            "${d.session.overallStatus} — app v${d.session.appVersion}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            item { SectionHeader("Device") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val s = d.snapshot
                        if (s == null) {
                            Text("No device snapshot captured.")
                        } else {
                            InfoRow("Device", "${s.manufacturer} ${s.model}")
                            InfoRow(
                                "Android",
                                "${s.androidRelease} (API ${s.apiLevel})" +
                                    (s.securityPatch?.let { ", patch $it" } ?: ""),
                            )
                            InfoRow("RAM", "${s.totalRamMb} MB")
                            InfoRow("Display", s.displaySummary)
                            InfoRow("Providers", s.capabilitySummary)
                        }
                    }
                }
            }

            item { SectionHeader("Results") }
            if (d.results.isEmpty()) {
                item { Text("No test results recorded.") }
            } else {
                items(d.results, key = { it.id }) { r ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row {
                                Text(
                                    r.testId,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    r.state.replace('_', ' '),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (r.state == "FAIL") {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                )
                            }
                            r.userNote?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            d.session.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                item { SectionHeader("Technician notes") }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(notes, modifier = Modifier.padding(16.dp))
                    }
                }
            }

            item { SectionHeader("AI summary") }
            item {
                val aiState by viewModel.aiSummary.collectAsState()
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        when (val s = aiState) {
                            ReportsViewModel.AiSummaryState.Idle -> Text(
                                "Send a minimized, redacted copy of this report to Groq " +
                                    "for a technician summary. Optional — the report above " +
                                    "never leaves the device unless you ask.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            ReportsViewModel.AiSummaryState.Loading ->
                                Text("Summarizing…")
                            is ReportsViewModel.AiSummaryState.Text ->
                                Text(s.summary, style = MaterialTheme.typography.bodyMedium)
                            is ReportsViewModel.AiSummaryState.Unavailable -> Text(
                                s.reason,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.summarize(
                                    ReportBuilder.build(d.session, d.snapshot, d.results),
                                )
                            },
                            enabled = aiState !is ReportsViewModel.AiSummaryState.Loading,
                            modifier = Modifier.padding(top = 8.dp),
                        ) { Text("Summarize with AI") }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                clipboard.setClipEntry(
                                    ClipEntry(
                                        ClipData.newPlainText(
                                            "DroidKit summary",
                                            ReportBuilder.buildSummary(d.session, d.results),
                                        ),
                                    ),
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text("Copy summary") }
                    Button(
                        onClick = { showExport = true },
                        modifier = Modifier.weight(1f),
                    ) { Text("Export") }
                    OutlinedButton(
                        onClick = { showDelete = true },
                        modifier = Modifier.weight(1f),
                    ) { Text("Delete") }
                }
            }
        }
    }

    if (showExport && d != null && report != null) {
        ModalBottomSheet(onDismissRequest = { showExport = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Export preview", style = MaterialTheme.typography.titleMedium)
                Text(
                    "This report contains:",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                )
                sensitivePreview.forEach {
                    Text("• $it", style = MaterialTheme.typography.bodySmall)
                }
                var format by remember { mutableStateOf("TEXT") }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    FilterChip(
                        selected = format == "TEXT",
                        onClick = { format = "TEXT" },
                        label = { Text("Text (.txt)") },
                    )
                    FilterChip(
                        selected = format == "PDF",
                        onClick = { format = "PDF" },
                        label = { Text("PDF (.pdf)") },
                    )
                    FilterChip(
                        selected = format == "CERT",
                        onClick = { format = "CERT" },
                        label = { Text("Certificate") },
                    )
                }
                Button(
                    onClick = {
                        scope.launch {
                            val text = if (format == "CERT") {
                                ReportBuilder.buildCertificate(d.session, d.snapshot, d.results)
                            } else {
                                report
                            }
                            val file = if (format == "TEXT") {
                                ReportExporter.exportText(context, sessionId, text)
                            } else {
                                ReportExporter.exportPdf(context, sessionId, text)
                            }
                            viewModel.recordSavedReport(sessionId, format, file.absolutePath)
                            shareFile(context, file, if (format == "TEXT") "TEXT" else "PDF")
                            showExport = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 24.dp),
                ) {
                    Text("Share ${if (format == "CERT") "certificate" else "$format report"}")
                }
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete report?") },
            text = { Text("This session and its ${d?.results?.size ?: 0} results are deleted permanently.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSession(sessionId)
                        showDelete = false
                        onDeleted()
                    },
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            },
        )
    }
}

private fun shareFile(context: android.content.Context, file: File, format: String) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = if (format == "PDF") "application/pdf" else "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share report"))
}

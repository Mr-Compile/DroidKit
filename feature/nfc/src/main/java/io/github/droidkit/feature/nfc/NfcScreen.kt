package io.github.droidkit.feature.nfc

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import io.github.droidkit.core.ai.AiAssistant
import io.github.droidkit.core.ai.AiExplainButton
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.feature.nfc.NfcViewModel.Verdict

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcScreen(
    onOpenWrite: () -> Unit,
    onBack: () -> Unit,
    viewModel: NfcViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val adapterEnabled by viewModel.adapterEnabled.collectAsStateWithLifecycle()
    val scanning by viewModel.scanning.collectAsStateWithLifecycle()
    val tag by viewModel.tag.collectAsStateWithLifecycle()
    val armed by viewModel.armed.collectAsStateWithLifecycle()
    val actionResult by viewModel.actionResult.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    var confirmErase by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        viewModel.refreshAdapterState()
        onPauseOrDispose { }
    }

    DisposableEffect(scanning, armed) {
        if (activity != null && (scanning || armed != null)) viewModel.startScan(activity)
        onDispose { activity?.let { viewModel.stopScan(it) } }
    }

    Scaffold(topBar = { DroidKitTopAppBar("NFC Lab", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SectionCard(title = "Capability") {
                    InfoRow("NFC hardware", viewModel.nfcSupported.toString(), copyable = false)
                    InfoRow("Adapter present", viewModel.adapterPresent.toString(), copyable = false)
                    when {
                        !viewModel.nfcSupported || !viewModel.adapterPresent -> Text(
                            "NFC not supported on this device (NOT_SUPPORTED).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        !adapterEnabled -> Column {
                            Text(
                                "NFC is off — enable it in system settings to scan tags.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            OutlinedButton(
                                onClick = {
                                    context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                                },
                                modifier = Modifier.padding(top = 8.dp),
                            ) { Text("Open NFC settings") }
                        }
                        else -> Text(
                            "Ready — hold a tag to the back of the device.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            if (viewModel.adapterPresent && adapterEnabled) {
                item {
                    SectionCard(title = "Scan") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                if (scanning || armed != null) {
                                    viewModel.disarm()
                                    viewModel.setScanning(false)
                                } else {
                                    viewModel.setScanning(true)
                                }
                            }) { Text(if (scanning || armed != null) "Stop" else "Start scan") }
                            OutlinedButton(onClick = onOpenWrite) { Text("Write / erase") }
                            if (tag != null) {
                                OutlinedButton(onClick = { confirmErase = true }) { Text("Erase") }
                            }
                        }
                        Text(
                            when {
                                armed != null -> "Armed — present the target tag now"
                                scanning -> "Scanning…"
                                else -> "Not scanning"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }

            val t = tag
            if (t != null) {
                val nfcAiFields = buildMap<String, String?> {
                    put("Tag ID", t.idHex)
                    put("Technologies", t.techList.joinToString())
                    put("NDEF capable", t.ndefCapable.toString())
                    put("Writable", t.ndefWritable?.toString())
                    put("Capacity bytes", t.ndefMaxSize?.toString())
                    put("NDEF type", t.ndefType)
                    t.records.forEachIndexed { i, r ->
                        put("Record ${i + 1} type", r.typeHex)
                        r.decoded?.let { put("Record ${i + 1} value", it) }
                    }
                }
                item {
                    AiSummaryCard(
                        screenTitle = "NFC tag",
                        fields = nfcAiFields,
                    )
                }
                item {
                    SectionCard(title = "Tag") {
                        InfoRow("Tag ID", t.idHex)
                        InfoRow("Technologies", t.techList.joinToString())
                        InfoRow("NDEF capable", t.ndefCapable.toString(), copyable = false)
                        InfoRow("Writable", t.ndefWritable?.toString())
                        InfoRow("Capacity", t.ndefMaxSize?.let { "$it bytes" })
                        InfoRow("NDEF type", t.ndefType)
                        InfoRow("Formatable", t.formatable.toString(), copyable = false)
                        AiExplainButton(
                            assistant = AiAssistant.NFC_ASSISTANT,
                            fields = nfcAiFields,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }

                if (t.records.isNotEmpty()) {
                    item {
                        SectionCard(title = "NDEF records (${t.records.size})") {
                            t.records.forEachIndexed { i, r ->
                                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Text(
                                        "#${i + 1} · ${r.tnfName} · type ${r.typeHex}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    if (r.decoded != null) {
                                        InfoRow(r.decodedKind ?: "value", r.decoded)
                                    } else {
                                        Text(
                                            r.payloadHex,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    val checks = viewModel.doctorChecks(t)
                    SectionCard(title = "Tag Doctor — ${viewModel.overallVerdict(checks).name}") {
                        checks.forEach { c ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(c.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        c.detail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    c.verdict.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = when (c.verdict) {
                                        Verdict.PASS -> MaterialTheme.colorScheme.primary
                                        Verdict.WARNING -> MaterialTheme.colorScheme.tertiary
                                        Verdict.UNSUPPORTED -> MaterialTheme.colorScheme.error
                                    },
                                )
                            }
                        }
                        Text(
                            "Bank, transit, access-control and secure-element credentials " +
                                "cannot be cloned or read.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            } else if (viewModel.adapterPresent && adapterEnabled) {
                item { EmptyState("No tag scanned yet.") }
            }

            if (history.isNotEmpty()) {
                item {
                    SectionCard(title = "Recent scans") {
                        val df = java.text.DateFormat.getTimeInstance()
                        history.take(10).forEach { scan ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    df.format(java.util.Date(scan.scannedAtEpochMs)) +
                                        (scan.tagIdTail?.let { " · …$it" } ?: ""),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    scan.techList +
                                        (scan.recordSummary?.let { " — $it" } ?: ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                )
                            }
                        }
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("Clear history")
                        }
                    }
                }
            }

            actionResult?.let { r ->
                item {
                    SectionCard(title = "Result") {
                        Text(
                            when (r) {
                                is NfcViewModel.ActionResult.Success -> r.text
                                is NfcViewModel.ActionResult.Failed -> "Failed: ${r.text}"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (r is NfcViewModel.ActionResult.Failed) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }
        }
    }

    if (confirmErase) {
        AlertDialog(
            onDismissRequest = { confirmErase = false },
            title = { Text("Erase tag?") },
            text = {
                Text("This writes an empty NDEF message to the next presented tag, removing its current content. Present the tag after confirming.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmErase = false
                    viewModel.armErase()
                }) { Text("Erase next tag") }
            },
            dismissButton = {
                TextButton(onClick = { confirmErase = false }) { Text("Cancel") }
            },
        )
    }
}

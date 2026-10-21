package io.github.droidkit.feature.nfc

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.droidkit.core.ai.AiAssistant
import io.github.droidkit.core.ai.AiExplainButton
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.UnsupportedState
import io.github.droidkit.feature.nfc.NfcViewModel.Verdict

/** Pulsing ring shown while reader mode listens for tags. */
@Composable
private fun ScanPulse(listening: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "nfcPulse")
    val pulse by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label = "nfcPulseValue",
    )
    Box(modifier = modifier.size(88.dp), contentAlignment = Alignment.Center) {
        if (listening) {
            Box(
                Modifier
                    .size(64.dp)
                    .scale(1f + pulse * 0.5f)
                    .alpha(0.55f * (1f - pulse))
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            )
        }
        Icon(
            Icons.Outlined.Nfc,
            contentDescription = null,
            tint = if (listening) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(40.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcScreen(
    onOpenWrite: () -> Unit,
    onBack: () -> Unit,
    viewModel: NfcViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    val adapterEnabled by viewModel.adapterEnabled.collectAsStateWithLifecycle()
    val scanning by viewModel.scanning.collectAsStateWithLifecycle()
    val tag by viewModel.tag.collectAsStateWithLifecycle()
    val techSections by viewModel.techSections.collectAsStateWithLifecycle()
    val armed by viewModel.armed.collectAsStateWithLifecycle()
    val actionResult by viewModel.actionResult.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    var confirmErase by remember { mutableStateOf(false) }
    var confirmFormat by remember { mutableStateOf(false) }
    var confirmDuplicate by remember { mutableStateOf(false) }
    var rawOpen by remember { mutableStateOf<Set<Int>>(emptySet()) }

    // Reader mode runs while the screen is resumed — no manual start needed.
    LifecycleResumeEffect(activity) {
        viewModel.refreshAdapterState()
        activity?.let { viewModel.startScan(it) }
        onPauseOrDispose { activity?.let { viewModel.stopScan(it) } }
    }

    // Buzz once per detected tag.
    LaunchedEffect(Unit) {
        var last = viewModel.scanSeq.value
        viewModel.scanSeq.collect { seq ->
            if (seq != last) {
                last = seq
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    fun openUri(uri: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
        }.onFailure {
            Toast.makeText(context, "No app can open this", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(topBar = { DroidKitTopAppBar("NFC Lab", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                if (!viewModel.nfcSupported || !viewModel.adapterPresent) {
                    UnsupportedState(
                        title = "NFC not supported",
                        detail = "This device has no NFC hardware (NOT_SUPPORTED). " +
                            "Hardware: ${viewModel.nfcSupported}, adapter: ${viewModel.adapterPresent}.",
                    )
                } else {
                    SectionCard(title = "Capability") {
                        InfoRow("NFC hardware", viewModel.nfcSupported.toString(), copyable = false)
                        InfoRow("Adapter present", viewModel.adapterPresent.toString(), copyable = false)
                        if (!adapterEnabled) {
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
                    }
                }
            }

            if (viewModel.adapterPresent && adapterEnabled) {
                item {
                    SectionCard(title = "Scan") {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            ScanPulse(listening = scanning || armed != null)
                            Text(
                                when {
                                    armed != null -> "Armed — present the target tag now"
                                    scanning -> "Listening — hold a tag to the back of the device"
                                    else -> "Scanning paused"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 8.dp),
                            ) {
                                OutlinedButton(onClick = {
                                    if (scanning || armed != null) {
                                        viewModel.disarm()
                                        activity?.let { viewModel.stopScan(it) }
                                    } else {
                                        activity?.let { viewModel.startScan(it) }
                                    }
                                }) { Text(if (scanning || armed != null) "Pause" else "Resume") }
                                OutlinedButton(onClick = onOpenWrite) { Text("Write") }
                            }
                        }
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
                    techSections.forEach { s ->
                        s.fields.forEach { (k, v) -> put("${s.tech} $k", v) }
                    }
                    t.records.forEachIndexed { i, r ->
                        put("Record ${i + 1} type", r.typeHex)
                        // WiFi keys never reach AI context — decode is redacted.
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

                if (techSections.isNotEmpty()) {
                    item {
                        SectionCard(title = "Technology details") {
                            techSections.forEach { s ->
                                Text(
                                    s.tech + if (s.truncated) " (partial)" else "",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                                s.fields.forEach { (k, v) -> InfoRow(k, v) }
                                if (s.pageDumpHex.isNotEmpty()) {
                                    Text(
                                        s.pageDumpHex.joinToString("\n"),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                            }
                        }
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
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (r.decodedKind == "uri" && r.decoded != null) {
                                            TextButton(onClick = { openUri(r.decoded) }) {
                                                Text("Open")
                                            }
                                        }
                                        r.decoded?.let { value ->
                                            TextButton(onClick = {
                                                clipboard.setText(AnnotatedString(value))
                                            }) { Text("Copy") }
                                        }
                                        TextButton(onClick = {
                                            rawOpen = if (i in rawOpen) rawOpen - i else rawOpen + i
                                        }) { Text(if (i in rawOpen) "Hide raw" else "Raw") }
                                    }
                                    if (i in rawOpen) {
                                        Text(
                                            r.payloadHex,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    SectionCard(title = "Tag actions") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { confirmDuplicate = true },
                                enabled = t.rawMessage != null,
                            ) { Text("Duplicate") }
                            if (t.formatable && !t.ndefCapable) {
                                OutlinedButton(onClick = { confirmFormat = true }) {
                                    Text("Format")
                                }
                            }
                            OutlinedButton(
                                onClick = { confirmErase = true },
                                enabled = t.ndefCapable,
                            ) { Text("Erase") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val report = buildTagReport(t, viewModel.doctorChecks(t), techSections)
                            OutlinedButton(onClick = {
                                clipboard.setText(AnnotatedString(report))
                            }) { Text("Copy report") }
                            OutlinedButton(onClick = {
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, report)
                                }
                                runCatching {
                                    context.startActivity(
                                        Intent.createChooser(send, "Share tag report"),
                                    )
                                }
                            }) { Text("Share") }
                        }
                        Text(
                            "Duplicate copies NDEF content only — tag UID, locked " +
                                "regions and secure credentials are never copied.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
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
                item { EmptyState("No tag scanned yet — listening for tags.") }
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

    if (confirmFormat) {
        AlertDialog(
            onDismissRequest = { confirmFormat = false },
            title = { Text("Format tag?") },
            text = {
                Text("This formats the next presented tag to NDEF with empty content. Existing tag data will be replaced.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmFormat = false
                    viewModel.armFormat()
                }) { Text("Format next tag") }
            },
            dismissButton = {
                TextButton(onClick = { confirmFormat = false }) { Text("Cancel") }
            },
        )
    }

    if (confirmDuplicate) {
        AlertDialog(
            onDismissRequest = { confirmDuplicate = false },
            title = { Text("Duplicate tag content?") },
            text = {
                Text("This writes a copy of the scanned tag's NDEF records to the next tag you present. Only record content is copied — the new tag keeps its own UID.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDuplicate = false
                    if (!viewModel.armDuplicate()) {
                        Toast.makeText(
                            context, "No NDEF message to copy", Toast.LENGTH_SHORT,
                        ).show()
                    }
                }) { Text("Copy to next tag") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDuplicate = false }) { Text("Cancel") }
            },
        )
    }
}

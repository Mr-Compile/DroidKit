package io.github.droidkit.feature.nfc

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard

private enum class RecordKind { TEXT, URI }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcWriteScreen(
    viewModel: NfcViewModel = hiltViewModel(),
) {
    val activity = LocalContext.current as? Activity
    val armed by viewModel.armed.collectAsStateWithLifecycle()
    val actionResult by viewModel.actionResult.collectAsStateWithLifecycle()
    var kind by remember { mutableStateOf(RecordKind.TEXT) }
    var input by remember { mutableStateOf("") }
    var confirmWrite by remember { mutableStateOf(false) }

    DisposableEffect(armed) {
        if (activity != null && armed != null) viewModel.startScan(activity)
        onDispose { activity?.let { viewModel.stopScan(it) } }
    }

    val message = remember(input, kind) {
        when (kind) {
            RecordKind.TEXT -> NfcTagParser.messageOf(NfcTagParser.textRecord(input))
            RecordKind.URI -> NfcTagParser.messageOf(NfcTagParser.uriRecord(input))
        }
    }
    val sizeBytes = message.toByteArray().size

    Scaffold(topBar = { TopAppBar(title = { Text("NFC Write") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SectionCard(title = "Record") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RecordKind.entries.forEach { k ->
                            FilterChip(
                                selected = kind == k,
                                onClick = { kind = k },
                                label = { Text(if (k == RecordKind.TEXT) "Text" else "URI") },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        label = { Text(if (kind == RecordKind.TEXT) "Text content" else "URI (e.g. https://example.com)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                    Text(
                        "Presets",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                    ) {
                        if (kind == RecordKind.URI) {
                            listOf(
                                "Web" to "https://",
                                "Call" to "tel:",
                                "Email" to "mailto:",
                                "SMS" to "sms:",
                                "Map" to "geo:",
                            ).forEach { (label, prefix) ->
                                FilterChip(
                                    selected = false,
                                    onClick = { if (!input.startsWith(prefix)) input = prefix },
                                    label = { Text(label) },
                                )
                            }
                        } else {
                            listOf(
                                "Asset tag" to "ASSET-",
                                "Inspection" to "INSPECTED-BY: ",
                                "Serial" to "SN: ",
                            ).forEach { (label, template) ->
                                FilterChip(
                                    selected = false,
                                    onClick = { if (input.isBlank()) input = template },
                                    label = { Text(label) },
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Preview") {
                    InfoRow("Record type", if (kind == RecordKind.TEXT) "RTD_TEXT" else "RTD_URI", copyable = false)
                    InfoRow("Message size", "$sizeBytes bytes", copyable = false)
                    Text(
                        "The write replaces all NDEF content on the presented tag.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedButton(
                        onClick = { confirmWrite = true },
                        enabled = input.isNotBlank() && armed == null,
                        modifier = Modifier.padding(top = 8.dp),
                    ) { Text("Write to tag") }
                }
            }

            if (armed != null) {
                item {
                    SectionCard(title = "Armed") {
                        Text(
                            "Present the target tag to the back of the device now.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        OutlinedButton(
                            onClick = { viewModel.disarm() },
                            modifier = Modifier.padding(top = 8.dp),
                        ) { Text("Cancel") }
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

    if (confirmWrite) {
        AlertDialog(
            onDismissRequest = { confirmWrite = false },
            title = { Text("Write to tag?") },
            text = {
                Text(
                    "This writes a $sizeBytes-byte NDEF message to the next presented tag, " +
                        "replacing its current content. The tag must be writable and have " +
                        "enough capacity."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmWrite = false
                    viewModel.armWrite(message, sizeBytes)
                }) { Text("Arm write") }
            },
            dismissButton = {
                TextButton(onClick = { confirmWrite = false }) { Text("Cancel") }
            },
        )
    }
}

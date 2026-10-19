package io.github.droidkit.feature.adb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdbScreen(
    viewModel: AdbViewModel = hiltViewModel(),
) {
    val session by viewModel.session.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val events by viewModel.events.collectAsState()
    val props by viewModel.props.collectAsState()
    val log by viewModel.log.collectAsState()

    val pairingHost by viewModel.pairingHost.collectAsState()
    val pairingPort by viewModel.pairingPort.collectAsState()
    val pairingCode by viewModel.pairingCode.collectAsState()
    val connectHost by viewModel.connectHost.collectAsState()
    val connectPort by viewModel.connectPort.collectAsState()
    val logLines by viewModel.logLines.collectAsState()
    val useTls by viewModel.useTls.collectAsState()

    var confirmReboot by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(events.size) {
        events.firstOrNull()?.let {
            when (it) {
                is AdbViewModel.Event.Message -> snackbar.showSnackbar(it.text)
            }
            viewModel.consumeEvent(it)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Wireless ADB") }) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SectionCard(title = "Session") {
                    if (session == null) {
                        Text(
                            "Not connected. Enable Wireless debugging in Developer " +
                                "options — it shows an IP plus a pairing port and a " +
                                "connect port.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        InfoRow("Connected", session!!, copyable = true)
                        OutlinedButton(
                            onClick = { viewModel.disconnect() },
                            enabled = !busy,
                            modifier = Modifier.padding(top = 8.dp),
                        ) { Text("Disconnect") }
                    }
                }
            }

            item {
                SectionCard(title = "1 — Pair") {
                    Text(
                        "Wireless debugging → 'Pair device with pairing code'. " +
                            "Enter the address, pairing port and 6-digit code.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = pairingHost,
                        onValueChange = { viewModel.pairingHost.value = it },
                        label = { Text("Host / IP") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pairingPort,
                            onValueChange = { viewModel.pairingPort.value = it },
                            label = { Text("Pairing port") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = pairingCode,
                            onValueChange = { viewModel.pairingCode.value = it },
                            label = { Text("Pairing code") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.pair() },
                        enabled = !busy,
                    ) { Text(if (busy) "Working…" else "Pair") }
                }
            }

            item {
                SectionCard(title = "2 — Connect") {
                    Text(
                        "The connect port is the main port shown on the Wireless " +
                            "debugging screen (different from the pairing port).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = connectHost,
                        onValueChange = { viewModel.connectHost.value = it },
                        label = { Text("Host / IP (same as pairing if blank)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = connectPort,
                            onValueChange = { viewModel.connectPort.value = it },
                            label = { Text("Connect port") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            modifier = Modifier.selectable(
                                selected = useTls,
                                onClick = { viewModel.useTls.value = !useTls },
                            ),
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = useTls,
                                onCheckedChange = { viewModel.useTls.value = it },
                            )
                            Text("TLS")
                        }
                    }
                    OutlinedButton(
                        onClick = { viewModel.connect() },
                        enabled = !busy,
                    ) { Text(if (busy) "Working…" else "Connect") }
                }
            }

            if (session != null) {
                item {
                    SectionCard(title = "Device properties") {
                        OutlinedButton(
                            onClick = { viewModel.loadDeviceProps() },
                            enabled = !busy,
                        ) { Text("Read device info") }
                        props.forEach { (label, value) ->
                            InfoRow(label, value, copyable = true)
                        }
                    }
                }

                item {
                    SectionCard(title = "Logcat") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = logLines,
                                onValueChange = { viewModel.logLines.value = it },
                                label = { Text("Lines") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            OutlinedButton(
                                onClick = { viewModel.readLog() },
                                enabled = !busy,
                                modifier = Modifier.padding(top = 8.dp),
                            ) { Text("Read") }
                        }
                        if (log.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                                    .verticalScroll(rememberScrollState())
                                    .padding(top = 8.dp),
                            ) {
                                Text(
                                    log,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                )
                            }
                        }
                    }
                }

                item {
                    SectionCard(title = "Power") {
                        Text(
                            "Reboots the device immediately.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(
                            onClick = { confirmReboot = true },
                            enabled = !busy,
                        ) { Text("Reboot device") }
                    }
                }
            }
        }
    }

    if (confirmReboot) {
        AlertDialog(
            onDismissRequest = { confirmReboot = false },
            title = { Text("Reboot this device?") },
            text = { Text("The device will power off and restart immediately. Unsaved work in other apps is lost.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.reboot()
                    confirmReboot = false
                }) { Text("Reboot") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReboot = false }) { Text("Cancel") }
            },
        )
    }
}

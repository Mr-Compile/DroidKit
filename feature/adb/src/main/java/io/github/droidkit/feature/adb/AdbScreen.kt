package io.github.droidkit.feature.adb

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.droidkit.core.privileges.AdbPairingState
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.adaptive.contentWidthCap
import io.github.droidkit.core.ui.components.DroidKitTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdbScreen(
    onBack: () -> Unit,
    viewModel: AdbViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val session by viewModel.session.collectAsStateWithLifecycle()
    val pairingState by viewModel.pairingState.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val props by viewModel.props.collectAsStateWithLifecycle()
    val log by viewModel.log.collectAsStateWithLifecycle()
    val qrPayload by viewModel.qrPayload.collectAsStateWithLifecycle()

    val pairingCode by viewModel.pairingCode.collectAsStateWithLifecycle()
    val pairingHost by viewModel.pairingHost.collectAsStateWithLifecycle()
    val pairingPort by viewModel.pairingPort.collectAsStateWithLifecycle()
    val connectHost by viewModel.connectHost.collectAsStateWithLifecycle()
    val connectPort by viewModel.connectPort.collectAsStateWithLifecycle()
    val logLines by viewModel.logLines.collectAsStateWithLifecycle()
    val useTls by viewModel.useTls.collectAsStateWithLifecycle()

    var confirmReboot by remember { mutableStateOf(false) }
    var showManual by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { viewModel.startPairing() }

    val qrImagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)?.let(QrCodes::decode)
        }
        if (text == null) {
            viewModel.pairWithQrText("")
        } else {
            viewModel.pairWithQrText(text)
        }
    }

    LaunchedEffect(events.size) {
        events.firstOrNull()?.let {
            when (it) {
                is AdbViewModel.Event.Message -> snackbar.showSnackbar(it.text)
            }
            viewModel.consumeEvent(it)
        }
    }

    // Keep the session label in sync with the guided flow's terminal state.
    LaunchedEffect(pairingState) {
        if (pairingState is AdbPairingState.Connected) viewModel.refreshSession()
    }

    Scaffold(
        topBar = { DroidKitTopAppBar("Wireless ADB", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).contentWidthCap(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SectionCard(title = "Session") {
                    if (session == null) {
                        Text(
                            "Not connected. Enable Wireless debugging in Developer " +
                                "options, then pair below — no IP or port typing needed.",
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
                SectionCard(title = "1 — Pair with code") {
                    Text(
                        "Open Developer options → Wireless debugging → " +
                            "“Pair device with pairing code”. The pairing port is " +
                            "found automatically — you only enter the code.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GuidedStateText(pairingState)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                viewModel.startPairing()
                                if (Build.VERSION.SDK_INT >= 33) {
                                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            enabled = !busy && pairingState !is AdbPairingState.SearchingPairing,
                        ) {
                            Text(
                                when (pairingState) {
                                    is AdbPairingState.SearchingPairing -> "Searching…"
                                    else -> "Start pairing"
                                },
                            )
                        }
                        OutlinedButton(onClick = { openDevOptions(context) }) {
                            Text("Open Developer options")
                        }
                        if (pairingState !is AdbPairingState.Idle &&
                            pairingState !is AdbPairingState.Connected
                        ) {
                            TextButton(onClick = { viewModel.stopPairing() }) { Text("Stop") }
                        }
                    }
                    OutlinedTextField(
                        value = pairingCode,
                        onValueChange = { viewModel.pairingCode.value = it },
                        label = { Text("Pairing code (6 digits)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Button(
                        onClick = { viewModel.submitPairingCode() },
                        enabled = !busy && pairingState is AdbPairingState.EndpointFound,
                    ) { Text("Pair") }
                    Text(
                        "You can also reply with the code straight in the pairing " +
                            "notification without leaving Developer options.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                SectionCard(title = "2 — Connect") {
                    Text(
                        "After pairing, the connect service is found automatically " +
                            "(the connect port differs from the pairing port).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedButton(
                        onClick = { viewModel.autoConnect() },
                        enabled = !busy && session == null,
                    ) {
                        Text(
                            when (pairingState) {
                                is AdbPairingState.SearchingConnect -> "Finding device…"
                                is AdbPairingState.Connecting -> "Connecting…"
                                else -> "Connect via auto-discovery"
                            },
                        )
                    }
                }
            }

            item {
                SectionCard(title = "QR pairing") {
                    Text(
                        "Show a QR for another device to scan, or import a " +
                            "screenshot of a pairing QR shown by a device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.showQrForOtherDevice() },
                            enabled = !busy,
                        ) { Text("Show QR") }
                        OutlinedButton(
                            onClick = {
                                qrImagePicker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    ),
                                )
                            },
                            enabled = !busy,
                        ) { Text("Import screenshot") }
                    }
                    qrPayload?.let { payload ->
                        Image(
                            bitmap = QrCodes.render(payload.encode()).asImageBitmap(),
                            contentDescription = "ADB pairing QR",
                            modifier = Modifier
                                .size(220.dp)
                                .padding(top = 8.dp),
                        )
                        Text(
                            "On the other device: Wireless debugging → " +
                                "“Pair device with QR code” → scan this.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                SectionCard(title = "Advanced — manual endpoint") {
                    TextButton(onClick = { showManual = !showManual }) {
                        Text(if (showManual) "Hide manual fields" else "Show manual fields")
                    }
                    if (showManual) {
                        Text(
                            "Fallback when mDNS discovery is blocked (guest Wi-Fi, " +
                                "some OEMs). Pairing port and connect port differ.",
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
                                value = connectPort,
                                onValueChange = { viewModel.connectPort.value = it },
                                label = { Text("Connect port") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                        }
                        OutlinedTextField(
                            value = connectHost,
                            onValueChange = { viewModel.connectHost.value = it },
                            label = { Text("Connect host (blank = pairing host)") },
                            modifier = Modifier.fillMaxWidth(),
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.pair() },
                                enabled = !busy,
                            ) { Text("Pair manually") }
                            OutlinedButton(
                                onClick = { viewModel.connect() },
                                enabled = !busy,
                            ) { Text("Connect manually") }
                        }
                    }
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

@Composable
private fun GuidedStateText(state: AdbPairingState) {
    val (text, color) = when (state) {
        is AdbPairingState.Idle -> null
        is AdbPairingState.SearchingPairing ->
            "Searching for the pairing service…" to MaterialTheme.colorScheme.primary
        is AdbPairingState.EndpointFound ->
            "Found pairing port ${state.endpoint.port} — enter the code." to
                MaterialTheme.colorScheme.primary
        is AdbPairingState.Pairing ->
            "Pairing…" to MaterialTheme.colorScheme.primary
        is AdbPairingState.Paired ->
            "Paired ✓ — finding the connect port…" to MaterialTheme.colorScheme.primary
        is AdbPairingState.SearchingConnect ->
            "Finding the connect service…" to MaterialTheme.colorScheme.primary
        is AdbPairingState.Connecting ->
            "Connecting…" to MaterialTheme.colorScheme.primary
        is AdbPairingState.Connected ->
            "Connected ✓" to MaterialTheme.colorScheme.primary
        is AdbPairingState.Failed ->
            state.message to MaterialTheme.colorScheme.error
    } ?: return
    Text(text, style = MaterialTheme.typography.bodyMedium, color = color)
}

private fun openDevOptions(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        .putExtra(":settings:fragment_args_key", "toggle_adb_wireless")
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

package io.github.droidkit.feature.bluetooth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GattExplorerScreen(
    onBack: () -> Unit,
    viewModel: GattExplorerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var writeTarget by remember { mutableStateOf<Pair<String, String>?>(null) }
    var writeHex by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose { viewModel.disconnect() }
    }

    Scaffold(topBar = { DroidKitTopAppBar("GATT explorer", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    "Device: ${viewModel.address} — reads and subscriptions are " +
                        "explicit per characteristic. Writes require confirmation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            when (val s = state) {
                is GattExplorerViewModel.State.Connecting -> item {
                    EmptyState("Connecting…")
                    OutlinedButton(onClick = { viewModel.connect() }) { Text("Connect") }
                }
                is GattExplorerViewModel.State.Failed -> item {
                    SectionCard(title = "Connection") {
                        Text(
                            s.reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        OutlinedButton(
                            onClick = { viewModel.connect() },
                            modifier = Modifier.padding(top = 8.dp),
                        ) { Text("Retry") }
                    }
                }
                is GattExplorerViewModel.State.Connected -> {
                    item {
                        Text(
                            s.deviceName ?: "Connected device",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                    item {
                        AiSummaryCard(
                            screenTitle = "GATT services",
                            fields = buildMap {
                                put("Services discovered", s.services.size.toString())
                                s.services.take(20).forEach { svc ->
                                    put(
                                        "Service ${svc.uuid.take(8)}",
                                        (if (svc.isPrimary) "primary" else "secondary") +
                                            "; characteristics " +
                                            svc.characteristics.joinToString { c ->
                                                "${c.uuid.take(8)}(${c.properties.joinToString("|")})"
                                            },
                                    )
                                }
                            },
                        )
                    }
                    if (s.services.isEmpty()) {
                        item { EmptyState("No GATT services discovered.") }
                    }
                    items(s.services, key = { it.uuid }) { svc ->
                        SectionCard(
                            title = (if (svc.isPrimary) "Service " else "Secondary service ") +
                                svc.uuid.take(8),
                        ) {
                            Text(
                                svc.uuid,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            svc.characteristics.forEach { c ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        c.uuid,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        c.properties.joinToString(", ").ifEmpty { "no properties" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    c.readValueHex?.let {
                                        Text(
                                            "value: $it",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                    c.notifyValueHex?.let {
                                        Text(
                                            "notify: $it",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                    c.readError?.let {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        if ("read" in c.properties && c.readValueHex == null && c.readError == null) {
                                            TextButton(onClick = {
                                                viewModel.readCharacteristic(svc.uuid, c.uuid)
                                            }) { Text("Read") }
                                        }
                                        if ("notify" in c.properties || "indicate" in c.properties) {
                                            TextButton(onClick = {
                                                viewModel.toggleSubscription(svc.uuid, c.uuid)
                                            }) { Text(if (c.subscribed) "Unsubscribe" else "Subscribe") }
                                        }
                                        if ("write" in c.properties || "write-no-rsp" in c.properties) {
                                            TextButton(onClick = {
                                                writeTarget = svc.uuid to c.uuid
                                                writeHex = ""
                                            }) { Text("Write") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    writeTarget?.let { (svcUuid, charUuid) ->
        val bytes = writeHex.toByteArrayOrNull()
        AlertDialog(
            onDismissRequest = { writeTarget = null },
            title = { Text("Write characteristic?") },
            text = {
                Column {
                    Text(
                        "This sends raw bytes to $charUuid on the remote " +
                            "device and can change its behavior. Enter payload " +
                            "as hex (e.g. 01 A3 FF).",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    OutlinedTextField(
                        value = writeHex,
                        onValueChange = { writeHex = it },
                        label = { Text("Payload hex") },
                        singleLine = true,
                        isError = writeHex.isNotBlank() && bytes == null,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val b = bytes ?: return@TextButton
                        viewModel.writeCharacteristic(svcUuid, charUuid, b)
                        writeTarget = null
                    },
                    enabled = bytes != null && bytes.isNotEmpty(),
                ) { Text("Write") }
            },
            dismissButton = {
                TextButton(onClick = { writeTarget = null }) { Text("Cancel") }
            },
        )
    }
}

private fun String.toByteArrayOrNull(): ByteArray? {
    val clean = filter { !it.isWhitespace() }
    if (clean.isEmpty()) return ByteArray(0)
    if (clean.length % 2 != 0 ||
        clean.any { it !in '0'..'9' && it !in 'a'..'f' && it !in 'A'..'F' }
    ) {
        return null
    }
    return ByteArray(clean.length / 2) { i ->
        ((Character.digit(clean[i * 2], 16) shl 4) or
            Character.digit(clean[i * 2 + 1], 16)).toByte()
    }
}

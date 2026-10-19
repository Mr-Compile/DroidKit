package io.github.droidkit.feature.bluetooth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GattExplorerScreen(
    viewModel: GattExplorerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    DisposableEffect(Unit) {
        onDispose { viewModel.disconnect() }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("GATT explorer") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    "Device: ${viewModel.address} — read-only inspection. DroidKit " +
                        "never writes to or subscribes to characteristics.",
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
                                    c.readError?.let {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                    if ("read" in c.properties && c.readValueHex == null && c.readError == null) {
                                        TextButton(onClick = {
                                            viewModel.readCharacteristic(svc.uuid, c.uuid)
                                        }) { Text("Read value") }
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

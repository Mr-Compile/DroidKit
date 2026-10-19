package io.github.droidkit.feature.network

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.feature.network.NetworkViewModel.LayerResult
import io.github.droidkit.feature.network.NetworkViewModel.LayerState
import io.github.droidkit.feature.network.NetworkViewModel.ToolState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    viewModel: NetworkViewModel = hiltViewModel(),
) {
    val snapshot by viewModel.snapshot.collectAsState()
    val layers by viewModel.layers.collectAsState()
    val ping by viewModel.ping.collectAsState()
    val dns by viewModel.dns.collectAsState()
    var pingHost by remember { mutableStateOf("") }
    var dnsHost by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Network Lab") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SectionCard(title = "Active connection") {
                    val s = snapshot
                    if (s == null) {
                        Text("Reading…", style = MaterialTheme.typography.bodyMedium)
                    } else if (!s.available) {
                        Text(
                            "No active network.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        InfoRow("Transports", s.transports.joinToString().ifBlank { "None" }, copyable = false)
                        InfoRow("Internet capable", s.internetCapable?.toString(), copyable = false)
                        InfoRow("Validated", s.validated?.toString(), copyable = false)
                        InfoRow("Metered", s.metered?.toString(), copyable = false)
                        InfoRow("Downstream", s.downKbps?.let { "$it kbps" }, copyable = false)
                        InfoRow("Upstream", s.upKbps?.let { "$it kbps" }, copyable = false)
                        InfoRow("Interface", s.interfaceName)
                        InfoRow("Local addresses", s.addresses.joinToString().ifBlank { null })
                        InfoRow("DNS servers", s.dnsServers.joinToString().ifBlank { null })
                        InfoRow("Routes", s.routes.toString(), copyable = false)
                        InfoRow("MTU", s.mtu?.toString(), copyable = false)
                        InfoRow("Private DNS", s.privateDns, copyable = false)
                        InfoRow("Captive portal", s.captivePortal?.toString(), copyable = false)
                        Text(
                            "SSID/BSSID/MAC require location permission and are not read here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }

            item {
                SectionCard(title = "Diagnostics") {
                    layers.forEach { LayerRow(it) }
                    OutlinedButton(
                        onClick = { viewModel.runDiagnostics() },
                        modifier = Modifier.padding(top = 8.dp),
                    ) { Text("Re-run") }
                }
            }

            item {
                SectionCard(title = "Ping") {
                    HostInput(pingHost, { pingHost = it }, "Host or IP") {
                        viewModel.ping(pingHost)
                    }
                    when (val p = ping) {
                        is ToolState.Running -> Text("Probing…", modifier = Modifier.padding(top = 8.dp))
                        is ToolState.Failed -> Text(
                            "Failed: ${p.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        is ToolState.Done -> Column(modifier = Modifier.padding(top = 8.dp)) {
                            InfoRow("Resolved", p.result.resolvedAddress)
                            InfoRow("Replies", "${p.result.received}/${p.result.transmitted} (${p.result.lossPercent}% loss)", copyable = false)
                            InfoRow("Min / Avg / Max", "${NetTools.formatMs(p.result.minMs)} / ${NetTools.formatMs(p.result.avgMs)} / ${NetTools.formatMs(p.result.maxMs)}", copyable = false)
                            Text(
                                "ICMP echo is often filtered — no reply does not prove the host is down.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        else -> {}
                    }
                }
            }

            item {
                SectionCard(title = "DNS lookup") {
                    HostInput(dnsHost, { dnsHost = it }, "Hostname") {
                        viewModel.lookup(dnsHost)
                    }
                    when (val d = dns) {
                        is ToolState.Running -> Text("Resolving…", modifier = Modifier.padding(top = 8.dp))
                        is ToolState.Failed -> Text(
                            "Failed: ${d.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        is ToolState.Done -> Column(modifier = Modifier.padding(top = 8.dp)) {
                            d.result.addresses.forEach { InfoRow("Address", it) }
                            InfoRow("Elapsed", "${d.result.elapsedMs} ms", copyable = false)
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun LayerRow(layer: LayerResult) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(layer.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                layer.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            layer.state.name.replace('_', ' '),
            style = MaterialTheme.typography.labelMedium,
            color = when (layer.state) {
                LayerState.PASS -> MaterialTheme.colorScheme.primary
                LayerState.FAIL -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun HostInput(
    value: String,
    onValue: (String) -> Unit,
    label: String,
    onRun: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedButton(
        onClick = onRun,
        enabled = value.isNotBlank(),
        modifier = Modifier.padding(top = 8.dp),
    ) { Text("Run") }
}

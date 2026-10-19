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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ai.AiAssistant
import io.github.droidkit.core.ai.AiExplainButton
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
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    val layers by viewModel.layers.collectAsStateWithLifecycle()
    val ping by viewModel.ping.collectAsStateWithLifecycle()
    val dns by viewModel.dns.collectAsStateWithLifecycle()
    val ports by viewModel.ports.collectAsStateWithLifecycle()
    var pingHost by remember { mutableStateOf("") }
    var dnsHost by remember { mutableStateOf("") }
    var portHost by remember { mutableStateOf("") }
    var portList by remember { mutableStateOf("80, 443") }

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

            item {
                SectionCard(title = "Port reachability") {
                    OutlinedTextField(
                        value = portHost,
                        onValueChange = { portHost = it },
                        label = { Text("Host or IP") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = portList,
                        onValueChange = { portList = it },
                        label = { Text("Ports (comma or space separated)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                    OutlinedButton(
                        onClick = { viewModel.probePorts(portHost, portList) },
                        enabled = portHost.isNotBlank() && portList.isNotBlank(),
                        modifier = Modifier.padding(top = 8.dp),
                    ) { Text("Probe") }
                    when (val p = ports) {
                        is ToolState.Running -> Text("Probing…", modifier = Modifier.padding(top = 8.dp))
                        is ToolState.Failed -> Text(
                            "Failed: ${p.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        is ToolState.Done -> Column(modifier = Modifier.padding(top = 8.dp)) {
                            p.result.forEach { r ->
                                InfoRow(
                                    "Port ${r.port}",
                                    (if (r.open) "open" else "closed/filtered") +
                                        " · ${NetTools.formatMs(r.elapsedMs)}",
                                    copyable = false,
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }

            item {
                SectionCard(title = "Interfaces") {
                    val interfaces = remember { viewModel.interfaces() }
                    if (interfaces.isEmpty()) {
                        Text("No interfaces exposed.", style = MaterialTheme.typography.bodyMedium)
                    }
                    interfaces.forEach { ni ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(
                                ni.name + if (ni.displayName != ni.name) " — ${ni.displayName}" else "",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                buildList {
                                    add(if (ni.up) "up" else "down")
                                    if (ni.loopback) add("loopback")
                                    if (ni.multicast) add("multicast")
                                    ni.mtu?.let { add("mtu $it") }
                                }.joinToString(" · "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            ni.addresses.forEach {
                                Text(
                                    "  $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            item {
                val nsd by viewModel.nsdState.collectAsStateWithLifecycle()
                var serviceType by remember { mutableStateOf("_ipp._tcp") }
                var resolved by remember { mutableStateOf<NetworkViewModel.NsdFound?>(null) }
                SectionCard(title = "Local service discovery") {
                    Text(
                        "Bounded 10s mDNS/NSD browse of one service type on the local " +
                            "network — user-initiated only, results stay on device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = serviceType,
                            onValueChange = { serviceType = it },
                            label = { Text("Service type") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedButton(
                            onClick = { viewModel.discoverServices(serviceType) },
                        ) { Text("Discover") }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        listOf("_ipp._tcp", "_http._tcp", "_googlecast._tcp", "_airplay._tcp", "_ssh._tcp")
                            .forEach { t ->
                                TextButton(onClick = { serviceType = t }) {
                                    Text(t, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                    }
                    when (val n = nsd) {
                        is ToolState.Running -> Text(
                            "Discovering… (10s window)",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        is ToolState.Failed -> Text(
                            n.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        is ToolState.Done -> {
                            if (n.result.isEmpty()) {
                                Text(
                                    "No services of this type found.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                            n.result.forEach { svc ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            svc.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f),
                                        )
                                        TextButton(onClick = {
                                            viewModel.resolveService(svc.name) { resolved = it }
                                        }) { Text("Resolve") }
                                    }
                                    resolved?.takeIf { it.name == svc.name }?.let { r ->
                                        Text(
                                            "host ${r.host ?: "—"} · port ${r.port ?: "—"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }

            item {
                AiExplainButton(
                    assistant = AiAssistant.NETWORK_TROUBLESHOOTER,
                    fields = buildMap {
                        val s = snapshot
                        put("Network available", s?.available?.toString())
                        put("Internet capable", s?.internetCapable?.toString())
                        put("Validated", s?.validated?.toString())
                        put("Transports", s?.transports?.joinToString())
                        put("Interface", s?.interfaceName)
                        put("DNS servers", s?.dnsServers?.joinToString())
                        layers.forEach { put("Layer ${it.name}", "${it.state}: ${it.detail}") }
                        (ping as? ToolState.Done)?.let {
                            put("Ping ${it.result.host}", "${it.result.lossPercent}% loss, avg ${it.result.avgMs} ms")
                        }
                        (dns as? ToolState.Done)?.let {
                            put("DNS ${it.result.host}", it.result.addresses.joinToString())
                        }
                        (ports as? ToolState.Done)?.let { done ->
                            put(
                                "Ports ${done.result.firstOrNull()?.host}",
                                done.result.joinToString { "${it.port}:${if (it.open) "open" else "closed"}" },
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
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

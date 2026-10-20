package io.github.droidkit.feature.usb

import android.content.ClipData
import android.content.Intent
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ai.AiAssistant
import io.github.droidkit.core.ai.AiExplainButton
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsbDetailScreen(
    deviceName: String,
    viewModel: UsbViewModel,
    onBack: () -> Unit,
) {
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val permissionVersion by viewModel.permissionVersion.collectAsStateWithLifecycle()
    val permissionPending by viewModel.permissionPending.collectAsStateWithLifecycle()
    val device = devices.firstOrNull { it.deviceName == deviceName }
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { DroidKitTopAppBar(device?.productName ?: "USB device", onBack = onBack) },
    ) { padding ->
        if (device == null) {
            EmptyState("Device disconnected.")
            return@Scaffold
        }
        // permissionVersion forces recomposition after a permission response.
        val hasPermission = permissionVersion.let { viewModel.hasPermission(device) }
        val serial = permissionVersion.let { viewModel.serialOrNull(device) }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SectionCard(title = "Identity") {
                    InfoRow("Device path", device.deviceName, copyable = false)
                    InfoRow("Vendor ID", UsbDescriptors.hex4(device.vendorId))
                    InfoRow("Product ID", UsbDescriptors.hex4(device.productId))
                    InfoRow("USB version", device.version)
                    InfoRow("Manufacturer", device.manufacturerName)
                    InfoRow("Product", device.productName)
                    if (hasPermission) {
                        InfoRow("Serial", serial)
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Serial", modifier = Modifier.weight(1f))
                            if (deviceName in permissionPending) {
                                Text("Requesting…", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                TextButton(onClick = { viewModel.requestPermission(device) }) {
                                    Text("Request USB permission")
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Classification") {
                    InfoRow("Class", "${UsbDescriptors.className(device.deviceClass)} (${UsbDescriptors.hex2(device.deviceClass)})")
                    InfoRow("Subclass", UsbDescriptors.hex2(device.deviceSubclass))
                    InfoRow("Protocol", UsbDescriptors.hex2(device.deviceProtocol))
                }
            }

            item {
                SectionCard(title = "Interfaces") {
                    if (device.interfaceCount == 0) {
                        Text(
                            "No interfaces exposed${if (!hasPermission) " — USB permission may be required" else ""}.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    for (i in 0 until device.interfaceCount) {
                        val iface = device.getInterface(i)
                        Text(
                            "Interface ${iface.id} — " +
                                "${UsbDescriptors.className(iface.interfaceClass)} " +
                                "(${UsbDescriptors.hex2(iface.interfaceClass)}/" +
                                "${UsbDescriptors.hex2(iface.interfaceSubclass)}/" +
                                "${UsbDescriptors.hex2(iface.interfaceProtocol)})",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Text(
                            UsbDescriptors.interfaceInterpretation(
                                iface.interfaceClass,
                                iface.interfaceSubclass,
                                iface.interfaceProtocol,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            iface.name ?: "No interface name",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        for (e in 0 until iface.endpointCount) {
                            val ep = iface.getEndpoint(e)
                            Text(
                                "  ep ${UsbDescriptors.hex2(ep.address)} — " +
                                    "${UsbDescriptors.directionName(ep.direction)}, " +
                                    "${UsbDescriptors.endpointTypeName(ep.type)}, " +
                                    "max packet ${ep.maxPacketSize}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                            )
                        }
                    }
                }
            }

            if (device.interfaceCount > 0) {
                item {
                    SectionCard(title = "Class inspector") {
                        Text(
                            UsbDescriptors.deviceInterpretation(
                                (0 until device.interfaceCount).map {
                                    val i = device.getInterface(it)
                                    Triple(i.interfaceClass, i.interfaceSubclass, i.interfaceProtocol)
                                },
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                SectionCard(title = "Configurations") {
                    if (device.configurationCount == 0) {
                        Text(
                            "No configurations exposed${if (!hasPermission) " — USB permission may be required" else ""}.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    for (i in 0 until device.configurationCount) {
                        val cfg = device.getConfiguration(i)
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                "Configuration ${cfg.id}${cfg.name?.let { " — $it" } ?: ""}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                "self-powered=${cfg.isSelfPowered}, remote-wakeup=${cfg.isRemoteWakeup}, maxPower=${cfg.maxPower} mA",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Status & limits") {
                    InfoRow(
                        "USB permission",
                        if (hasPermission) "granted" else "not granted",
                        copyable = false,
                    )
                    Text(
                        "Android USB APIs cannot expose flash-controller model, NAND Flash ID, " +
                            "bad blocks, or firmware. Tools claiming ChipGenius-style internals " +
                            "on Android are not possible through these APIs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(onClick = {
                            scope.launch {
                                clipboard.setClipEntry(
                                    ClipEntry(ClipData.newPlainText("USB summary", device.summaryText(serial))),
                                )
                            }
                        }) { Text("Copy summary") }
                        OutlinedButton(onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, device.summaryText(serial))
                            }
                            context.startActivity(Intent.createChooser(send, "Share USB summary"))
                        }) { Text("Share") }
                        AiExplainButton(
                            assistant = AiAssistant.USB_ASSISTANT,
                            fields = buildMap {
                                put("Device path", device.deviceName)
                                put("Vendor ID", UsbDescriptors.hex4(device.vendorId))
                                put("Product ID", UsbDescriptors.hex4(device.productId))
                                put("USB version", device.version)
                                put("Manufacturer", device.manufacturerName)
                                put("Product", device.productName)
                                put("Class", UsbDescriptors.className(device.deviceClass))
                                put("Subclass", UsbDescriptors.hex2(device.deviceSubclass))
                                put("Protocol", UsbDescriptors.hex2(device.deviceProtocol))
                                put("Interfaces", device.interfaceCount.toString())
                                put("USB permission", hasPermission.toString())
                            },
                        )
                    }
                }
            }
        }
    }
}

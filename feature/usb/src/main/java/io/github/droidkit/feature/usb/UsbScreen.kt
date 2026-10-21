package io.github.droidkit.feature.usb

import android.hardware.usb.UsbDevice
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.adaptive.DroidKitListDetail
import io.github.droidkit.core.ui.adaptive.rememberDroidKitWindowInfo
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsbScreen(
    onOpenDetail: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: UsbViewModel = hiltViewModel(),
) {
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val windowInfo = rememberDroidKitWindowInfo()

    Scaffold(topBar = { DroidKitTopAppBar("USB Lab", onBack = onBack) }) { padding ->
        if (windowInfo.isExpandedWidth) {
            // Two-pane at expanded: detail route stays navigable for deep
            // links, but device selection opens it inside the pane.
            DroidKitListDetail<String>(
                modifier = Modifier.fillMaxSize().padding(padding),
                listPane = { onSelect ->
                    UsbDeviceList(
                        devices = devices,
                        hostSupported = viewModel.hostSupported,
                        events = events,
                        onSelect = onSelect,
                    )
                },
                detailPane = { name, back ->
                    UsbDetailScreen(
                        deviceName = name,
                        viewModel = viewModel,
                        onBack = back,
                        embedded = true,
                    )
                },
            )
        } else {
            UsbDeviceList(
                devices = devices,
                hostSupported = viewModel.hostSupported,
                events = events,
                onSelect = onOpenDetail,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun UsbDeviceList(
    devices: List<UsbDevice>,
    hostSupported: Boolean,
    events: List<UsbViewModel.UsbEvent>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            SectionCard(title = "Capability") {
                if (hostSupported) {
                    Text(
                        "USB Host mode supported — attach a device to inspect it.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Text(
                        "USB Host not supported on this device (NOT_SUPPORTED).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        if (events.isNotEmpty()) {
            item {
                SectionCard(title = "Attach / detach log") {
                    Text(
                        "Session events while this screen was open — not persisted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    events.asReversed().take(20).forEach { e ->
                        Text(
                            (if (e.attached) "▲ attached  " else "▽ detached  ") +
                                (e.productName ?: e.deviceName) + "  ·  " + e.vidPid +
                                "  ·  " + android.text.format.DateFormat.format(
                                    "HH:mm:ss", e.atEpochMs,
                                ),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
        if (hostSupported) {
            if (devices.isEmpty()) {
                item { EmptyState("No USB devices attached.") }
            } else {
                items(devices, key = { it.deviceName }) { device ->
                    Card(
                        onClick = { onSelect(device.deviceName) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                device.productName ?: device.deviceName,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                "VID:PID ${UsbDescriptors.hex4(device.vendorId)}:" +
                                    UsbDescriptors.hex4(device.productId) +
                                    (UsbDescriptors.vendorName(device.vendorId)
                                        ?.let { " · $it" } ?: "") +
                                    " · ${UsbDescriptors.className(device.deviceClass)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

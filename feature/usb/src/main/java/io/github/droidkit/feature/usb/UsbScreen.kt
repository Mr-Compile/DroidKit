package io.github.droidkit.feature.usb

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

    Scaffold(topBar = { DroidKitTopAppBar("USB Lab", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SectionCard(title = "Capability") {
                    if (viewModel.hostSupported) {
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
            if (viewModel.hostSupported) {
                if (devices.isEmpty()) {
                    item { EmptyState("No USB devices attached.") }
                } else {
                    items(devices, key = { it.deviceName }) { device ->
                        Card(
                            onClick = { onOpenDetail(device.deviceName) },
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
}

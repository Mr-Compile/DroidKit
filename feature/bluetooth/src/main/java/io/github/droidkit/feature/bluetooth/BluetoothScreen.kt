package io.github.droidkit.feature.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(
    viewModel: BluetoothViewModel = hiltViewModel(),
) {
    val enabled by viewModel.enabled.collectAsState()
    val permissionVersion by viewModel.permissionVersion.collectAsState()
    val bonded by viewModel.bonded.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { viewModel.onPermissionResult() }
    val enableLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { viewModel.refresh() }

    val hasPermission = permissionVersion.let { viewModel.hasConnectPermission() }

    Scaffold(topBar = { TopAppBar(title = { Text("Bluetooth Lab") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SectionCard(title = "Adapter") {
                    InfoRow("Adapter present", viewModel.adapterPresent.toString(), copyable = false)
                    InfoRow("Classic Bluetooth", viewModel.classicSupported.toString(), copyable = false)
                    InfoRow("Bluetooth LE", viewModel.leSupported.toString(), copyable = false)
                    if (!viewModel.adapterPresent) {
                        Text(
                            "No Bluetooth hardware (NOT_SUPPORTED).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        InfoRow("Adapter enabled", enabled.toString(), copyable = false)
                        if (!enabled) {
                            OutlinedButton(
                                onClick = {
                                    enableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                                },
                                enabled = hasPermission,
                            ) { Text("Ask to enable Bluetooth") }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Bonded devices") {
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasPermission -> {
                            Text(
                                "BLUETOOTH_CONNECT permission required to list bonded devices.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            OutlinedButton(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                },
                                modifier = Modifier.padding(top = 8.dp),
                            ) { Text("Grant permission") }
                        }
                        bonded.isEmpty() -> Text(
                            if (enabled) "No bonded devices." else "Enable Bluetooth to read bonded devices.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        else -> Text(
                            "${bonded.size} bonded device(s) — names/addresses shown as Android permits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (hasPermission && bonded.isNotEmpty()) {
                items(bonded) { device ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(device.name ?: "Unnamed device", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${device.address ?: "Address not exposed"} · ${device.typeName} · ${device.bondStateName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(title = "BLE scan") {
                    Text(
                        "BLE scanning is a POST-V1 feature. Nearby-device scans are " +
                            "privacy-sensitive and will require explicit, time-bounded, " +
                            "user-initiated scans when shipped.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

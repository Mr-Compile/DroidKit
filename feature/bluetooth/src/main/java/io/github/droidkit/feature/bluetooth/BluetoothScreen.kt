package io.github.droidkit.feature.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import io.github.droidkit.core.ui.components.DroidKitCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import io.github.droidkit.core.ui.components.DroidKitOutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.ui.CopyAllAction
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.formatSectionsForCopy
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState

/** RSSI trend — -100..-30 dBm mapped to the sparkline height. */
@Composable
private fun RssiSparkline(samples: List<Int>, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.fillMaxWidth().height(40.dp)) {
        if (samples.size < 2) return@Canvas
        val padX = 4.dp.toPx()
        val padY = 6.dp.toPx()
        val stepX = (size.width - 2 * padX) / (samples.size - 1)
        val offsets = samples.mapIndexed { i, v ->
            Offset(
                x = padX + i * stepX,
                y = padY + (1f - (v.coerceIn(-100, -30) + 100) / 70f) * (size.height - 2 * padY),
            )
        }
        val path = Path().apply {
            moveTo(offsets.first().x, offsets.first().y)
            offsets.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, color = color, style = Stroke(2.dp.toPx()))
        drawCircle(color = color, radius = 3.dp.toPx(), center = offsets.last())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(
    onOpenDevice: (String) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: BluetoothViewModel = hiltViewModel(),
) {
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    val permissionVersion by viewModel.permissionVersion.collectAsStateWithLifecycle()
    val bonded by viewModel.bonded.collectAsStateWithLifecycle()
    val scanning by viewModel.scanning.collectAsStateWithLifecycle()
    val scanResults by viewModel.scanResults.collectAsStateWithLifecycle()
    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { viewModel.onPermissionResult() }
    val scanPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { viewModel.onPermissionResult() }
    val enableLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { viewModel.refresh() }

    val hasPermission = permissionVersion.let { viewModel.hasConnectPermission() }
    val hasScanPermission = permissionVersion.let { viewModel.hasScanPermission() }

    Scaffold(
        topBar = {
            DroidKitTopAppBar(
                "Bluetooth Lab",
                onBack = onBack,
                actions = {
                    CopyAllAction {
                        formatSectionsForCopy(
                            buildList {
                                add(
                                    "Adapter" to listOf(
                                        "Adapter present" to viewModel.adapterPresent.toString(),
                                        "Classic Bluetooth" to viewModel.classicSupported.toString(),
                                        "Bluetooth LE" to viewModel.leSupported.toString(),
                                        "Adapter enabled" to enabled.toString(),
                                    ),
                                )
                                if (hasPermission && bonded.isNotEmpty()) {
                                    add(
                                        "Bonded devices" to bonded.map { d ->
                                            (d.name ?: "Unnamed device") to
                                                "${d.address ?: "Address not exposed"} · " +
                                                "${d.typeName} · ${d.bondStateName}"
                                        },
                                    )
                                }
                                if (scanResults.isNotEmpty()) {
                                    add(
                                        "BLE scan" to scanResults.map { d ->
                                            (d.name ?: "Unnamed advertiser") to
                                                "${d.address} · ${d.rssi} dBm" +
                                                (d.decoded?.beaconHint?.let { " · $it" } ?: "")
                                        },
                                    )
                                }
                            },
                        ).takeIf { it.isNotBlank() }
                    }
                },
            )
        },
    ) { padding ->
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
                            DroidKitOutlinedButton(
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
                            DroidKitOutlinedButton(
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
                    DroidKitCard(modifier = Modifier.fillMaxWidth()) {
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
                    when {
                        !viewModel.leSupported -> Text(
                            "No Bluetooth LE hardware (NOT_SUPPORTED).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        !enabled -> Text(
                            "Enable Bluetooth to scan.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        !hasScanPermission -> {
                            Text(
                                "${viewModel.requiredScanPermission().substringAfterLast('.')} " +
                                    "permission required to scan for nearby devices.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            DroidKitOutlinedButton(
                                onClick = {
                                    scanPermissionLauncher.launch(viewModel.requiredScanPermission())
                                },
                                modifier = Modifier.padding(top = 8.dp),
                            ) { Text("Grant scan permission") }
                        }
                        else -> {
                            Text(
                                "User-initiated, ${BluetoothViewModel.SCAN_WINDOW_MS / 1000}s bounded " +
                                    "scan of nearby BLE advertisers. No data leaves the device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            DroidKitOutlinedButton(
                                onClick = { if (scanning) viewModel.stopScan() else viewModel.startScan() },
                                modifier = Modifier.padding(top = 8.dp),
                            ) { Text(if (scanning) "Stop scan" else "Start BLE scan") }
                            if (!scanning && scanResults.isEmpty()) {
                                Text(
                                    "No devices seen yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
            }

            if (scanning || scanResults.isNotEmpty()) {
                items(scanResults, key = { it.address }) { device ->
                    val isOpen = expanded[device.address] == true
                    DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                device.name ?: "Unnamed advertiser",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                "${device.address} · ${device.rssi} dBm" +
                                    (device.decoded?.beaconHint?.let { " · $it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (device.rssiHistory.size > 1) {
                                RssiSparkline(
                                    device.rssiHistory,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                            device.decoded?.fields?.forEach { (k, v) ->
                                InfoRow(k, v)
                            }
                            if (isOpen && device.rawBytes != null) {
                                Text(
                                    BleAdvertiser.hex(device.rawBytes),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (device.rawBytes != null) {
                                    TextButton(onClick = {
                                        expanded[device.address] = !isOpen
                                    }) { Text(if (isOpen) "Hide raw" else "Raw") }
                                }
                                TextButton(onClick = {
                                    if (hasPermission) {
                                        viewModel.stopScan()
                                        onOpenDevice(device.address)
                                    }
                                }) {
                                    Text(
                                        if (hasPermission) "Inspect GATT"
                                        else "GATT needs BLUETOOTH_CONNECT",
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                AiSummaryCard(
                    screenTitle = "Bluetooth lab",
                    fields = buildMap {
                        put("Adapter present", viewModel.adapterPresent.toString())
                        put("Classic Bluetooth", viewModel.classicSupported.toString())
                        put("Bluetooth LE", viewModel.leSupported.toString())
                        put("Adapter enabled", enabled.toString())
                        put("Bonded device count", bonded.size.toString())
                        bonded.take(10).forEach {
                            put("Bonded ${it.typeName}", it.bondStateName)
                        }
                        put("BLE scan running", scanning.toString())
                        put("Nearby devices seen", scanResults.size.toString())
                        // MACs/names stay local — signal strength alone is
                        // enough for a normality judgment.
                        scanResults.take(10).forEachIndexed { i, e ->
                            put("Nearby device ${i + 1}", "${e.rssi} dBm")
                        }
                    },
                )
            }
        }
    }
}

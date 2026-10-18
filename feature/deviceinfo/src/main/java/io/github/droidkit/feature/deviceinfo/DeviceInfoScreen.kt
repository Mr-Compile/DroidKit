package io.github.droidkit.feature.deviceinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreen(viewModel: DeviceInfoViewModel = hiltViewModel()) {
    val probe = viewModel.probe
    val display = probe.displayInfo
    val battery = probe.batteryCapacityPct

    fun section(title: String, lines: List<Pair<String, String>>): @Composable () -> Unit = {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                lines.forEach { (k, v) ->
                    Text("$k: $v", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    val sections = listOf(
        section("Device", listOf(
            "Manufacturer" to probe.manufacturer,
            "Model" to probe.model,
            "Device" to probe.device,
            "Product" to probe.product,
        )),
        section("Android", listOf(
            "Release" to probe.androidRelease,
            "API level" to probe.sdkInt.toString(),
        )),
        section("CPU / ABI", listOf("Supported ABIs" to probe.supportedAbis.joinToString())),
        section("Memory", listOf("Total RAM" to "${probe.totalRamMb} MB")),
        section("Storage", listOf(
            "Total" to "${probe.storage.totalBytes / (1024 * 1024 * 1024)} GB",
            "Free" to "${probe.storage.freeBytes / (1024 * 1024 * 1024)} GB",
        )),
        section("Display", listOf(
            "Resolution" to "${display.widthPx} x ${display.heightPx}",
            "Density" to "${display.densityDpi} dpi",
            "Refresh rate" to "${display.refreshHz} Hz",
        )),
        section("Battery", listOf(
            "Charge level" to if (battery >= 0f) "${battery.toInt()}%" else "Not exposed by platform",
        )),
        section("Sensors", listOf("Count" to probe.sensors.size.toString()) +
            probe.sensors.take(12).map { it.name to it.vendor }),
        section("Cameras", listOf("Camera ids" to probe.cameraIds.joinToString().ifBlank { "None" })),
        section("Hardware features", listOf(
            "NFC" to probe.hasFeature("android.hardware.nfc").toString(),
            "Bluetooth" to probe.hasFeature("android.hardware.bluetooth").toString(),
            "USB host" to probe.hasFeature("android.hardware.usb.host").toString(),
            "Telephony" to probe.hasFeature("android.hardware.telephony").toString(),
        )),
    )

    Scaffold(topBar = { TopAppBar(title = { Text("Device Inspector") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(sections.size) { i -> sections[i]() }
        }
    }
}

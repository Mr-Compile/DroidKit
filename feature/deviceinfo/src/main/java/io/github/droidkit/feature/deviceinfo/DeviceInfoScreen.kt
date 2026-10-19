package io.github.droidkit.feature.deviceinfo

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import io.github.droidkit.core.ai.AiAssistant
import io.github.droidkit.core.ai.AiExplainButton
import io.github.droidkit.core.system.DeviceSystemProbe
import io.github.droidkit.core.system.formatBytes
import io.github.droidkit.core.system.formatHz
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard

private data class Section(
    val title: String,
    val rows: List<Pair<String, String?>>,
    val labRoute: String? = null,
    val labLabel: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreen(
    nav: NavHostController,
    viewModel: DeviceInfoViewModel = hiltViewModel(),
) {
    val probe = viewModel.probe
    val context = LocalContext.current

    val providerStatuses by viewModel.providerStatuses.collectAsStateWithLifecycle()
    val sections = buildSections(probe, providerStatuses.mapKeys { it.key.name })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Inspector") },
                actions = {
                    IconButton(onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, probe.summary() + "\n" + probe.capabilitySummary())
                        }
                        context.startActivity(Intent.createChooser(send, "Share device summary"))
                    }) { Icon(Icons.Filled.Share, contentDescription = "Share summary") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(sections.size) { i ->
                val section = sections[i]
                SectionCard(title = section.title) {
                    section.rows.forEach { (label, value) -> InfoRow(label, value) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = {
                            copySection(context, section)
                        }) { Text("Copy section") }
                        if (section.labRoute != null && section.labLabel != null) {
                            TextButton(onClick = { nav.navigate(section.labRoute) }) {
                                Text(section.labLabel)
                            }
                        }
                    }
                }
            }
            item {
                AiExplainButton(
                    assistant = AiAssistant.DEVICE_EXPLAINER,
                    fields = sections
                        .flatMap { s -> s.rows.map { (l, v) -> "${s.title} / $l" to v } }
                        .toMap(),
                    onNavigate = { nav.navigate(it) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun buildSections(
    probe: DeviceSystemProbe,
    providerStatuses: Map<String, *>,
): List<Section> {
    val display = probe.displayInfo
    val battery = probe.batteryStatus
    val cameras = probe.cameraSummary

    fun feature(name: String) = probe.hasFeature(name).toString()

    return listOf(
        Section("Identity", listOf(
            "Manufacturer" to probe.manufacturer,
            "Model" to probe.model,
            "Device" to probe.device,
            "Product" to probe.product,
            "App version" to probe.appVersionName,
        )),
        Section("OS", listOf(
            "Android release" to probe.androidRelease,
            "API level" to probe.sdkInt.toString(),
            "Security patch" to probe.securityPatch,
        )),
        Section("Hardware", listOf(
            "Supported ABIs" to probe.supportedAbis.joinToString(),
            "Cameras" to "${cameras.total} (back ${cameras.backFacing}, front ${cameras.frontFacing}, external ${cameras.external})",
            "Sensors" to probe.sensors.size.toString(),
        ), labRoute = "sensors", labLabel = "Sensor inventory"),
        Section("Memory & Storage", listOf(
            "Total RAM" to "${probe.memory.totalMb} MB",
            "Available RAM" to "${probe.memory.availableMb} MB",
            "Low-memory threshold" to "${probe.memory.thresholdMb} MB",
            "Internal storage total" to formatBytes(probe.storage.totalBytes),
            "Internal storage free" to formatBytes(probe.storage.freeBytes),
        ), labRoute = "deviceinfo/storage", labLabel = "Storage Lab"),
        Section("Display", listOf(
            "Resolution" to "${display.widthPx} x ${display.heightPx}",
            "Density" to "${display.densityDpi} dpi",
            "Current refresh rate" to formatHz(display.refreshHz),
            "Supported modes" to probe.displayModeRatesHz.joinToString { formatHz(it) }.ifBlank { null },
        )),
        Section("Battery", listOf(
            "Charge level" to battery.levelPct.takeIf { it >= 0f }?.let { "${it.toInt()}%" },
            "Status" to battery.statusText,
            "Source" to battery.pluggedText,
            "Temperature" to battery.temperatureC.takeIf { it >= 0f }?.let { "%.1f °C".format(it) },
        ), labRoute = "battery", labLabel = "Battery Lab"),
        Section("Hardware features", listOf(
            "NFC" to feature("android.hardware.nfc"),
            "Bluetooth" to feature("android.hardware.bluetooth"),
            "Bluetooth LE" to feature("android.hardware.bluetooth_le"),
            "USB host" to feature("android.hardware.usb.host"),
            "Telephony" to feature("android.hardware.telephony"),
            "Location" to feature("android.hardware.location"),
        )),
        Section("Access providers",
            providerStatuses.entries.map { (k, v) -> k to v.toString() }.ifEmpty {
                listOf("Providers" to null)
            }),
    )
}

private fun copySection(context: android.content.Context, section: Section) {
    val text = buildString {
        appendLine("[${section.title}]")
        section.rows.forEach { (k, v) -> appendLine("$k: ${v ?: "Not exposed"}") }
    }
    val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
        as android.content.ClipboardManager
    cm.setPrimaryClip(android.content.ClipData.newPlainText(section.title, text))
}

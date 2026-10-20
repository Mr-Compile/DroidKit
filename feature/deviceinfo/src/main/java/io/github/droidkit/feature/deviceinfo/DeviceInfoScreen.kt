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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.system.DeviceSystemProbe
import io.github.droidkit.core.system.formatBytes
import io.github.droidkit.core.system.formatHz
import io.github.droidkit.core.system.formatUptime
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.navigation.openRoute

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
    val elevatedProvider by viewModel.elevatedProvider.collectAsStateWithLifecycle()
    val advanced by viewModel.advanced.collectAsStateWithLifecycle()
    val advancedLoading by viewModel.advancedLoading.collectAsStateWithLifecycle()
    val sections = buildSections(probe, providerStatuses.mapKeys { it.key.name })

    Scaffold(
        topBar = {
            DroidKitTopAppBar(
                title = "Device Inspector",
                onBack = { nav.navigateUp() },
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
                            TextButton(onClick = { nav.openRoute(section.labRoute) }) {
                                Text(section.labLabel)
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Advanced detail") {
                    Text(
                        "Read-only deep snapshot via the active elevated provider " +
                            "(Shizuku, root or wireless ADB): system props, thermal zones, " +
                            "/proc details, partitions, battery and Wi-Fi dumpsys fields. " +
                            "Nothing here is modified.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    when {
                        elevatedProvider == null && advanced == null ->
                            InfoRow("Elevated provider", "Not available — set up Shizuku, root or ADB in Permission Center")
                        advanced == null && !advancedLoading ->
                            Button(onClick = { viewModel.loadAdvancedDetail() }) {
                                Text("Load advanced detail")
                            }
                    }
                    if (advancedLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    advanced?.let { adv -> AdvancedDetailRows(adv, onRefresh = { viewModel.loadAdvancedDetail() }) }
                }
            }

            val aiFields = sections
                .flatMap { s -> s.rows.map { (l, v) -> "${s.title} / $l" to v } }
                .toMap()
            item {
                AiSummaryCard(
                    screenTitle = "Device info",
                    fields = aiFields,
                )
            }
            item {
                AiExplainButton(
                    assistant = AiAssistant.DEVICE_EXPLAINER,
                    fields = aiFields,
                    onNavigate = { nav.openRoute(it) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AdvancedDetailRows(adv: AdvancedDetail, onRefresh: () -> Unit) {
    InfoRow("Source provider", adv.provider?.name)
    adv.cpu?.let { cpu ->
        InfoRow(
            "CPU",
            listOfNotNull(
                "${cpu.coreCount} cores".takeIf { cpu.coreCount > 0 },
                cpu.modelName,
                cpu.hardware?.let { "HW $it" },
            ).joinToString(" — ").ifBlank { null },
        )
    }
    adv.mem?.let { mem ->
        InfoRow("MemTotal / MemFree", listOfNotNull(
            mem.totalKb?.let { formatBytes(it * 1024) },
            mem.freeKb?.let { formatBytes(it * 1024) },
        ).joinToString(" / "))
    }
    adv.thermalZones.forEach { zone ->
        InfoRow(zone.name, "${zone.type} — %.1f °C".format(zone.milliCelsius / 1000f))
    }
    if (adv.partitions.isNotEmpty()) {
        InfoRow("Block devices", "${adv.partitions.size} partitions")
    }
    adv.learnedCapacityHint?.let { InfoRow("Learned battery capacity", it) }
    adv.wifiLink?.let { link ->
        InfoRow("Wi-Fi link", listOfNotNull(
            link.ssid,
            link.wifiGeneration,
            link.frequencyMhz?.let { "$it MHz" },
        ).joinToString(" · ").ifBlank { null })
        InfoRow("RSSI / link speed", listOfNotNull(
            link.rssiDbm?.let { "$it dBm" },
            link.linkSpeedMbps?.let { "$it Mbps" },
        ).joinToString(" / "))
    }
    adv.props.forEach { prop -> InfoRow(prop.name, prop.value.ifBlank { "empty" }) }
    InfoRow("System props total", adv.propsTotal.toString())
    adv.batteryDetail.forEach { (k, v) -> InfoRow("battery: $k", v) }
    if (adv.failures.isNotEmpty()) {
        InfoRow("Not exposed", adv.failures.joinToString())
    }
    TextButton(onClick = onRefresh) { Text("Refresh") }
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
        Section("OS & build", listOf(
            "Android release" to probe.androidRelease,
            "API level" to probe.sdkInt.toString(),
            "Security patch" to probe.securityPatch,
            "Fingerprint" to probe.fingerprint,
            "Build ID / display" to "${probe.buildId} / ${probe.buildDisplay}",
            "Incremental" to probe.buildIncremental,
            "Type / tags" to "${probe.buildType} / ${probe.buildTags}",
            "Build date" to probe.buildTimeMs.takeIf { it > 0 }
                ?.let { java.text.DateFormat.getDateInstance().format(java.util.Date(it)) },
            "Kernel" to (probe.kernelDetail ?: probe.kernelRelease),
            "ART runtime" to probe.artVersion,
            "Uptime" to formatUptime(probe.uptimeMs()),
            "Deep sleep (approx.)" to formatUptime(probe.deepSleepMs()),
        )),
        Section("Hardware", listOf(
            "SoC" to listOfNotNull(probe.socManufacturer, probe.socModel)
                .joinToString(" ").ifBlank { null },
            "Board / hardware" to "${probe.board} / ${probe.hardware}",
            "CPU cores" to probe.cpuCoreCount.toString(),
            "Supported ABIs" to probe.supportedAbis.joinToString(),
            "Baseband" to probe.radioVersion,
            "Bootloader" to probe.bootloader,
            "Media codecs" to probe.codecSummary
                ?.let { "${it.decoders} decoders / ${it.encoders} encoders" },
            "Widevine security level" to probe.widevineSecurityLevel,
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
            "HDR" to (probe.displayHdrTypes.takeIf { it.isNotEmpty() }?.joinToString() ?: "None"),
            "Wide color gamut" to probe.displayWideColorGamut?.toString(),
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

package io.github.droidkit.feature.deviceinfo

import android.Manifest
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import io.github.droidkit.core.ui.components.DroidKitButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import io.github.droidkit.core.ai.AiAssistant
import io.github.droidkit.core.ai.AiExplainButton
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.permissions.RuntimePermissionState
import io.github.droidkit.core.system.DeviceSystemProbe
import io.github.droidkit.core.system.formatBytes
import io.github.droidkit.core.system.formatHz
import io.github.droidkit.core.system.formatInches
import io.github.droidkit.core.system.formatUptime
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.adaptive.contentWidthCap
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.SectionHeader
import io.github.droidkit.core.ui.components.rememberRuntimePermission
import io.github.droidkit.core.ui.navigation.openRoute
import java.text.DateFormat
import java.util.Date

private data class Section(
    val title: String,
    val rows: List<Pair<String, String?>>,
    val labRoute: String? = null,
    val labLabel: String? = null,
    /** Sensitive values (identifiers, SIM data) — excluded from AI/copy/share. */
    val sensitive: Boolean = false,
    /** Custom body rendered after [rows] — used by the permission-gated SIM section. */
    val trailing: (@Composable ColumnScope.() -> Unit)? = null,
)

private const val SHOW_REGULATORY_INFO = "android.settings.SHOW_REGULATORY_INFO"

/** Row labels never forwarded to AI context or share/copy summaries. */
private val SENSITIVE_ROW_LABELS = setOf("Android ID")

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

    val regulatoryIntent = remember { Intent(SHOW_REGULATORY_INFO) }
    val canOpenRegulatory = remember {
        runCatching { regulatoryIntent.resolveActivity(context.packageManager) != null }
            .getOrDefault(false)
    }

    val sections = buildSections(
        probe = probe,
        providerStatuses = providerStatuses.mapKeys { it.key.name },
        advanced = advanced,
        elevatedReady = elevatedProvider != null,
        regulatoryBody = if (canOpenRegulatory) {
            {
                TextButton(onClick = { context.startActivity(regulatoryIntent) }) {
                    Text("Regulatory labels")
                }
            }
        } else {
            null
        },
        simBody = { SimSectionBody(viewModel) },
    )

    Scaffold(
        topBar = {
            DroidKitTopAppBar(
                title = "Device Overview",
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
            modifier = Modifier.fillMaxSize().contentWidthCap(),
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
                    section.trailing?.invoke(this)
                    if ((section.rows.isNotEmpty() && !section.sensitive) || section.labRoute != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (section.rows.isNotEmpty() && !section.sensitive) {
                                TextButton(onClick = {
                                    copySection(context, section)
                                }) { Text("Copy section") }
                            }
                            if (section.labRoute != null && section.labLabel != null) {
                                TextButton(onClick = { nav.openRoute(section.labRoute) }) {
                                    Text(section.labLabel)
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Advanced detail") {
                    Text(
                        "Read-only deep snapshot via the active elevated provider " +
                            "(root or wireless ADB): system props, thermal zones, " +
                            "/proc details, partitions, battery and Wi-Fi dumpsys fields. " +
                            "Nothing here is modified.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    when {
                        elevatedProvider == null && advanced == null ->
                            InfoRow("Elevated provider", "Not available — set up wireless ADB or root in Permission Center")
                        advanced == null && !advancedLoading ->
                            DroidKitButton(onClick = { viewModel.loadAdvancedDetail() }) {
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
                .filter { !it.sensitive }
                .flatMap { s -> s.rows.map { (l, v) -> "${s.title} / $l" to v } }
                .filter { (k, _) -> k.substringAfterLast(" / ") !in SENSITIVE_ROW_LABELS }
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
private fun ColumnScope.SimSectionBody(viewModel: DeviceInfoViewModel) {
    val probe = viewModel.probe
    val phoneReq = rememberRuntimePermission(Manifest.permission.READ_PHONE_STATE)
    val sims by viewModel.simStatus.collectAsStateWithLifecycle()

    LaunchedEffect(phoneReq.state) {
        viewModel.refreshSimStatus(phoneReq.state == RuntimePermissionState.GRANTED)
    }

    InfoRow("Supported modems", probe.supportedModemCount?.toString())
    InfoRow("eSIM supported", probe.eSimSupported?.toString())

    when (phoneReq.state) {
        RuntimePermissionState.GRANTED -> {
            if (sims.isEmpty()) {
                InfoRow("Active SIMs", "None detected")
            }
            sims.forEach { sim -> SimSlotRows(sim) }
        }
        RuntimePermissionState.NOT_DECLARED ->
            InfoRow("SIM detail", "READ_PHONE_STATE not declared — grant impossible")
        RuntimePermissionState.DENIED_PERMANENT -> {
            InfoRow("SIM detail", "Phone permission permanently denied")
            TextButton(onClick = { phoneReq.openSettings() }) { Text("Open Settings") }
        }
        RuntimePermissionState.DENIED_REQUESTABLE -> {
            InfoRow("SIM detail", "Permission required for carrier, number and network state")
            DroidKitButton(onClick = { phoneReq.request() }, modifier = Modifier.fillMaxWidth()) {
                Text("Grant phone access")
            }
        }
    }
}

@Composable
private fun ColumnScope.SimSlotRows(sim: SimSlotStatus) {
    SectionHeader("SIM ${sim.slotIndex + 1}${if (sim.isEmbedded) " · eSIM" else ""}")
    InfoRow("Carrier", sim.carrierName)
    InfoRow("Display name", sim.displayName)
    InfoRow("Number", sim.phoneNumber)
    InfoRow("MCC/MNC", sim.mccMnc)
    InfoRow("Carrier ID", sim.carrierId?.toString())
    InfoRow("ICCID", sim.iccIdTail)
    InfoRow("Data roaming", sim.dataRoaming?.let { if (it) "On" else "Off" })
    InfoRow("Service state", sim.serviceState)
    InfoRow("Data network", sim.dataNetworkType)
    InfoRow("Voice network", sim.voiceNetworkType)
    InfoRow("Roaming", sim.networkRoaming?.let { if (it) "Yes" else "No" })
    InfoRow("Signal level", sim.signalLevel?.let { "$it/4" })
    InfoRow(
        "Default for",
        listOfNotNull(
            "Data".takeIf { sim.isDefaultData },
            "Voice".takeIf { sim.isDefaultVoice },
        ).joinToString(", ").ifBlank { null },
    )
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
    advanced: AdvancedDetail?,
    elevatedReady: Boolean,
    regulatoryBody: (@Composable ColumnScope.() -> Unit)?,
    simBody: @Composable ColumnScope.() -> Unit,
): List<Section> {
    val display = probe.displayInfo
    val battery = probe.batteryStatus
    val cameras = probe.cameraSummary

    fun feature(name: String) = probe.hasFeature(name).toString()
    fun prop(name: String): String? = advanced?.props?.firstOrNull { it.name == name }
        ?.value?.takeIf { it.isNotBlank() }

    /** Value when the snapshot has it, else a tier-honest placeholder. */
    fun elevatedOr(value: String?): String? = value
        ?: when {
            !elevatedReady -> "Restricted — no elevated provider"
            advanced == null -> "Restricted — load advanced detail"
            else -> null
        }

    return listOf(
        Section("Identity", listOf(
            "Device name" to probe.deviceName,
            "Manufacturer" to probe.manufacturer,
            "Model" to probe.model,
            "Device" to probe.device,
            "Product" to probe.product,
            "Android ID" to probe.androidId,
            "App version" to probe.appVersionName,
        )),
        Section("Status", listOf(
            "Uptime" to formatUptime(probe.uptimeMs()),
            "Boot time" to DateFormat.getDateTimeInstance().format(Date(probe.bootEpochMs)),
            "IP addresses" to probe.ipAddresses().joinToString(", ").ifBlank { null },
            "USB debugging" to probe.usbDebuggingEnabled?.let { if (it) "On" else "Off" },
            "Developer options" to probe.developmentSettingsEnabled?.let { if (it) "On" else "Off" },
        )),
        Section("SIM & network", emptyList(), sensitive = true, trailing = simBody),
        Section("OS & build", listOf(
            "Android release" to probe.androidRelease,
            "API level" to probe.sdkInt.toString(),
            "Codename" to probe.buildCodename,
            "OS extension level" to probe.sdkExtensionLevel?.toString(),
            "Security patch" to probe.securityPatch,
            "Google Play system update" to probe.googlePlaySystemUpdate,
            "Google Play services" to probe.playServicesVersion,
            "Media performance class" to probe.mediaPerformanceClass?.toString(),
            "Fingerprint" to probe.fingerprint,
            "Build ID / display" to "${probe.buildId} / ${probe.buildDisplay}",
            "Incremental" to probe.buildIncremental,
            "Type / tags" to "${probe.buildType} / ${probe.buildTags}",
            "Build date" to probe.buildTimeMs.takeIf { it > 0 }
                ?.let { DateFormat.getDateInstance().format(Date(it)) },
            "Kernel" to (probe.kernelDetail ?: probe.kernelRelease),
            "ART runtime" to probe.artVersion,
            "Deep sleep (approx.)" to formatUptime(probe.deepSleepMs()),
        )),
        Section("Security", listOf(
            "Security patch" to probe.securityPatch,
            "Storage encryption" to probe.storageEncryptionStatus,
            "SELinux" to elevatedOr(advanced?.selinuxStatus),
            "Verified boot state" to elevatedOr(prop("ro.boot.verifiedbootstate")),
            "Crypto state" to elevatedOr(prop("ro.crypto.state")),
            "Crypto type" to elevatedOr(prop("ro.crypto.type")),
            "Project Treble" to elevatedOr(prop("ro.treble.enabled")),
            "Seamless (A/B) updates" to elevatedOr(prop("ro.build.ab_update")),
            "Base OS" to elevatedOr(prop("ro.build.version.base_os")),
            "First API level" to elevatedOr(prop("ro.product.first_api_level")),
        ), trailing = regulatoryBody),
        Section("Hardware", listOf(
            "SoC" to listOfNotNull(probe.socManufacturer, probe.socModel)
                .joinToString(" ").ifBlank { null },
            "Board / hardware" to "${probe.board} / ${probe.hardware}",
            "CPU cores (app-visible)" to probe.cpuCoreCount.toString(),
            "Supported ABIs" to probe.supportedAbis.joinToString(),
            "OpenGL ES" to probe.glEsVersion,
            "Vulkan API" to (probe.vulkanInfo?.apiVersionBits
                ?.takeIf { it > 0 }
                ?.let { DeviceInfoExtras.vulkanVersionName(it) }
                ?: "Not declared"),
            "Vulkan HW level" to probe.vulkanInfo?.levelBits
                ?.takeIf { it > 0 }
                ?.let { DeviceInfoExtras.vulkanVersionName(it) },
            "Vulkan compute" to probe.vulkanInfo?.hasCompute?.let { if (it) "Yes" else "No" },
            "dEQP level" to probe.vulkanInfo?.deqpLevelBits
                ?.takeIf { it > 0 }
                ?.let { DeviceInfoExtras.deqpDateName(it) },
            "Memory class / large heap" to
                "${probe.memoryClassMb} MB / ${probe.largeMemoryClassMb} MB",
            "Low-RAM device" to probe.isLowRamDevice.let { if (it) "Yes" else "No" },
            "Screen size" to probe.screenSizeInches?.let { formatInches(it) },
            "Baseband" to probe.radioVersion,
            "Bootloader" to probe.bootloader,
            "Widevine security level" to probe.widevineSecurityLevel,
            "Cameras" to "${cameras.total} (back ${cameras.backFacing}, front ${cameras.frontFacing}, external ${cameras.external})",
            "Sensors" to probe.sensors.size.toString(),
        ), labRoute = "sensors", labLabel = "Sensor inventory"),
        Section("Media codecs", listOf(
            "Registered codecs" to probe.codecSummary
                ?.let { "${it.decoders} decoders / ${it.encoders} encoders" },
        ), labRoute = "deviceinfo/codecs", labLabel = "Codec inventory"),
        Section("Identifiers (restricted)", listOf(
            "IMEI" to elevatedOr(advanced?.imeiHint),
            "Serial number" to elevatedOr(
                prop("ro.serialno") ?: prop("sys.serialno") ?: prop("ro.boot.serialno"),
            ),
            "Wi-Fi MAC" to elevatedOr(
                advanced?.linkAddresses
                    ?.firstOrNull { (iface, _) -> iface.startsWith("wlan") }?.second
                    ?: advanced?.linkAddresses?.firstOrNull()?.second,
            ),
            "Bluetooth MAC" to elevatedOr(advanced?.settingsEntries?.get("secure.bluetooth_address")),
        ), sensitive = true),
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
            "UWB" to feature("android.hardware.uwb"),
            "Wi-Fi Aware" to feature("android.hardware.wifi.aware"),
            "Satellite" to feature("android.hardware.telephony.satellite"),
            "Telephony" to feature("android.hardware.telephony"),
            "Location" to feature("android.hardware.location"),
        )),
        Section("Access providers",
            providerStatuses.entries.map { (k, v) -> k to v.toString() }.ifEmpty {
                listOf("Providers" to null)
            },
            labRoute = "permissions", labLabel = "Permission Center"),
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

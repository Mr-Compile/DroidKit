package io.github.droidkit.feature.deviceinfo

import io.github.droidkit.core.privileges.ElevatedAction
import io.github.droidkit.core.privileges.ElevatedOps
import io.github.droidkit.core.privileges.ProviderKind
import javax.inject.Inject
import javax.inject.Singleton

data class PropEntry(val name: String, val value: String)
data class ThermalZone(val name: String, val type: String, val milliCelsius: Long)
data class CpuInfo(val coreCount: Int, val hardware: String?, val modelName: String?)
data class MemInfo(val totalKb: Long?, val freeKb: Long?, val buffersKb: Long?, val cachedKb: Long?)
data class Partition(val major: Int, val minor: Int, val blockCount: Long, val name: String)

data class WifiLinkDetail(
    val ssid: String?,
    val bssid: String?,
    val rssiDbm: Int?,
    val linkSpeedMbps: Int?,
    val frequencyMhz: Int?,
    val wifiGeneration: String?,
)

data class AdvancedDetail(
    val provider: ProviderKind?,
    val props: List<PropEntry>,
    val propsTotal: Int,
    val thermalZones: List<ThermalZone>,
    val cpu: CpuInfo?,
    val mem: MemInfo?,
    val partitions: List<Partition>,
    val batteryDetail: List<Pair<String, String>>,
    /** Learned-capacity hint with its unit preserved — e.g. "3018 mAh (learned)". */
    val learnedCapacityHint: String?,
    val wifiLink: WifiLinkDetail?,
    val failures: List<String>,
) {
    companion object {
        val EMPTY = AdvancedDetail(
            provider = null, props = emptyList(), propsTotal = 0,
            thermalZones = emptyList(), cpu = null, mem = null,
            partitions = emptyList(), batteryDetail = emptyList(),
            learnedCapacityHint = null, wifiLink = null, failures = emptyList(),
        )
    }
}

/**
 * On-demand read-only deep snapshot via whichever elevated provider is ready.
 * Curated display allow-list for system props keeps the output technician-relevant
 * and avoids dumping hundreds of opaque keys into the UI.
 */
@Singleton
class DeviceDeepProbe @Inject constructor(
    private val elevatedOps: ElevatedOps,
) {
    suspend fun activeProvider(): ProviderKind? = elevatedOps.activeProvider()

    suspend fun load(): AdvancedDetail {
        val provider = elevatedOps.activeProvider() ?: return AdvancedDetail.EMPTY
        val failures = mutableListOf<String>()

        suspend fun run(action: ElevatedAction): String {
            val result = elevatedOps.execute(action, timeoutMs = 15_000)
            return if (result.ok) {
                result.stdout
            } else {
                failures += action::class.simpleName ?: "action"
                ""
            }
        }

        val propsOut = run(ElevatedAction.ReadSystemProps)
        val thermalOut = run(ElevatedAction.ReadThermalZones)
        val cpuOut = run(ElevatedAction.ReadCpuInfo)
        val memOut = run(ElevatedAction.ReadMeminfo)
        val partsOut = run(ElevatedAction.ReadPartitions)
        val batteryOut = run(ElevatedAction.DumpBatteryDetail)
        val learnedOut = run(ElevatedAction.ReadLearnedBatteryCapacity)
        val wifiOut = run(ElevatedAction.DumpWifiLink)

        val allProps = parseProps(propsOut)
        return AdvancedDetail(
            provider = provider,
            props = allProps.filter { it.name in DISPLAY_PROP_NAMES },
            propsTotal = allProps.size,
            thermalZones = parseThermalZones(thermalOut),
            cpu = parseCpuInfo(cpuOut),
            mem = parseMemInfo(memOut),
            partitions = parsePartitions(partsOut),
            batteryDetail = parseBatteryDump(batteryOut),
            learnedCapacityHint = parseLearnedCapacity(learnedOut),
            wifiLink = parseWifiLinkDump(wifiOut),
            failures = failures,
        )
    }
}

val DISPLAY_PROP_NAMES: Set<String> = setOf(
    "ro.product.cpu.abi",
    "ro.product.cpu.abilist",
    "ro.product.cpu.abilist32",
    "ro.product.cpu.abilist64",
    "ro.board.platform",
    "ro.hardware",
    "ro.hardware.chipname",
    "ro.soc.model",
    "ro.soc.manufacturer",
    "ro.product.brand",
    "ro.product.device",
    "ro.product.name",
    "ro.product.model",
    "ro.build.display.id",
    "ro.build.version.release",
    "ro.build.version.sdk",
    "ro.build.version.incremental",
    "ro.build.version.security_patch",
    "ro.build.version.base_os",
    "ro.build.selinux",
    "ro.boot.verifiedbootstate",
    "ro.crypto.state",
    "ro.crypto.type",
    "ro.treble.enabled",
    "ro.build.ab_update",
    "ro.virtual_ab.enabled",
    "ro.product.first_api_level",
    "ro.product.vndk.version",
    "ro.vendor.api_level",
    "ro.zygote",
    "ro.kernel.qemu",
    "ro.config.low_ram",
    "dalvik.vm.heapsize",
    "dalvik.vm.heapgrowthlimit",
    "persist.sys.timezone",
)

// ---------- pure parsers (JVM-testable) ----------

private val PROP_LINE = Regex("""^\[([^\]]+)\]:\s*\[(.*)]\s*$""")

fun parseProps(raw: String): List<PropEntry> = raw.lineSequence()
    .mapNotNull { line ->
        PROP_LINE.matchEntire(line.trim())?.let { PropEntry(it.groupValues[1], it.groupValues[2]) }
    }
    .toList()

fun parseThermalZones(raw: String): List<ThermalZone> {
    val zones = mutableListOf<ThermalZone>()
    var name: String? = null
    var type = "unknown"
    var temp: Long? = null
    fun flush() {
        val t = temp
        if (name != null || t != null) zones += ThermalZone(name ?: "zone", type, t ?: 0L)
    }
    raw.lineSequence().map { it.trim() }.forEach { line ->
        when {
            line.startsWith("== ") -> {
                flush()
                name = line.removePrefix("== ").substringAfterLast('/')
                type = "unknown"
                temp = null
            }
            name == null -> Unit
            line.matches(Regex("""-?\d+""")) -> temp = line.toLong()
            type == "unknown" -> type = line
        }
    }
    flush()
    return zones.filter { it.milliCelsius != 0L || it.type != "unknown" }
}

fun parseCpuInfo(raw: String): CpuInfo? {
    if (raw.isBlank()) return null
    var cores = 0
    var hardware: String? = null
    var model: String? = null
    raw.lineSequence().map { it.trim() }.forEach { line ->
        val parts = line.split(':', limit = 2)
        if (parts.size == 2) {
            val key = parts[0].trim().lowercase()
            val value = parts[1].trim()
            when {
                key == "processor" -> cores++
                key == "hardware" -> hardware = value
                key == "model name" && model == null -> model = value
            }
        }
    }
    return if (cores == 0 && hardware == null && model == null) {
        null
    } else {
        CpuInfo(coreCount = cores, hardware = hardware, modelName = model)
    }
}

private val MEMINFO_LINE = Regex("""^(\w+):\s+(\d+)\s*kB""")

fun parseMemInfo(raw: String): MemInfo? {
    if (raw.isBlank()) return null
    val values = raw.lineSequence().mapNotNull { line ->
        MEMINFO_LINE.matchEntire(line.trim())?.let { it.groupValues[1] to it.groupValues[2].toLong() }
    }.toMap()
    return MemInfo(
        totalKb = values["MemTotal"],
        freeKb = values["MemFree"],
        buffersKb = values["Buffers"],
        cachedKb = values["Cached"],
    ).takeIf { it.totalKb != null || it.freeKb != null }
}

private val PARTITION_LINE = Regex("""^\s*(\d+)\s+(\d+)\s+(\d+)\s+(\S+)\s*$""")

fun parsePartitions(raw: String): List<Partition> = raw.lineSequence()
    .mapNotNull { line ->
        PARTITION_LINE.matchEntire(line)?.let { m ->
            Partition(
                major = m.groupValues[1].toInt(),
                minor = m.groupValues[2].toInt(),
                blockCount = m.groupValues[3].toLong(),
                name = m.groupValues[4],
            )
        }
    }
    .toList()

/** `key : value` pairs from `dumpsys battery`, capped to avoid flooding the UI. */
fun parseBatteryDump(raw: String): List<Pair<String, String>> = raw.lineSequence()
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .mapNotNull { line ->
        val sep = line.indexOf(':')
        if (sep <= 0) null else line.substring(0, sep).trim() to line.substring(sep + 1).trim()
    }
    .filter { (k, _) -> k.length <= 40 }
    .take(80)
    .toList()

/**
 * Learned-capacity hints — "Learned battery capacity: 3018 mAh" style lines
 * (mAh) or `Charge counter` batteryproperties rows (µAh). Units stay attached
 * to the value so the UI never has to guess.
 */
fun parseLearnedCapacity(raw: String): String? {
    raw.lineSequence().map { it.trim() }.forEach { line ->
        if (line.contains("learned", ignoreCase = true)) {
            Regex("""([\d][\d\s,.]*\s*mAh)""", RegexOption.IGNORE_CASE).find(line)
                ?.let { return "${it.groupValues[1].trim()} (learned)" }
            Regex("""(\d[\d,.]*)""").find(line)
                ?.let { return "${it.groupValues[1]} mAh (learned)" }
        }
    }
    raw.lineSequence().map { it.trim() }.forEach { line ->
        if (line.contains("counter", ignoreCase = true)) {
            Regex("""(\d{4,})""").find(line)
                ?.let { return "${it.groupValues[1]} µAh (charge counter)" }
        }
    }
    return null
}

fun parseWifiLinkDump(raw: String): WifiLinkDetail? {
    if (raw.isBlank()) return null
    var ssid: String? = null
    var bssid: String? = null
    var rssi: Int? = null
    var link: Int? = null
    var freq: Int? = null
    var gen: String? = null

    fun valueOf(line: String, label: String): String? =
        Regex("""\b$label:\s*([^,]+)""").find(line)?.groupValues?.get(1)?.trim()

    raw.lineSequence().map { it.trim() }.forEach { line ->
        if (ssid == null) ssid = valueOf(line, "SSID")?.removeSurrounding("\"")
        if (bssid == null) bssid = valueOf(line, "BSSID")
        if (rssi == null) rssi = valueOf(line, "RSSI")?.toIntOrNull()
        if (link == null) link = valueOf(line, "Link speed")?.removeSuffix("Mbps")?.trim()?.toIntOrNull()
        if (freq == null) {
            freq = valueOf(line, "Frequency")?.removeSuffix("MHz")?.trim()?.toIntOrNull()
                ?: valueOf(line, "frequency")?.removeSuffix("MHz")?.trim()?.toIntOrNull()
        }
        if (gen == null && line.contains("Wi-Fi generation", ignoreCase = true)) {
            gen = line.substringAfter(':').trim().takeIf { it.isNotBlank() }
        }
    }
    if (ssid == null && rssi == null && link == null && gen == null) return null
    return WifiLinkDetail(ssid, bssid, rssi, link, freq, gen)
}

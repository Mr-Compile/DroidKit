package io.github.droidkit.core.system

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.hardware.display.DisplayManager
import android.media.MediaCodecList
import android.media.MediaDrm
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.Display
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class ProbeSensor(
    val name: String,
    val vendor: String,
    val type: Int,
    val stringType: String,
    val version: Int,
    val maxRange: Float,
    val resolution: Float,
    val powerMa: Float,
    val isWakeUp: Boolean,
    val minDelayUs: Int,
)

data class BatteryStatus(
    val levelPct: Float,        // -1 when not exposed
    val statusText: String,     // Charging | Discharging | Full | Not charging | Unknown
    val pluggedText: String,    // AC | USB | Wireless | Not plugged
    val temperatureC: Float,    // -1f when not exposed
    val voltageMv: Int,         // -1 when not exposed
    val technology: String?,    // null when not exposed
    val healthText: String?,    // Good | Overheat | Dead | ... null when not exposed
    val currentNowUa: Int?,     // instantaneous current, null when unsupported
    val chargeCounterUa: Int?,  // µAh present in pack right now, null when unsupported
    val cycleCount: Int?,       // API 34+ EXTRA_CYCLE_COUNT where the HAL reports it
)

data class CameraSummary(val total: Int, val backFacing: Int, val frontFacing: Int, val external: Int)

data class MemoryInfo(val totalMb: Long, val availableMb: Long, val lowMemory: Boolean, val thresholdMb: Long)

data class StorageInfo(val totalBytes: Long, val freeBytes: Long)

data class DisplayInfo(val widthPx: Int, val heightPx: Int, val densityDpi: Int, val refreshHz: Float)

@Singleton
class DeviceSystemProbe @Inject constructor(
    private val context: Context,
) {
    val sdkInt: Int = Build.VERSION.SDK_INT
    val manufacturer: String = Build.MANUFACTURER
    val model: String = Build.MODEL
    val device: String = Build.DEVICE
    val product: String = Build.PRODUCT
    val androidRelease: String = Build.VERSION.RELEASE
    val securityPatch: String? = Build.VERSION.SECURITY_PATCH
    val supportedAbis: List<String> = Build.SUPPORTED_ABIS.toList()

    // ---- deeper build/hardware identity (all standard API, honest nulls) ----

    val fingerprint: String = Build.FINGERPRINT
    val buildId: String = Build.ID
    val buildDisplay: String = Build.DISPLAY
    val buildType: String = Build.TYPE
    val buildTags: String = Build.TAGS
    val buildIncremental: String = Build.VERSION.INCREMENTAL
    val buildTimeMs: Long = Build.TIME
    val bootloader: String? = Build.BOOTLOADER.takeIf { it.isNotBlank() && it != "unknown" }
    val hardware: String = Build.HARDWARE
    val board: String = Build.BOARD
    val socModel: String? =
        if (sdkInt >= Build.VERSION_CODES.S) Build.SOC_MODEL.takeIf { it.isNotBlank() } else null
    val socManufacturer: String? =
        if (sdkInt >= Build.VERSION_CODES.S) Build.SOC_MANUFACTURER.takeIf { it.isNotBlank() } else null
    val radioVersion: String? = Build.getRadioVersion()?.takeIf { it.isNotBlank() }

    /** Kernel release (`os.version`) — the long line from `/proc/version` when readable. */
    val kernelRelease: String? = System.getProperty("os.version")
    val kernelDetail: String? by lazy {
        runCatching { File("/proc/version").readText().trim() }.getOrNull()
    }

    val cpuCoreCount: Int = Runtime.getRuntime().availableProcessors()
    val artVersion: String? = System.getProperty("java.vm.version")

    /** Milliseconds since boot including deep sleep. */
    fun uptimeMs(): Long = SystemClock.elapsedRealtime()

    /** Approximate deep-sleep total — elapsed realtime minus CPU-awake time. */
    fun deepSleepMs(): Long = (uptimeMs() - SystemClock.uptimeMillis()).coerceAtLeast(0)

    data class CodecSummary(val decoders: Int, val encoders: Int)

    val codecSummary: CodecSummary? by lazy {
        runCatching {
            val infos = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
            CodecSummary(
                decoders = infos.count { !it.isEncoder },
                encoders = infos.count { it.isEncoder },
            )
        }.getOrNull()
    }

    /** Widevine DRM security level (L1/L3) — null when the CDM is absent or refuses. */
    val widevineSecurityLevel: String? by lazy {
        runCatching {
            val drm = MediaDrm(UUID.fromString(WIDEVINE_UUID))
            try {
                drm.getPropertyString("securityLevel")
            } finally {
                drm.close()
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }?.uppercase()
    }

    val displayHdrTypes: List<String>
        get() = defaultDisplay()?.hdrCapabilities?.supportedHdrTypes
            ?.map { hdrTypeName(it) } ?: emptyList()

    val displayWideColorGamut: Boolean?
        get() = defaultDisplay()?.isWideColorGamut

    val appVersionName: String by lazy {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "unknown"
        }
    }

    val memory: MemoryInfo by lazy {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        MemoryInfo(
            info.totalMem / (1024 * 1024),
            info.availMem / (1024 * 1024),
            info.lowMemory,
            info.threshold / (1024 * 1024),
        )
    }

    val totalRamMb: Long by lazy { memory.totalMb }

    val storage: StorageInfo by lazy {
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        StorageInfo(stat.totalBytes, stat.availableBytes)
    }

    val sensors: List<ProbeSensor> by lazy {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        sm?.getSensorList(android.hardware.Sensor.TYPE_ALL)
            ?.map {
                ProbeSensor(
                    name = it.name,
                    vendor = it.vendor,
                    type = it.type,
                    stringType = it.stringType ?: "custom",
                    version = it.version,
                    maxRange = it.maximumRange,
                    resolution = it.resolution,
                    powerMa = it.power,
                    isWakeUp = it.isWakeUpSensor,
                    minDelayUs = it.minDelay,
                )
            }
            ?: emptyList()
    }

    val cameraIds: List<String> by lazy {
        val cm = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        try {
            cm?.cameraIdList?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    val batteryStatus: BatteryStatus
        get() {
            val i: android.content.Intent? = context.registerReceiver(
                null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val raw = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            val level = if (raw in 0..100) raw.toFloat() else -1f
            val cur = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            val counter = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                ?.takeIf { it != Int.MIN_VALUE }
            if (i == null) {
                return BatteryStatus(level, "Unknown", "Not plugged", -1f, -1, null, null,
                    cur?.takeIf { it != Int.MIN_VALUE && it != 0 }, counter, null)
            }
            val status = when (i.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)) {
                android.os.BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                android.os.BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                android.os.BatteryManager.BATTERY_STATUS_FULL -> "Full"
                android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
                else -> "Unknown"
            }
            val plugged = when (i.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, 0)) {
                android.os.BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                android.os.BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                else -> "Not plugged"
            }
            val tempTenths = i.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, -1000)
            val volts = i.getIntExtra(android.os.BatteryManager.EXTRA_VOLTAGE, -1)
            val health = when (i.getIntExtra(android.os.BatteryManager.EXTRA_HEALTH, -1)) {
                android.os.BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                android.os.BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
                android.os.BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                android.os.BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified failure"
                else -> null
            }
            // EXTRA_CYCLE_COUNT exists from API 34; before that the HAL kept it
            // behind a system API. Some HALs also report garbage — pass it
            // through as "reported" data, never interpreted.
            val cycles = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                i.getIntExtra(android.os.BatteryManager.EXTRA_CYCLE_COUNT, -1)
                    .takeIf { it >= 0 }
            } else {
                null
            }
            return BatteryStatus(level, status, plugged,
                if (tempTenths > -1000) tempTenths / 10f else -1f, volts,
                i.getStringExtra(android.os.BatteryManager.EXTRA_TECHNOLOGY), health,
                cur?.takeIf { it != Int.MIN_VALUE && it != 0 }, counter, cycles)
        }

    /** Battery charge counter percent; -1f when the platform does not expose it. */
    val batteryCapacityPct: Float
        get() = batteryStatus.levelPct

    val cameraSummary: CameraSummary by lazy {
        val cm = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        var back = 0; var front = 0; var ext = 0
        try {
            cm?.cameraIdList?.forEach { id ->
                when (cm.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)) {
                    android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK -> back++
                    android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT -> front++
                    else -> ext++
                }
            }
        } catch (e: Exception) { /* leave zeros — absence is honest */ }
        CameraSummary(back + front + ext, back, front, ext)
    }

    /** Default display via DisplayManager — works on all API levels and on the application context. */
    private fun defaultDisplay(): Display? =
        (context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager)
            ?.getDisplay(Display.DEFAULT_DISPLAY)

    val displayModeRatesHz: List<Float>
        get() = defaultDisplay()?.supportedModes?.map { it.refreshRate }?.distinct()?.sorted()
            ?: emptyList()

    val displayInfo: DisplayInfo
        get() {
            val dm: DisplayMetrics = context.resources.displayMetrics
            return DisplayInfo(
                widthPx = dm.widthPixels,
                heightPx = dm.heightPixels,
                densityDpi = dm.densityDpi,
                refreshHz = defaultDisplay()?.refreshRate ?: 0f,
            )
        }

    fun hasFeature(feature: String): Boolean =
        context.packageManager.hasSystemFeature(feature)

    fun capabilitySummary(): String = buildString {
        append("nfc=").append(hasFeature("android.hardware.nfc"))
        append(" bt=").append(hasFeature("android.hardware.bluetooth"))
        append(" usbHost=").append(hasFeature("android.hardware.usb.host"))
        append(" telephony=").append(hasFeature("android.hardware.telephony"))
        append(" cameras=").append(cameraIds.size)
        append(" sensors=").append(sensors.size)
    }

    fun summary(): String = buildString {
        append("$manufacturer $model")
        append(" | Android $androidRelease (API $sdkInt)")
        append(" | ABI ${supportedAbis.firstOrNull().orEmpty()}")
        append(" | RAM ${totalRamMb}MB")
        append(" | Storage free ${storage.freeBytes / (1024 * 1024 * 1024)}GB/${storage.totalBytes / (1024 * 1024 * 1024)}GB")
    }
}

private const val WIDEVINE_UUID = "edef8ba9-79d6-4ace-a3c8-27dcd51d21ed"

private fun hdrTypeName(type: Int): String = when (type) {
    Display.HdrCapabilities.HDR_TYPE_HDR10 -> "HDR10"
    Display.HdrCapabilities.HDR_TYPE_HLG -> "HLG"
    Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> "HDR10+"
    Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> "Dolby Vision"
    else -> "type $type"
}

/** Compact uptime like `3d 4h 12m`. */
fun formatUptime(ms: Long): String {
    val totalMin = ms / 60_000
    val days = totalMin / (24 * 60)
    val hours = (totalMin / 60) % 24
    val mins = totalMin % 60
    return buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0 || days > 0) append("${hours}h ")
        append("${mins}m")
    }.trim()
}

fun formatBytes(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024 * 1024)
    return if (gb >= 1) "%.1f GB".format(gb) else "${bytes / (1024 * 1024)} MB"
}

fun formatHz(hz: Float): String =
    if (hz == hz.toInt().toFloat()) "${hz.toInt()} Hz" else "%.2f Hz".format(hz)

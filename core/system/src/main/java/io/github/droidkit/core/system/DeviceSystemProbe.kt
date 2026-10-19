package io.github.droidkit.core.system

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
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
            if (i == null) {
                return BatteryStatus(level, "Unknown", "Not plugged", -1f, -1, null, null,
                    cur?.takeIf { it != Int.MIN_VALUE && it != 0 })
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
            return BatteryStatus(level, status, plugged,
                if (tempTenths > -1000) tempTenths / 10f else -1f, volts,
                i.getStringExtra(android.os.BatteryManager.EXTRA_TECHNOLOGY), health,
                cur?.takeIf { it != Int.MIN_VALUE && it != 0 })
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

    val displayModeRatesHz: List<Float>
        get() = context.display?.supportedModes?.map { it.refreshRate }?.distinct()?.sorted()
            ?: emptyList()

    val displayInfo: DisplayInfo
        get() {
            val dm: DisplayMetrics = context.resources.displayMetrics
            val display = context.display
            return DisplayInfo(
                widthPx = dm.widthPixels,
                heightPx = dm.heightPixels,
                densityDpi = dm.densityDpi,
                refreshHz = display?.refreshRate ?: 0f,
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

fun formatBytes(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024 * 1024)
    return if (gb >= 1) "%.1f GB".format(gb) else "${bytes / (1024 * 1024)} MB"
}

fun formatHz(hz: Float): String =
    if (hz == hz.toInt().toFloat()) "${hz.toInt()} Hz" else "%.2f Hz".format(hz)

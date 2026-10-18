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

data class ProbeSensor(val name: String, val vendor: String, val type: Int)

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

    val totalRamMb: Long by lazy {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        info.totalMem / (1024 * 1024)
    }

    val storage: StorageInfo by lazy {
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        StorageInfo(stat.totalBytes, stat.availableBytes)
    }

    val sensors: List<ProbeSensor> by lazy {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        sm?.getSensorList(android.hardware.Sensor.TYPE_ALL)
            ?.map { ProbeSensor(it.name, it.vendor, it.type) }
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

    /** Battery charge counter percent; -1f when the platform does not expose it. */
    val batteryCapacityPct: Float
        get() {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val v = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            return if (v in 0..100) v.toFloat() else -1f
        }

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

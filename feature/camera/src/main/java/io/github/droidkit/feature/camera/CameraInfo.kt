package io.github.droidkit.feature.camera

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.graphics.ImageFormat
import android.util.Range
import android.util.Size

/**
 * Pure name/format helpers for Camera2 metadata — constants are compile-time
 * inlined, so every function here is unit-testable on the JVM.
 */
object CameraInfo {

    fun facingName(facing: Int?): String = when (facing) {
        CameraCharacteristics.LENS_FACING_FRONT -> "Front"
        CameraCharacteristics.LENS_FACING_BACK -> "Back"
        CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
        else -> "Unknown"
    }

    fun hardwareLevelName(level: Int?): String = when (level) {
        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "Legacy"
        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited"
        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full"
        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "Level 3"
        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "External"
        else -> "Unknown"
    }

    fun capabilityName(cap: Int): String? = when (cap) {
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE -> "Backward compatible"
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR -> "Manual sensor"
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING -> "Manual post-processing"
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW -> "RAW"
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE -> "Burst"
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS -> "Read sensor settings"
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT -> "Depth"
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO -> "High-speed video"
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MOTION_TRACKING -> "Motion tracking"
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA -> "Logical multi-camera"
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_ULTRA_HIGH_RESOLUTION_SENSOR -> "Ultra-high-res"
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_SECURE_IMAGE_DATA -> "Secure image data"
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_SYSTEM_CAMERA -> "System camera"
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_OFFLINE_PROCESSING -> "Offline processing"
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MONOCHROME -> "Monochrome sensor"
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_REMOSAIC_REPROCESSING -> "Remosaic reprocessing"
        else -> null
    }

    /** Pixel format names for the formats a StreamConfigurationMap exposes. */
    fun imageFormatName(format: Int): String = when (format) {
        ImageFormat.JPEG -> "JPEG"
        ImageFormat.YUV_420_888 -> "YUV_420_888"
        ImageFormat.PRIVATE -> "PRIVATE (Surface)"
        ImageFormat.RAW_SENSOR -> "RAW_SENSOR"
        ImageFormat.RAW10 -> "RAW10"
        ImageFormat.RAW12 -> "RAW12"
        ImageFormat.YV12 -> "YV12"
        ImageFormat.NV21 -> "NV21"
        ImageFormat.HEIC -> "HEIC"
        ImageFormat.DEPTH16 -> "DEPTH16"
        ImageFormat.DEPTH_POINT_CLOUD -> "DEPTH_POINT_CLOUD"
        ImageFormat.Y8 -> "Y8"
        else -> "0x%X".format(format)
    }

    fun sizeLabel(s: Size): String = "${s.width}×${s.height}"

    /**
     * Biggest-first, capped list — full tables get huge on multi-camera phones.
     * Takes width/height pairs so it's testable without android.util.Size.
     */
    fun sizesSummaryPairs(sizes: List<Pair<Int, Int>>?, cap: Int = 8): String? {
        if (sizes.isNullOrEmpty()) return null
        val sorted = sizes.sortedByDescending { (w, h) -> w.toLong() * h }
        val shown = sorted.take(cap).joinToString { (w, h) -> "${w}×${h}" }
        val more = sorted.size - minOf(cap, sorted.size)
        return shown + (if (more > 0) "  (+$more more)" else "")
    }

    fun sizesSummary(sizes: Array<Size>?, cap: Int = 8): String? =
        sizesSummaryPairs(sizes?.map { it.width to it.height }, cap)

    /** Min frame duration → max sustained fps label for one output size. */
    fun maxFpsLabel(minFrameDurationNs: Long): String =
        if (minFrameDurationNs > 0) "max %.0f fps".format(1e9 / minFrameDurationNs)
        else "no frame-duration floor"

    fun fpsRangeLabel(lower: Any?, upper: Any?): String =
        "$lower–$upper fps"

    fun fpsRangeLabel(r: Range<*>): String = fpsRangeLabel(r.lower, r.upper)

    fun videoStabilizationName(mode: Int): String? = when (mode) {
        CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF -> "Off"
        CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON -> "On"
        CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_PREVIEW_STABILIZATION -> "Preview stabilization"
        else -> null
    }

    fun oisName(mode: Int): String? = when (mode) {
        CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF -> "Off"
        CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON -> "On"
        else -> null
    }

    fun afModeName(mode: Int): String? = when (mode) {
        CameraMetadata.CONTROL_AF_MODE_OFF -> "Off"
        CameraMetadata.CONTROL_AF_MODE_AUTO -> "Auto"
        CameraMetadata.CONTROL_AF_MODE_MACRO -> "Macro"
        CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO -> "Continuous video"
        CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE -> "Continuous picture"
        CameraMetadata.CONTROL_AF_MODE_EDOF -> "EDoF"
        else -> null
    }

    fun aeModeName(mode: Int): String? = when (mode) {
        CameraMetadata.CONTROL_AE_MODE_OFF -> "Off"
        CameraMetadata.CONTROL_AE_MODE_ON -> "On"
        CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH -> "Auto flash"
        CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH -> "Always flash"
        CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE -> "Auto flash redeye"
        CameraMetadata.CONTROL_AE_MODE_ON_EXTERNAL_FLASH -> "External flash"
        CameraMetadata.CONTROL_AE_MODE_ON_LOW_LIGHT_BOOST_BRIGHTNESS_PRIORITY ->
            "Low-light boost"
        else -> null
    }

    fun awbModeName(mode: Int): String? = when (mode) {
        CameraMetadata.CONTROL_AWB_MODE_OFF -> "Off"
        CameraMetadata.CONTROL_AWB_MODE_AUTO -> "Auto"
        CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT -> "Incandescent"
        CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT -> "Fluorescent"
        CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT -> "Warm fluorescent"
        CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT -> "Daylight"
        CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT -> "Cloudy daylight"
        CameraMetadata.CONTROL_AWB_MODE_TWILIGHT -> "Twilight"
        CameraMetadata.CONTROL_AWB_MODE_SHADE -> "Shade"
        else -> null
    }

    fun sceneModeName(mode: Int): String? = when (mode) {
        CameraMetadata.CONTROL_SCENE_MODE_DISABLED -> null
        CameraMetadata.CONTROL_SCENE_MODE_FACE_PRIORITY -> "Face priority"
        CameraMetadata.CONTROL_SCENE_MODE_ACTION -> "Action"
        CameraMetadata.CONTROL_SCENE_MODE_PORTRAIT -> "Portrait"
        CameraMetadata.CONTROL_SCENE_MODE_LANDSCAPE -> "Landscape"
        CameraMetadata.CONTROL_SCENE_MODE_NIGHT -> "Night"
        CameraMetadata.CONTROL_SCENE_MODE_NIGHT_PORTRAIT -> "Night portrait"
        CameraMetadata.CONTROL_SCENE_MODE_THEATRE -> "Theatre"
        CameraMetadata.CONTROL_SCENE_MODE_BEACH -> "Beach"
        CameraMetadata.CONTROL_SCENE_MODE_SNOW -> "Snow"
        CameraMetadata.CONTROL_SCENE_MODE_SUNSET -> "Sunset"
        CameraMetadata.CONTROL_SCENE_MODE_STEADYPHOTO -> "Steady photo"
        CameraMetadata.CONTROL_SCENE_MODE_FIREWORKS -> "Fireworks"
        CameraMetadata.CONTROL_SCENE_MODE_SPORTS -> "Sports"
        CameraMetadata.CONTROL_SCENE_MODE_PARTY -> "Party"
        CameraMetadata.CONTROL_SCENE_MODE_CANDLELIGHT -> "Candlelight"
        CameraMetadata.CONTROL_SCENE_MODE_BARCODE -> "Barcode"
        CameraMetadata.CONTROL_SCENE_MODE_HIGH_SPEED_VIDEO -> "High-speed video"
        CameraMetadata.CONTROL_SCENE_MODE_HDR -> "HDR"
        else -> null
    }

    fun effectName(mode: Int): String? = when (mode) {
        CameraMetadata.CONTROL_EFFECT_MODE_OFF -> null
        CameraMetadata.CONTROL_EFFECT_MODE_MONO -> "Mono"
        CameraMetadata.CONTROL_EFFECT_MODE_NEGATIVE -> "Negative"
        CameraMetadata.CONTROL_EFFECT_MODE_SOLARIZE -> "Solarize"
        CameraMetadata.CONTROL_EFFECT_MODE_SEPIA -> "Sepia"
        CameraMetadata.CONTROL_EFFECT_MODE_POSTERIZE -> "Posterize"
        CameraMetadata.CONTROL_EFFECT_MODE_WHITEBOARD -> "Whiteboard"
        CameraMetadata.CONTROL_EFFECT_MODE_BLACKBOARD -> "Blackboard"
        CameraMetadata.CONTROL_EFFECT_MODE_AQUA -> "Aqua"
        else -> null
    }

    fun timestampSourceName(source: Int?): String? = when (source) {
        CameraMetadata.SENSOR_INFO_TIMESTAMP_SOURCE_REALTIME -> "Realtime"
        CameraMetadata.SENSOR_INFO_TIMESTAMP_SOURCE_UNKNOWN -> "Unknown"
        null -> null
        else -> "Unknown"
    }

    /** Neutral pixel-pack label for megapixels — used in sensor summaries. */
    fun megapixels(size: Size?): String? =
        size?.let { "%.1f MP".format(it.width.toDouble() * it.height / 1_000_000.0) }
}

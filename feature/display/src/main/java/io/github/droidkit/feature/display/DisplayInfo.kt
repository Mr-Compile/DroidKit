package io.github.droidkit.feature.display

import android.util.DisplayMetrics
import android.view.Display
import android.view.Surface
import kotlin.math.roundToInt

/**
 * Pure name/format helpers for display diagnostics. Every input is a plain
 * Int/Float so the mappings stay JVM-testable — no Display or metrics objects
 * are required to exercise them.
 */
object DisplayInfo {

    /** HDR type ids from [Display.HdrCapabilities.getHdrTypes]. */
    fun hdrTypeName(type: Int): String? = when (type) {
        Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> "Dolby Vision"
        Display.HdrCapabilities.HDR_TYPE_HDR10 -> "HDR10"
        Display.HdrCapabilities.HDR_TYPE_HLG -> "HLG"
        Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> "HDR10+"
        else -> null
    }

    fun hdrTypeLabel(types: IntArray): String {
        if (types.isEmpty()) return "None reported"
        return types.joinToString(" · ") { hdrTypeName(it) ?: "Unknown ($it)" }
    }

    /** [Display.getRotation] values → degrees. */
    fun rotationName(rotation: Int): String = when (rotation) {
        Surface.ROTATION_0 -> "0°"
        Surface.ROTATION_90 -> "90°"
        Surface.ROTATION_180 -> "180°"
        Surface.ROTATION_270 -> "270°"
        else -> "Unknown ($rotation)"
    }

    /** [Display.getState] values. */
    fun stateName(state: Int): String = when (state) {
        Display.STATE_ON -> "On"
        Display.STATE_OFF -> "Off"
        Display.STATE_DOZE -> "Doze"
        Display.STATE_DOZE_SUSPEND -> "Doze (suspended)"
        Display.STATE_ON_SUSPEND -> "On (suspended)"
        Display.STATE_VR -> "VR"
        Display.STATE_UNKNOWN -> "Unknown"
        else -> "Unknown ($state)"
    }

    /** Classic Android density buckets; anything else is honestly "custom". */
    fun densityBucketName(densityDpi: Int): String = when (densityDpi) {
        DisplayMetrics.DENSITY_LOW -> "ldpi"
        DisplayMetrics.DENSITY_MEDIUM -> "mdpi"
        DisplayMetrics.DENSITY_TV -> "tvdpi"
        DisplayMetrics.DENSITY_HIGH -> "hdpi"
        DisplayMetrics.DENSITY_XHIGH -> "xhdpi"
        DisplayMetrics.DENSITY_XXHIGH -> "xxhdpi"
        DisplayMetrics.DENSITY_XXXHIGH -> "xxxhdpi"
        else -> "custom"
    }

    fun densityLabel(densityDpi: Int): String =
        "$densityDpi dpi (${densityBucketName(densityDpi)})"

    /**
     * Physical panel size estimate in millimetres from pixel dimensions and
     * the display's exact x/y dots-per-inch. Returns null when Android does
     * not provide a usable dpi (<= 0) — callers show "unavailable" instead.
     */
    fun physicalSizeMm(
        widthPx: Int,
        heightPx: Int,
        xdpi: Float,
        ydpi: Float,
    ): Pair<Float, Float>? {
        if (widthPx <= 0 || heightPx <= 0 || xdpi <= 0f || ydpi <= 0f) return null
        return Pair(widthPx / xdpi * 25.4f, heightPx / ydpi * 25.4f)
    }

    fun formatMm(value: Float): String = "%.1f mm".format(value)

    fun physicalSizeLabel(size: Pair<Float, Float>?): String =
        size?.let { "${formatMm(it.first)} x ${formatMm(it.second)}" } ?: "Unavailable"

    /** Diagonal in inches for the classic spec-sheet number. */
    fun diagonalInches(size: Pair<Float, Float>?): String? {
        size ?: return null
        val mm = kotlin.math.sqrt(
            size.first * size.first + size.second * size.second,
        ) / 25.4f
        return "%.1f″".format((mm * 10).roundToInt() / 10f)
    }
}

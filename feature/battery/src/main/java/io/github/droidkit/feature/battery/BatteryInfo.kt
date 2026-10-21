package io.github.droidkit.feature.battery

import kotlin.math.abs
import kotlin.math.roundToInt

/** Pure battery formatting/derivation helpers — all JVM-testable. */
object BatteryInfo {

    /** µA → "xxx mA" label preserving sign; null/unsupported stays null. */
    fun maLabel(ua: Int?): String? = ua?.let { "${it / 1000} mA" }

    /** Instantaneous power estimate in mW from current (µA) and voltage (mV). */
    fun powerMw(currentUa: Int?, voltageMv: Int): Int? {
        if (currentUa == null || currentUa == 0 || voltageMv <= 0) return null
        return (abs(currentUa) / 1000.0 * voltageMv / 1000.0).roundToInt()
    }

    enum class TempLevel { UNKNOWN, NORMAL, WARM, HOT }

    /**
     * Temperature tiers for the warning banner. ≥45 °C is "hot" (existing
     * behavior); 35–45 °C is "warm" — still safe but worth flagging during
     * charging diagnostics.
     */
    fun tempLevel(tempC: Float): TempLevel = when {
        tempC < 0f -> TempLevel.UNKNOWN
        tempC >= 45f -> TempLevel.HOT
        tempC >= 35f -> TempLevel.WARM
        else -> TempLevel.NORMAL
    }

    fun tempHint(level: TempLevel): String? = when (level) {
        TempLevel.HOT -> "Battery unusually warm — let device cool"
        TempLevel.WARM -> "Battery warm — heavy load or fast charge in progress?"
        else -> null
    }

    /**
     * Normalize samples to (x,y) points for a w×h sparkline. y grows downward
     * (canvas coordinates). Single-sample → flat mid line; empty → no points.
     */
    fun sparklinePoints(
        values: List<Double>,
        width: Float,
        height: Float,
    ): List<Pair<Float, Float>> {
        if (values.isEmpty() || width <= 0f || height <= 0f) return emptyList()
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0.0 } ?: 1.0
        return values.mapIndexed { i, v ->
            val x = if (values.size == 1) width / 2f else width * i / (values.size - 1)
            val y = height - ((v - min) / range).toFloat() * height
            x to y
        }
    }
}

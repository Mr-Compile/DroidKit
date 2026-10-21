package io.github.droidkit.feature.sensors

import kotlin.math.sqrt

/**
 * Rolling min/max/mean accumulator for live sensor streams — pure JVM,
 * unit-testable. Tracks each axis independently plus the vector magnitude
 * for multi-axis sensors.
 */
class SensorStats(val axes: Int) {

    var count: Long = 0
        private set

    private val mins = FloatArray(axes) { Float.POSITIVE_INFINITY }
    private val maxs = FloatArray(axes) { Float.NEGATIVE_INFINITY }
    private val sums = DoubleArray(axes)

    /** Latest vector magnitude and the session peak (0 for 1-axis sensors). */
    var lastMagnitude: Float = 0f
        private set
    var peakMagnitude: Float = 0f
        private set

    fun add(values: FloatArray) {
        count++
        for (i in 0 until axes) {
            val v = values.getOrElse(i) { 0f }
            if (v < mins[i]) mins[i] = v
            if (v > maxs[i]) maxs[i] = v
            sums[i] += v
        }
        if (axes >= 2) {
            var m = 0f
            for (i in 0 until minOf(axes, values.size)) {
                val v = values[i]
                m += v * v
            }
            lastMagnitude = sqrt(m)
            if (lastMagnitude > peakMagnitude) peakMagnitude = lastMagnitude
        }
    }

    fun min(axis: Int): Float? = mins.getOrNull(axis)?.takeIf { it.isFinite() }
    fun max(axis: Int): Float? = maxs.getOrNull(axis)?.takeIf { it.isFinite() }
    fun mean(axis: Int): Double? =
        if (count > 0 && axis in 0 until axes) sums[axis] / count else null

    fun reset() {
        count = 0
        lastMagnitude = 0f
        peakMagnitude = 0f
        mins.fill(Float.POSITIVE_INFINITY)
        maxs.fill(Float.NEGATIVE_INFINITY)
        sums.fill(0.0)
    }
}

/** Batch-check verdict — honest about the difference between dead and quiet. */
enum class SensorCheckVerdict {
    /** Delivered at least one event inside the window. */
    RESPONDING,

    /** Sensor exists and was registered but produced zero events — many
     *  sensors (e.g. proximity) only fire on change, so silence is not a
     *  proven failure. */
    SILENT,

    /** No default sensor of this type exists on the device. */
    ABSENT,
}

fun sensorCheckVerdict(sensorPresent: Boolean, events: Int): SensorCheckVerdict = when {
    !sensorPresent -> SensorCheckVerdict.ABSENT
    events > 0 -> SensorCheckVerdict.RESPONDING
    else -> SensorCheckVerdict.SILENT
}

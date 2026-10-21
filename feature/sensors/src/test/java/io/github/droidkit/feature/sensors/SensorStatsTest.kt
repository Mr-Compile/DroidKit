package io.github.droidkit.feature.sensors

import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SensorStatsTest {

    @Test
    fun `accumulates min max mean per axis`() {
        val s = SensorStats(3)
        s.add(floatArrayOf(1f, 2f, 3f))
        s.add(floatArrayOf(-1f, 4f, 0f))
        s.add(floatArrayOf(0f, 0f, 6f))
        assertEquals(3, s.count)
        assertEquals(-1f, s.min(0)!!, 0.001f)
        assertEquals(1f, s.max(0)!!, 0.001f)
        assertEquals(0.0, s.mean(0)!!, 0.001)
        assertEquals(4f, s.max(1)!!, 0.001f)
        assertEquals(2.0, s.mean(1)!!, 0.001)
        assertEquals(0f, s.min(2)!!, 0.001f)
        assertEquals(6f, s.max(2)!!, 0.001f)
        assertEquals(3.0, s.mean(2)!!, 0.001)
    }

    @Test
    fun `tracks vector magnitude for multi-axis sensors`() {
        val s = SensorStats(3)
        s.add(floatArrayOf(3f, 4f, 0f)) // magnitude 5
        s.add(floatArrayOf(1f, 0f, 0f)) // magnitude 1
        assertEquals(1f, s.lastMagnitude, 0.001f)
        assertEquals(5f, s.peakMagnitude, 0.001f)
    }

    @Test
    fun `single-axis sensors skip magnitude`() {
        val s = SensorStats(1)
        s.add(floatArrayOf(42f))
        assertEquals(0f, s.lastMagnitude, 0.001f)
        assertEquals(42f, s.min(0)!!, 0.001f)
    }

    @Test
    fun `empty stats return nulls`() {
        val s = SensorStats(3)
        assertNull(s.min(0))
        assertNull(s.max(0))
        assertNull(s.mean(0))
    }

    @Test
    fun `reset clears the window`() {
        val s = SensorStats(1)
        s.add(floatArrayOf(9f))
        s.reset()
        assertEquals(0, s.count)
        assertNull(s.min(0))
    }

    @Test
    fun `short values arrays are tolerated`() {
        val s = SensorStats(3)
        s.add(floatArrayOf(1f)) // missing y,z -> treated as 0
        assertEquals(1f, s.min(0)!!, 0.001f)
        assertEquals(0f, s.min(1)!!, 0.001f)
        assertEquals(sqrt(1f), s.lastMagnitude, 0.001f)
    }

    @Test
    fun `sensorCheckVerdict maps outcomes honestly`() {
        assertEquals(
            SensorCheckVerdict.ABSENT,
            sensorCheckVerdict(sensorPresent = false, events = 0),
        )
        assertEquals(
            SensorCheckVerdict.RESPONDING,
            sensorCheckVerdict(sensorPresent = true, events = 12),
        )
        // silent != failed
        assertEquals(
            SensorCheckVerdict.SILENT,
            sensorCheckVerdict(sensorPresent = true, events = 0),
        )
    }
}

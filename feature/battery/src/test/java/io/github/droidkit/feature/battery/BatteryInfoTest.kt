package io.github.droidkit.feature.battery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryInfoTest {

    @Test
    fun maLabel() {
        assertEquals("850 mA", BatteryInfo.maLabel(850_000))
        assertEquals("-420 mA", BatteryInfo.maLabel(-420_000))
        assertNull(BatteryInfo.maLabel(null))
    }

    @Test
    fun powerEstimate() {
        // 1 A at 4.2 V ≈ 4200 mW
        assertEquals(4200, BatteryInfo.powerMw(1_000_000, 4200))
        assertNull(BatteryInfo.powerMw(null, 4200))
        assertNull(BatteryInfo.powerMw(1_000_000, -1))
        // negative (discharging) current still yields a magnitude
        assertEquals(2100, BatteryInfo.powerMw(-500_000, 4200))
    }

    @Test
    fun tempTiers() {
        assertEquals(BatteryInfo.TempLevel.UNKNOWN, BatteryInfo.tempLevel(-1f))
        assertEquals(BatteryInfo.TempLevel.NORMAL, BatteryInfo.tempLevel(25f))
        assertEquals(BatteryInfo.TempLevel.WARM, BatteryInfo.tempLevel(35f))
        assertEquals(BatteryInfo.TempLevel.WARM, BatteryInfo.tempLevel(44.9f))
        assertEquals(BatteryInfo.TempLevel.HOT, BatteryInfo.tempLevel(45f))
        assertNull(BatteryInfo.tempHint(BatteryInfo.TempLevel.NORMAL))
        assertNull(BatteryInfo.tempHint(BatteryInfo.TempLevel.UNKNOWN))
    }

    @Test
    fun sparklinePoints() {
        assertTrue(BatteryInfo.sparklinePoints(emptyList(), 100f, 40f).isEmpty())
        assertEquals(1, BatteryInfo.sparklinePoints(listOf(5.0), 100f, 40f).size)
        val pts = BatteryInfo.sparklinePoints(listOf(0.0, 10.0, 5.0), 100f, 40f)
        assertEquals(3, pts.size)
        assertEquals(0f, pts[0].first, 0.01f)
        assertEquals(100f, pts[2].first, 0.01f)
        assertEquals(40f, pts[0].second, 0.01f)   // min -> bottom
        assertEquals(0f, pts[1].second, 0.01f)    // max -> top
        assertEquals(20f, pts[2].second, 0.01f)
        // flat series stays mid-line rather than dividing by zero
        val flat = BatteryInfo.sparklinePoints(listOf(3.0, 3.0, 3.0), 100f, 40f)
        flat.forEach { assertEquals(40f, it.second, 0.01f) }
    }
}

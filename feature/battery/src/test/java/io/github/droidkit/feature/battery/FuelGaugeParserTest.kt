package io.github.droidkit.feature.battery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FuelGaugeParserTest {

    @Test
    fun `full + design on one node yields measured health`() {
        val dump = """
            /sys/class/power_supply/maxfg/charge_full:4750000
            /sys/class/power_supply/maxfg/charge_full_design:5000000
            /sys/class/power_supply/maxfg/cycle_count:412
            /sys/class/power_supply/battery/capacity:86
        """.trimIndent()
        val r = parsePowerSupplyDump(dump)!!
        assertEquals("maxfg", r.node)
        assertEquals(4_750_000L, r.chargeFullUa)
        assertEquals(5_000_000L, r.chargeFullDesignUa)
        assertEquals(412, r.cycleCount)
        assertEquals(95f, r.healthPct!!, 0.01f)
    }

    @Test
    fun `oem soh or asoc node wins over computed ratio`() {
        val dump = """
            /sys/class/power_supply/battery/charge_full:4100000
            /sys/class/power_supply/battery/charge_full_design:5000000
            /sys/class/power_supply/battery/asoc:82
        """.trimIndent()
        val r = parsePowerSupplyDump(dump)!!
        assertEquals(82f, r.healthPct!!, 0.01f)
    }

    @Test
    fun `soh reported in tenths of a percent is normalized`() {
        val dump = "/sys/class/power_supply/bms/soh:915"
        val r = parsePowerSupplyDump(dump)!!
        assertEquals(91.5f, r.stateOfHealthPct!!, 0.01f)
    }

    @Test
    fun `missing or unparsable data returns null, never fabricated`() {
        assertNull(parsePowerSupplyDump(""))
        assertNull(parsePowerSupplyDump("grep: /sys/class/power_supply/*/charge_full: No such file"))
        assertNull(parsePowerSupplyDump("random\nnoise"))
    }

    @Test
    fun `partial exposure still reports what exists`() {
        val dump = "/sys/class/power_supply/battery/cycle_count:77"
        val r = parsePowerSupplyDump(dump)!!
        assertEquals(77, r.cycleCount)
        assertNull(r.healthPct)
    }
}

package io.github.droidkit.feature.appops

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppOpsInfoTest {

    @Test
    fun describeKnownAndUnknownOps() {
        assertEquals("Access the camera", AppOpsInfo.describe("CAMERA"))
        assertEquals("Draw overlays on other apps", AppOpsInfo.describe("SYSTEM_ALERT_WINDOW"))
        assertTrue(AppOpsInfo.describe("SOME_NEW_OP").startsWith("Undocumented op"))
    }

    @Test
    fun modeLabels() {
        assertEquals("Allowed", AppOpsInfo.modeLabel("allow"))
        assertEquals("Foreground only", AppOpsInfo.modeLabel("foreground"))
        assertEquals("Ignored (silently fails)", AppOpsInfo.modeLabel("ignore"))
        assertEquals("Weird", AppOpsInfo.modeLabel("weird"))
    }

    @Test
    fun modeStatsOrdered() {
        val ops = listOf(
            AppOpEntry("A", "deny"),
            AppOpEntry("B", "allow"),
            AppOpEntry("C", "allow"),
            AppOpEntry("D", "ignore"),
        )
        val stats = AppOpsInfo.modeStats(ops)
        assertEquals(listOf("allow" to 2, "deny" to 1, "ignore" to 1), stats)
    }

    @Test
    fun groupByMode() {
        val ops = listOf(
            AppOpEntry("A", "deny"),
            AppOpEntry("B", "allow"),
            AppOpEntry("C", "allow"),
        )
        val groups = AppOpsInfo.groupByMode(ops)
        assertEquals(listOf("allow", "deny"), groups.map { it.first })
        assertEquals(listOf("B", "C"), groups[0].second.map { it.op })
        assertEquals(listOf("A"), groups[1].second.map { it.op })
    }
}

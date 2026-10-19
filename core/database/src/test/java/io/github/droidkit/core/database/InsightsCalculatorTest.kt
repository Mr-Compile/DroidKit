package io.github.droidkit.core.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightsCalculatorTest {

    private fun session(id: String, started: Long, status: String = "COMPLETED") =
        DiagnosticSessionEntity(
            id = id,
            startedAtEpochMs = started,
            completedAtEpochMs = started + 1,
            deviceSnapshotId = null,
            overallStatus = status,
            notes = null,
            appVersion = "0.9.0",
        )

    private fun result(sessionId: String, testId: String, state: String, category: String = "Cat") =
        DiagnosticResultEntity(
            id = "$sessionId-$testId-$state-${category}",
            sessionId = sessionId,
            testId = testId,
            category = category,
            state = state,
            measuredDataJson = null,
            userNote = null,
            timestampEpochMs = 0L,
        )

    @Test
    fun `health counts pass fail and folds every other state into skip`() {
        val results = listOf(
            result("s1", "a", "PASS"), result("s1", "b", "PASS"), result("s1", "c", "PASS"),
            result("s1", "d", "FAIL"),
            result("s1", "e", "NOT_SUPPORTED"), result("s1", "f", "NOT_TESTED"),
            result("s1", "g", "PERMISSION_REQUIRED"), result("s1", "h", "UNKNOWN"),
        )
        val h = InsightsCalculator.health("s1", 1000L, "Full diagnostic", results)
        assertEquals(3, h.pass)
        assertEquals(1, h.fail)
        assertEquals(4, h.skip)
        assertEquals(8, h.total)
        assertEquals(0.375f, h.passRate, 0.0001f)
        assertTrue(h.hasFailure)
    }

    @Test
    fun `health treats unrecognized state strings as skip`() {
        val h = InsightsCalculator.health("s1", 0L, null, listOf(result("s1", "a", "GARBAGE")))
        assertEquals(0, h.pass)
        assertEquals(0, h.fail)
        assertEquals(1, h.skip)
    }

    @Test
    fun `health on empty results yields zero rate`() {
        val h = InsightsCalculator.health("s1", 0L, null, emptyList())
        assertEquals(0, h.total)
        assertEquals(0f, h.passRate, 0.0001f)
    }

    @Test
    fun `inRange keeps only completed sessions at or after cutoff, ascending`() {
        val now = 1_000_000_000L
        val sessions = listOf(
            session("new", now - 1_000),
            session("old", now - 40L * 86_400_000),
            session("mid", now - 10L * 86_400_000),
            session("wip", now - 500, status = "IN_PROGRESS"),
        )
        val thirty = InsightsCalculator.inRange(sessions, InsightRange.DAYS_30, now)
        assertEquals(listOf("mid", "new"), thirty.map { it.id })
        val all = InsightsCalculator.inRange(sessions, InsightRange.ALL, now)
        assertEquals(listOf("old", "mid", "new"), all.map { it.id })
    }

    @Test
    fun `attention ranks by severity and resolves titles`() {
        val results = listOf(
            result("s1", "touch.grid", "FAIL", "Touch"),
            result("s2", "touch.grid", "FAIL", "Touch"),
            result("s2", "audio.microphone", "FAIL", "Audio"),
            result("s1", "audio.microphone", "NOT_SUPPORTED", "Audio"),
            result("s1", "display.color", "PASS", "Display"),
            result("s1", "power.charging", "NOT_TESTED", "Power"),
        )
        val att = InsightsCalculator.attention(results, titleFor = { id ->
            mapOf("touch.grid" to "Touch grid", "audio.microphone" to "Microphone")[id]
        })
        assertEquals(3, att.size)
        assertEquals("touch.grid", att[0].testId)          // severity 4
        assertEquals("audio.microphone", att[1].testId)    // severity 3
        assertEquals("power.charging", att[2].testId)      // severity 1
        assertEquals("Touch grid", att[0].title)
        assertEquals("Touch", att[0].category)
        assertEquals("power.charging", att[2].title)       // fallback = raw testId
        assertTrue(att.none { it.testId == "display.color" })
    }

    @Test
    fun `attention honors the limit`() {
        val results = (1..6).flatMap { i ->
            listOf(result("s1", "t.$i", "FAIL"), result("s2", "t.$i", "FAIL"))
        }
        assertEquals(4, InsightsCalculator.attention(results, { null }, limit = 4).size)
    }

    @Test
    fun `storageTrend decodes first volume free bytes ascending`() {
        val snap = { id: String, at: Long, name: String, total: Long, free: Long ->
            StorageSnapshotEntity(
                id = id,
                capturedAtEpochMs = at,
                label = null,
                volumesData = "$name|/data|mounted|$total|$free\nother|/ext|mounted|$total|$free",
            )
        }
        val pts = InsightsCalculator.storageTrend(listOf(
            snap("b", 2000L, "Internal (data)", 100, 40),
            snap("a", 1000L, "Internal (data)", 100, 60),
            StorageSnapshotEntity("bad", 1500L, null, "malformed"),
        ))
        assertEquals(listOf(1000L, 2000L), pts.map { it.atEpochMs })
        assertEquals(listOf(60L, 40L), pts.map { it.freeBytes })
    }

    @Test
    fun `InsightRange cutoff math`() {
        assertEquals(0L, InsightRange.ALL.cutoff(0L).coerceAtLeast(0L))
        assertEquals(1_000L - 30L * 86_400_000, InsightRange.DAYS_30.cutoff(1_000L))
        assertNull(null)
    }
}

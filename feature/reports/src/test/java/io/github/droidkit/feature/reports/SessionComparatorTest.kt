package io.github.droidkit.feature.reports

import io.github.droidkit.core.database.DiagnosticResultEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionComparatorTest {

    private fun result(
        testId: String,
        state: String,
        ts: Long = 1,
        category: String = "Cat",
    ) = DiagnosticResultEntity(
        id = "$testId-$ts",
        sessionId = "s",
        testId = testId,
        category = category,
        state = state,
        measuredDataJson = null,
        userNote = null,
        timestampEpochMs = ts,
    )

    @Test
    fun `fail to pass marks FIXED`() {
        val out = SessionComparator.compare(
            listOf(result("touch.grid", "FAIL")),
            listOf(result("touch.grid", "PASS")),
        )
        assertEquals(Transition.FIXED, out.single().transition)
    }

    @Test
    fun `pass to fail marks REGRESSED`() {
        val out = SessionComparator.compare(
            listOf(result("camera.rear", "PASS")),
            listOf(result("camera.rear", "FAIL")),
        )
        assertEquals(Transition.REGRESSED, out.single().transition)
    }

    @Test
    fun `new removed unchanged classified`() {
        val out = SessionComparator.compare(
            listOf(result("a", "PASS"), result("b", "PASS")),
            listOf(result("a", "PASS"), result("c", "FAIL")),
        ).associateBy { it.testId }
        assertEquals(Transition.UNCHANGED, out.getValue("a").transition)
        assertEquals(Transition.REMOVED, out.getValue("b").transition)
        assertEquals(Transition.NEW, out.getValue("c").transition)
    }

    @Test
    fun `latest retry result wins`() {
        val out = SessionComparator.compare(
            listOf(result("a", "FAIL", ts = 1), result("a", "PASS", ts = 2)),
            listOf(result("a", "PASS", ts = 3)),
        )
        assertEquals(Transition.UNCHANGED, out.single().transition)
    }

    @Test
    fun `not_supported to pass improves`() {
        val out = SessionComparator.compare(
            listOf(result("nfc", "NOT_SUPPORTED")),
            listOf(result("nfc", "PASS")),
        )
        assertEquals(Transition.IMPROVED, out.single().transition)
    }

    @Test
    fun `fail to not_supported is changed not fixed`() {
        val out = SessionComparator.compare(
            listOf(result("x", "FAIL")),
            listOf(result("x", "NOT_SUPPORTED")),
        )
        assertEquals(Transition.CHANGED, out.single().transition)
    }

    @Test
    fun `empty inputs produce empty diff`() {
        assertTrue(SessionComparator.compare(emptyList(), emptyList()).isEmpty())
    }
}

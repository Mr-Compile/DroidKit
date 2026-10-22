package io.github.droidkit.feature.diagnostics

import io.github.droidkit.core.common.TestResultState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionEngineTest {

    private fun engine() = SessionEngine(PhoneDoctorTests.ALL)

    @Test fun `registry contains core and hardware tests`() {
        assertEquals(23, PhoneDoctorTests.ALL.size)
        assertEquals(10, PhoneDoctorTests.CORE.size)
        assertEquals(13, PhoneDoctorTests.HARDWARE.size)
        assertEquals(PhoneDoctorTests.CORE.last().id, PhoneDoctorTests.ALL[9].id)
    }

    @Test fun `engine walks tests in registry order`() {
        val e = engine()
        e.start()
        assertEquals(PhoneDoctorTests.ALL.first().id, e.state.value.current?.id)
        assertEquals(PhoneDoctorTests.ALL.size, e.state.value.total)
        e.record(TestResultState.PASS, null)
        assertEquals(PhoneDoctorTests.ALL[1].id, e.state.value.current?.id)
        assertEquals(1, e.state.value.index)
    }

    @Test fun `record stores result and skip stores NOT_TESTED`() {
        val e = engine()
        e.start()
        val first = PhoneDoctorTests.ALL[0].id
        val second = PhoneDoctorTests.ALL[1].id
        e.record(TestResultState.FAIL, "dead pixel top-left")
        e.skip()
        assertEquals(TestResultState.FAIL, e.state.value.results[first])
        assertEquals(TestResultState.NOT_TESTED, e.state.value.results[second])
        assertEquals("dead pixel top-left", e.state.value.notes[first])
        assertFalse(e.state.value.notes.containsKey(second))
    }

    @Test fun `record ignores blank notes`() {
        val e = engine()
        e.start()
        e.record(TestResultState.PASS, "   ")
        assertFalse(e.state.value.notes.containsKey(PhoneDoctorTests.ALL[0].id))
    }

    @Test fun `finish marks engine done after all tests`() {
        val e = engine()
        e.start()
        repeat(PhoneDoctorTests.ALL.size) { e.skip() }
        assertTrue(e.state.value.done)
        assertNull(e.state.value.current)
    }

    @Test fun `finish without answering all marks remaining NOT_TESTED`() {
        val e = engine()
        e.start()
        e.record(TestResultState.PASS, null)
        e.finish()
        val s = e.state.value
        assertTrue(s.done)
        assertEquals(PhoneDoctorTests.ALL.size, s.results.size)
        assertEquals(TestResultState.NOT_TESTED, s.results[PhoneDoctorTests.ALL.last().id])
    }

    @Test fun `restart requeues selected tests`() {
        val e = engine()
        e.start()
        e.record(TestResultState.FAIL, "x")
        e.skip()
        e.finish()
        val failed = PhoneDoctorTests.ALL[0].id
        e.restart(listOf(failed))
        assertFalse(e.state.value.done)
        assertEquals(failed, e.state.value.current?.id)
        assertEquals(1, e.state.value.total)
        assertFalse(e.state.value.results.containsKey(failed))
    }

    @Test fun `seed resumes at first unanswered test`() {
        val e = engine()
        val first = PhoneDoctorTests.ALL[0].id
        val second = PhoneDoctorTests.ALL[1].id
        e.seed(
            PhoneDoctorTests.ALL.map { it.id },
            mapOf(first to TestResultState.PASS, second to TestResultState.FAIL),
            mapOf(first to "looks fine"),
        )
        val s = e.state.value
        assertFalse(s.done)
        assertEquals(2, s.index)
        assertEquals(PhoneDoctorTests.ALL[2].id, s.current?.id)
        assertEquals(PhoneDoctorTests.ALL.size, s.total)
        assertEquals(TestResultState.FAIL, s.results[second])
        assertEquals("looks fine", s.notes[first])
    }

    @Test fun `seed keeps catalog order for a subset queue`() {
        val e = engine()
        val ids = PhoneDoctorTests.USED_PHONE.map { it.id }
        e.seed(ids, mapOf(ids.first() to TestResultState.PASS), emptyMap())
        val s = e.state.value
        assertEquals(PhoneDoctorTests.USED_PHONE.size, s.total)
        assertEquals(PhoneDoctorTests.USED_PHONE[1].id, s.current?.id)
    }

    @Test fun `seed with every test answered finishes immediately`() {
        val e = engine()
        val only = PhoneDoctorTests.ALL[0].id
        e.seed(listOf(only), mapOf(only to TestResultState.PASS), emptyMap())
        assertTrue(e.state.value.done)
        assertNull(e.state.value.current)
    }

    @Test fun `record after seed appends to resumed results`() {
        val e = engine()
        val first = PhoneDoctorTests.ALL[0].id
        val second = PhoneDoctorTests.ALL[1].id
        e.seed(
            PhoneDoctorTests.ALL.map { it.id },
            mapOf(first to TestResultState.PASS),
            emptyMap(),
        )
        e.record(TestResultState.FAIL, "crackling")
        assertEquals(TestResultState.PASS, e.state.value.results[first])
        assertEquals(TestResultState.FAIL, e.state.value.results[second])
        assertEquals(PhoneDoctorTests.ALL[2].id, e.state.value.current?.id)
    }

    @Test fun `reset clears a stale finished state`() {
        val e = engine()
        e.start()
        e.finish()
        assertTrue(e.state.value.done)
        e.reset()
        val s = e.state.value
        assertFalse(s.done)
        assertNull(s.current)
        assertTrue(s.results.isEmpty())
        assertEquals(0, s.total)
    }
}

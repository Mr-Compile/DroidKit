package io.github.droidkit.feature.diagnostics

import io.github.droidkit.core.common.TestResultState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionEngineTest {

    private fun engine() = SessionEngine(PhoneDoctorTests.CORE)

    @Test fun `engine walks tests in registry order`() {
        val e = engine()
        e.start()
        assertEquals(PhoneDoctorTests.CORE.first().id, e.state.value.current?.id)
        assertEquals(PhoneDoctorTests.CORE.size, e.state.value.total)
        e.record(TestResultState.PASS, null)
        assertEquals(PhoneDoctorTests.CORE[1].id, e.state.value.current?.id)
        assertEquals(1, e.state.value.index)
    }

    @Test fun `record stores result and skip stores NOT_TESTED`() {
        val e = engine()
        e.start()
        val first = PhoneDoctorTests.CORE[0].id
        val second = PhoneDoctorTests.CORE[1].id
        e.record(TestResultState.FAIL, "dead pixel top-left")
        e.skip()
        assertEquals(TestResultState.FAIL, e.state.value.results[first])
        assertEquals(TestResultState.NOT_TESTED, e.state.value.results[second])
    }

    @Test fun `finish marks engine done after all tests`() {
        val e = engine()
        e.start()
        repeat(PhoneDoctorTests.CORE.size) { e.skip() }
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
        assertEquals(PhoneDoctorTests.CORE.size, s.results.size)
        assertEquals(TestResultState.NOT_TESTED, s.results[PhoneDoctorTests.CORE.last().id])
    }

    @Test fun `restart requeues selected tests`() {
        val e = engine()
        e.start()
        e.record(TestResultState.FAIL, "x")
        e.skip()
        e.finish()
        val failed = PhoneDoctorTests.CORE[0].id
        e.restart(listOf(failed))
        assertFalse(e.state.value.done)
        assertEquals(failed, e.state.value.current?.id)
        assertEquals(1, e.state.value.total)
        assertFalse(e.state.value.results.containsKey(failed))
    }
}

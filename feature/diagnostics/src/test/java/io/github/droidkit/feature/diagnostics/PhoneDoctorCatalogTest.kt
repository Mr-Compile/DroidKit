package io.github.droidkit.feature.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * DK-050: every catalog entry must route to a real test screen — the
 * Unsupported placeholder is a safety net, never a shipped destination.
 */
class PhoneDoctorCatalogTest {

    @Test fun `every catalog test routes to a real screen`() {
        val unrouted = PhoneDoctorTests.ALL.filter { routeFor(it) == PhoneDoctorRoute.Unsupported }
        assertTrue("Unrouted catalog entries: ${unrouted.map { it.id }}", unrouted.isEmpty())
    }

    @Test fun `used-phone preset is a strict subset of the catalog`() {
        val ids = PhoneDoctorTests.ALL.map { it.id }.toSet()
        PhoneDoctorTests.USED_PHONE.forEach { assertTrue(it.id, it.id in ids) }
        assertTrue(PhoneDoctorTests.USED_PHONE.size < PhoneDoctorTests.ALL.size)
    }

    @Test fun `catalog ids are unique`() {
        val ids = PhoneDoctorTests.ALL.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test fun `sensor tests carry a sensorType and connectivity tests a capabilityKey`() {
        PhoneDoctorTests.ALL.filter { it.id.startsWith("sensor.") }.forEach {
            assertNotNull(it.id, it.sensorType)
        }
        PhoneDoctorTests.ALL.filter { it.id.startsWith("connectivity.") }.forEach {
            assertNotNull(it.id, it.capabilityKey)
        }
    }
}

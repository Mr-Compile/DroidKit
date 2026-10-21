package io.github.droidkit.feature.diagnostics

import io.github.droidkit.core.ui.theme.DroidKitTestColors
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

    @Test fun `listener tests resolve to their dedicated screens`() {
        assertEquals(
            PhoneDoctorRoute.HeadsetJack,
            routeFor(PhoneDoctorTests.findById("audio.headset_jack")!!),
        )
        assertEquals(
            PhoneDoctorRoute.VolumeButtons,
            routeFor(PhoneDoctorTests.findById("buttons.volume")!!),
        )
        assertEquals(
            PhoneDoctorRoute.ChargingCheck,
            routeFor(PhoneDoctorTests.findById("power.charging")!!),
        )
    }

    @Test fun `every catalog test is searchable with a real route and icon`() {
        val entries = DiagnosticsFeature.searchEntries
        val byId = entries.associateBy { it.id }
        PhoneDoctorTests.ALL.forEach { test ->
            val entry = byId["diag.${test.id}"]
            assertNotNull("missing search entry for ${test.id}", entry)
            entry!!
            assertEquals(DiagnosticsFeature.testRoute(test.id), entry.route)
            assertTrue(entry.iconKey.isNotEmpty())
        }
        assertEquals(PhoneDoctorTests.ALL.size, entries.size)
    }

    @Test fun `every catalog test resolves an icon key and a category accent`() {
        PhoneDoctorTests.ALL.forEach { test ->
            assertNotNull("no icon for ${test.id}", DiagnosticsFeature.testIconKey(test.id))
            assertNotNull(
                "no tile accent for category ${test.category}",
                DroidKitTestColors.accent(test.category, darkTheme = true),
            )
            assertNotNull(DroidKitTestColors.accent(test.category, darkTheme = false))
        }
    }
}

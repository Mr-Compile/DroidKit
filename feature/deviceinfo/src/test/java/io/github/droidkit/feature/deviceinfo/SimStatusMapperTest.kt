package io.github.droidkit.feature.deviceinfo

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The network/service/SIM-state constants are compile-time ints inlined into
 * these mappers, so the pure functions are JVM-testable without Robolectric.
 */
class SimStatusMapperTest {

    @Test fun `network types map to readable names`() {
        assertEquals("LTE", networkTypeName(13))
        assertEquals("NR (5G)", networkTypeName(20))
        assertEquals("HSPA+", networkTypeName(15))
        assertEquals("Unknown", networkTypeName(0))
        assertEquals("Type 99", networkTypeName(99))
    }

    @Test fun `service states map to readable names`() {
        assertEquals("In service", serviceStateName(0))
        assertEquals("Out of service", serviceStateName(1))
        assertEquals("Emergency calls only", serviceStateName(2))
        assertEquals("Radio off", serviceStateName(3))
        assertEquals("Unknown", serviceStateName(7))
    }

    @Test fun `sim states map to readable names`() {
        assertEquals("Ready", simStateName(5))
        assertEquals("Absent", simStateName(1))
        assertEquals("PIN required", simStateName(2))
        assertEquals("Permanently disabled", simStateName(7))
        assertEquals("Unknown", simStateName(42))
    }
}

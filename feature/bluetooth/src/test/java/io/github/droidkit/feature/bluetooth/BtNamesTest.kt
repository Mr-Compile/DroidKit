package io.github.droidkit.feature.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Test

class BtNamesTest {

    @Test
    fun `device type names`() {
        assertEquals("Classic", BtNames.deviceType(1))
        assertEquals("LE", BtNames.deviceType(2))
        assertEquals("Dual (Classic + LE)", BtNames.deviceType(3))
        assertEquals("Unknown", BtNames.deviceType(0))
        assertEquals("Unknown", BtNames.deviceType(99))
    }

    @Test
    fun `bond state names`() {
        assertEquals("Not bonded", BtNames.bondState(10))
        assertEquals("Bonding", BtNames.bondState(11))
        assertEquals("Bonded", BtNames.bondState(12))
        assertEquals("Unknown", BtNames.bondState(-1))
    }
}

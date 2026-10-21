package io.github.droidkit.feature.nfc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NfcTechProbeTest {

    @Test
    fun `classic type names map framework constants`() {
        assertEquals("Classic", NfcTechProbe.classicTypeName(0))
        assertEquals("Plus", NfcTechProbe.classicTypeName(1))
        assertEquals("Pro", NfcTechProbe.classicTypeName(2))
        assertEquals("Unknown (7)", NfcTechProbe.classicTypeName(7))
    }

    @Test
    fun `ultralight type names map framework constants`() {
        assertEquals("Ultralight", NfcTechProbe.ultralightTypeName(1))
        assertEquals("Ultralight C", NfcTechProbe.ultralightTypeName(2))
        assertEquals("Unknown (9)", NfcTechProbe.ultralightTypeName(9))
    }

    @Test
    fun `ultralight page cap is bounded`() {
        assertTrue(NfcTechProbe.ULTRALIGHT_MAX_PAGES in 16..256)
    }
}

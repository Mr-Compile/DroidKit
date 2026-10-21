package io.github.droidkit.core.system

import org.junit.Assert.assertEquals
import org.junit.Test

class ProbeFormattersTest {
    @Test fun `formatBytes renders GB for large values`() {
        assertEquals("64.0 GB", formatBytes(64L * 1024 * 1024 * 1024))
    }
    @Test fun `formatBytes renders MB under 1 GB`() {
        assertEquals("512 MB", formatBytes(512L * 1024 * 1024))
    }
    @Test fun `formatHz trims trailing zero`() {
        assertEquals("120 Hz", formatHz(120f))
        assertEquals("59.94 Hz", formatHz(59.94f))
    }
    @Test fun `formatInches renders one decimal with inch mark`() {
        assertEquals("6.7\"", formatInches(6.71f))
        assertEquals("5.0\"", formatInches(5.0f))
    }
}

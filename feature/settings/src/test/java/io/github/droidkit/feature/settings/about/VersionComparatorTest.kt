package io.github.droidkit.feature.settings.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {

    @Test
    fun `equal versions compare zero`() {
        assertEquals(0, VersionComparator.compare("0.8.0", "0.8.0"))
        assertEquals(0, VersionComparator.compare("1.0.0", "1.0.0"))
    }

    @Test
    fun `leading v is ignored`() {
        assertEquals(0, VersionComparator.compare("v0.8.0", "0.8.0"))
        assertEquals(0, VersionComparator.compare("v1.0.0", "1.0.0"))
    }

    @Test
    fun `numeric segments order correctly`() {
        assertTrue(VersionComparator.compare("0.9.0", "0.8.0") > 0)
        assertTrue(VersionComparator.compare("1.0.0", "0.9.9") > 0)
        assertTrue(VersionComparator.compare("0.8.1", "0.8.0") > 0)
        assertTrue(VersionComparator.compare("0.8.0", "0.9.0") < 0)
        assertTrue(VersionComparator.compare("0.10.0", "0.9.0") > 0)
    }

    @Test
    fun `missing segments pad with zero`() {
        assertEquals(0, VersionComparator.compare("1.0", "1.0.0"))
        assertTrue(VersionComparator.compare("1.0.1", "1.0") > 0)
    }

    @Test
    fun `stable release outranks prerelease of same core`() {
        assertTrue(VersionComparator.compare("0.9.0", "0.9.0-alpha.1") > 0)
        assertTrue(VersionComparator.compare("1.0.0", "1.0.0-beta.2") > 0)
        assertTrue(VersionComparator.compare("0.9.0-alpha.1", "0.9.0") < 0)
    }

    @Test
    fun `prerelease identifiers compare per semver`() {
        assertTrue(VersionComparator.compare("0.9.0-alpha.2", "0.9.0-alpha.1") > 0)
        assertTrue(VersionComparator.compare("0.9.0-beta", "0.9.0-alpha") > 0)
        assertTrue(VersionComparator.compare("0.9.0-alpha.1", "0.9.0-alpha") > 0)
        assertTrue(VersionComparator.compare("0.9.0-alpha", "0.9.0-alpha.1") < 0)
    }

    @Test
    fun `newer tag beats installed even across prerelease boundary`() {
        // installed alpha vs published stable of the same core
        assertTrue(VersionComparator.compare("v0.9.0", "0.9.0-alpha.1") > 0)
        // installed stable vs newer alpha
        assertTrue(VersionComparator.compare("v1.0.0-beta.1", "0.9.0") > 0)
    }

    @Test
    fun `build metadata does not affect order`() {
        assertEquals(0, VersionComparator.compare("1.0.0+5", "1.0.0"))
    }
}

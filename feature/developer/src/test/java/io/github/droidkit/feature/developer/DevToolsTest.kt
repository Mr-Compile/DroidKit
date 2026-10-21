package io.github.droidkit.feature.developer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DevToolsTest {

    @Test
    fun parsePropsReadsBracketLines() {
        val out = """
            [ro.build.fingerprint]: [google/panther/panther:15/AP3A:12345:user/release-keys]
            [persist.sys.timezone]: [Europe/Berlin]
            noise line that is not a prop
            [ro.debuggable]: [1]
        """.trimIndent()
        val props = DevTools.parseProps(out)
        assertEquals(3, props.size)
        assertEquals("ro.build.fingerprint", props[0].first)
        assertEquals("google/panther/panther:15/AP3A:12345:user/release-keys", props[0].second)
        assertEquals("1", props[2].second)
    }

    @Test
    fun parsePropsEmptyAndGarbage() {
        assertTrue(DevTools.parseProps("").isEmpty())
        assertTrue(DevTools.parseProps("getprop: not found").isEmpty())
    }

    @Test
    fun propNotesCoverKnownKeys() {
        assertEquals(
            "Security patch level",
            DevTools.propNote("ro.build.version.security_patch"),
        )
        assertNull(DevTools.propNote("vendor.some.obscure.prop"))
    }

    @Test
    fun mbFormats() {
        assertEquals("0 MB", DevTools.mb(0))
        assertEquals("1,024 MB", DevTools.mb(1L shl 30))
        assertEquals("7,753 MB", DevTools.mb(8_130_000_000L))
    }
}

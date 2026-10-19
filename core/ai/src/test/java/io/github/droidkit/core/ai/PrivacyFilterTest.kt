package io.github.droidkit.core.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyFilterTest {

    private val filter = PrivacyFilter()

    @Test
    fun `groq key is redacted`() {
        val out = filter.redact("key = gsk_abcdefghijklmnopqrstuvwxyz123456")
        assertFalse(out.contains("gsk_"))
        assertTrue(out.contains("[REDACTED_API_KEY]"))
    }

    @Test
    fun `bearer token is redacted`() {
        val out = filter.redact("Authorization: Bearer abcdef1234567890")
        assertFalse(out.contains("abcdef1234567890"))
    }

    @Test
    fun `private key block is redacted`() {
        val pem = "-----BEGIN PRIVATE KEY-----\nMIIabc\n-----END PRIVATE KEY-----"
        val out = filter.redact("before $pem after")
        assertFalse(out.contains("MIIabc"))
        assertTrue(out.contains("[REDACTED_PRIVATE_KEY]"))
    }

    @Test
    fun `mac address is redacted`() {
        val out = filter.redact("bssid AA:BB:CC:DD:EE:FF")
        assertFalse(out.contains("AA:BB:CC:DD:EE:FF"))
    }

    @Test
    fun `ordinary values pass through`() {
        val out = filter.redact("model Pixel 8 Android 15")
        assertTrue(out.contains("Pixel 8"))
    }

    @Test
    fun `minimize truncates`() {
        assertTrue(filter.minimize("report line. ".repeat(1_000), 100).length == 100)
    }
}

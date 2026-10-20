package io.github.droidkit.core.privileges

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdbQrPayloadTest {

    @Test
    fun `parse accepts canonical Android Studio payload`() {
        val payload = AdbQrPayload.parse("WIFI:T:ADB;S:studio-58m*7E2fq4;P:aBc123XyZ0;;")
        assertEquals("studio-58m*7E2fq4", payload?.serviceName)
        assertEquals("aBc123XyZ0", payload?.secret)
    }

    @Test
    fun `parse tolerates single trailing semicolon and whitespace`() {
        val payload = AdbQrPayload.parse("  WIFI:T:ADB;S:adb-abc-123;P:secret01; ")
        assertEquals("adb-abc-123", payload?.serviceName)
        assertEquals("secret01", payload?.secret)
    }

    @Test
    fun `parse rejects non-adb payloads`() {
        assertNull(AdbQrPayload.parse("WIFI:T:WPA;S:mynetwork;P:password;;"))
        assertNull(AdbQrPayload.parse("https://example.com"))
        assertNull(AdbQrPayload.parse("WIFI:T:ADB;S:only-name;;"))
        assertNull(AdbQrPayload.parse(""))
    }

    @Test
    fun `encode produces a parseable payload round trip`() {
        val original = AdbQrPayload("droidkit-abc123", "Secret12345")
        val reparsed = AdbQrPayload.parse(original.encode())
        assertEquals(original, reparsed)
    }

    @Test
    fun `generate yields unique payloads with requested prefix`() {
        val a = AdbQrPayload.generate(random = Random(42))
        val b = AdbQrPayload.generate(random = Random(43))
        assertTrue(a.serviceName.startsWith("droidkit-"))
        assertTrue(b.serviceName.startsWith("droidkit-"))
        assertNotEquals(a, b)
        assertEquals(10, a.secret.length)
    }

    @Test
    fun `generated payload round trips through encode and parse`() {
        val payload = AdbQrPayload.generate(random = Random(7))
        assertEquals(payload, AdbQrPayload.parse(payload.encode()))
    }
}

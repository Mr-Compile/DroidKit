package io.github.droidkit.feature.nfc

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NfcTagParserTest {

    @Test
    fun `hex renders bytes or null`() {
        assertEquals("04 A3 1F", NfcTagParser.hex(byteArrayOf(0x04, 0xA3.toByte(), 0x1F)))
        assertNull(NfcTagParser.hex(null))
        assertNull(NfcTagParser.hex(ByteArray(0)))
    }

    @Test
    fun `text record decodes utf8 with language length`() {
        // status=0x02 (UTF-8, lang len 2) + "en" + "Hello"
        val payload = byteArrayOf(0x02) + "en".toByteArray() + "Hello".toByteArray()
        assertEquals("Hello", NfcTagParser.decodeTextRecord(payload))
    }

    @Test
    fun `text record decodes utf16 flag`() {
        val text = "Hi"
        val payload = byteArrayOf(0x82.toByte()) + "en".toByteArray() +
            text.toByteArray(Charsets.UTF_16)
        assertEquals("Hi", NfcTagParser.decodeTextRecord(payload))
    }

    @Test
    fun `text record rejects truncated or empty`() {
        assertNull(NfcTagParser.decodeTextRecord(ByteArray(0)))
        assertNull(NfcTagParser.decodeTextRecord(byteArrayOf(0x05, 'e'.code.toByte())))
    }

    @Test
    fun `uri record applies prefix table`() {
        // 0x04 = https:// + "example.com"
        val payload = byteArrayOf(0x04) + "example.com".toByteArray()
        assertEquals("https://example.com", NfcTagParser.decodeUriRecord(payload))
    }

    @Test
    fun `uri record rejects unknown prefix`() {
        val payload = byteArrayOf(0x7F) + "x".toByteArray()
        assertNull(NfcTagParser.decodeUriRecord(payload))
    }

    @Test
    fun `textRecordPayload builds status byte plus language`() {
        val payload = NfcTagParser.textRecordPayload("Hi", "en")
        assertArrayEquals(
            byteArrayOf(0x02) + "en".toByteArray() + "Hi".toByteArray(),
            payload,
        )
        // round-trip through the decoder
        assertEquals("Hi", NfcTagParser.decodeTextRecord(payload))
    }

    @Test
    fun `tnf and tech names`() {
        assertEquals("Well-known", NfcTagParser.tnfName(1))
        assertEquals("Empty", NfcTagParser.tnfName(0.toShort()))
        assertEquals("Ndef", NfcTagParser.techSimpleName("android.nfc.tech.Ndef"))
    }
}

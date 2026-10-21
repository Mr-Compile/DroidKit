package io.github.droidkit.feature.nfc

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NdefRecordSpecTest {

    private fun tlv(id: Int, value: ByteArray) = byteArrayOf(
        (id shr 8).toByte(), id.toByte(),
        (value.size shr 8).toByte(), value.size.toByte(),
    ) + value

    @Test
    fun `wifi payload builds credential container tlv`() {
        val p = wifiCredentialPayload("HomeNet", WifiSecurity.WPA2_PSK, "secret123")
        // outer attr = 0x100E credential
        assertEquals(0x10.toByte(), p[0]); assertEquals(0x0E.toByte(), p[1])
        val innerLen = ((p[2].toInt() and 0xFF) shl 8) or (p[3].toInt() and 0xFF)
        assertEquals(p.size - 4, innerLen)

        var expected = ByteArray(0)
        expected += tlv(0x1026, byteArrayOf(0x01))
        expected += tlv(0x1045, "HomeNet".toByteArray())
        expected += tlv(0x1003, byteArrayOf(0x00, 0x20)) // WPA2
        expected += tlv(0x100F, byteArrayOf(0x00, 0x08)) // AES
        expected += tlv(0x1020, ByteArray(6) { 0xFF.toByte() })
        expected += tlv(0x1027, "secret123".toByteArray())
        assertArrayEquals(expected, p.copyOfRange(4, p.size))
    }

    @Test
    fun `wifi open network omits key attribute`() {
        val p = wifiCredentialPayload("Cafe", WifiSecurity.OPEN, "ignored")
        val inner = p.copyOfRange(4, p.size)
        // no 0x1027 key attr present
        var hasKey = false
        var i = 0
        while (i + 4 <= inner.size) {
            val id = ((inner[i].toInt() and 0xFF) shl 8) or (inner[i + 1].toInt() and 0xFF)
            val len = ((inner[i + 2].toInt() and 0xFF) shl 8) or (inner[i + 3].toInt() and 0xFF)
            if (id == 0x1027) hasKey = true
            i += 4 + len
        }
        assertTrue(!hasKey)
    }

    @Test
    fun `wsc payload round-trips through decoder`() {
        val p = wifiCredentialPayload("Office5G", WifiSecurity.WPA_PSK, "key")
        assertEquals(
            Triple("Office5G", "WPA", true),
            NfcTagParser.decodeWsc(p),
        )
    }

    @Test
    fun `decodeWsc rejects malformed and non-credential payloads`() {
        assertEquals(null, NfcTagParser.decodeWsc(ByteArray(0)))
        assertEquals(null, NfcTagParser.decodeWsc(byteArrayOf(0x10, 0x45, 0x00, 0x01, 0x41)))
        assertEquals(null, NfcTagParser.decodeWsc(byteArrayOf(0x10, 0x0E, 0x00, 0x50, 0x01)))
    }

    @Test
    fun `vcard emits v3 structure and escapes reserved chars`() {
        val card = vcard("Doe, Jane", "+15550100", "j@x.io", "Acme;Corp")
        assertTrue(card.startsWith("BEGIN:VCARD\r\nVERSION:3.0\r\n"))
        assertTrue(card.contains("FN:Doe Jane"))
        assertTrue(card.contains("TEL;TYPE=CELL:+15550100"))
        assertTrue(card.contains("EMAIL:j@x.io"))
        assertTrue(card.contains("ORG:Acme Corp"))
        assertTrue(card.endsWith("END:VCARD\r\n"))
    }

    @Test
    fun `vcard omits empty optional fields`() {
        val card = vcard("Bob", "", "", "")
        assertTrue(!card.contains("TEL"))
        assertTrue(!card.contains("EMAIL"))
        assertTrue(!card.contains("ORG"))
    }

    @Test
    fun `decodeVcard summarizes contact fields`() {
        assertEquals(
            "Jane Doe · +15550100 · jane@x.io",
            NfcTagParser.decodeVcard(
                vcard("Jane Doe", "+15550100", "jane@x.io", "").toByteArray(),
            ),
        )
        assertEquals(null, NfcTagParser.decodeVcard("not a card".toByteArray()))
    }

    @Test
    fun `hexToBytes parses loose and rejects malformed`() {
        assertArrayEquals(
            byteArrayOf(0x04, 0xA3.toByte(), 0x1F),
            NfcTagParser.hexToBytes("04 a3 1f"),
        )
        assertArrayEquals(ByteArray(0), NfcTagParser.hexToBytes(""))
        assertEquals(null, NfcTagParser.hexToBytes("0G"))
        assertEquals(null, NfcTagParser.hexToBytes("123")) // odd length
    }

    @Test
    fun `spec summaries describe content without building records`() {
        assertEquals("Call +15550100", NdefRecordSpec.Phone("+15550100").summary())
        assertEquals("Geo 1.0,2.0", NdefRecordSpec.Geo("1.0", "2.0").summary())
        assertEquals(
            "Wi-Fi \"Home\" (WPA2)",
            NdefRecordSpec.Wifi("Home", WifiSecurity.WPA2_PSK, "k").summary(),
        )
        assertEquals("Contact: Bob", NdefRecordSpec.Contact("Bob", "", "", "").summary())
        assertEquals("App: com.x", NdefRecordSpec.App("com.x").summary())
        assertEquals("Link: https://a.io", NdefRecordSpec.Uri("https://a.io").summary())
    }

    @Test
    fun `type names match picker tiles`() {
        assertEquals("Wi-Fi", NdefRecordSpec.Wifi("s", WifiSecurity.OPEN, "").typeName)
        assertEquals("MIME", NdefRecordSpec.Mime("text/plain", "x").typeName)
        assertEquals("Raw", NdefRecordSpec.Raw(1, "", "").typeName)
    }
}

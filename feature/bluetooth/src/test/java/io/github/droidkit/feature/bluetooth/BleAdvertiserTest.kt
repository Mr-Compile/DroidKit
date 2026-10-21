package io.github.droidkit.feature.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BleAdvertiserTest {

    private fun ad(type: Int, vararg data: Int): ByteArray =
        byteArrayOf((data.size + 1).toByte(), type.toByte()) +
            data.map { it.toByte() }.toByteArray()

    @Test
    fun `decodes complete name`() {
        val bytes = ad(0x09, *"Beacon-1".toByteArray().map { it.toInt() }.toIntArray())
        val info = BleAdvertiser.decode(bytes)
        assertEquals("Beacon-1", info.name)
        assertTrue(info.fields.any { it.first == "Complete name" && it.second == "Beacon-1" })
    }

    @Test
    fun `decodes flags`() {
        val info = BleAdvertiser.decode(ad(0x01, 0x06))
        val flags = info.fields.first { it.first == "Flags" }.second
        assertTrue(flags.contains("General Discoverable"))
        assertTrue(flags.contains("BR/EDR not supported"))
    }

    @Test
    fun `decodes 16-bit service uuids with names`() {
        // AD_UUID16_COMPLETE with 0x180F (battery) + 0x180A (device info)
        val info = BleAdvertiser.decode(ad(0x03, 0x0F, 0x18, 0x0A, 0x18))
        val v = info.fields.first { it.first.startsWith("Service UUIDs") }.second
        assertTrue(v.contains("Battery"))
        assertTrue(v.contains("Device Information"))
    }

    @Test
    fun `decodes tx power`() {
        val info = BleAdvertiser.decode(ad(0x0A, 0xF4)) // -12 dBm
        assertEquals("-12 dBm", info.fields.first { it.first == "TX power" }.second)
    }

    @Test
    fun `decodes manufacturer data with company name`() {
        // mfg: company 0x004C (Apple, little-endian) + payload
        val info = BleAdvertiser.decode(ad(0xFF, 0x4C, 0x00, 0x02, 0x15))
        val label = info.fields.first().first
        assertTrue(label.contains("Apple"))
    }

    @Test
    fun `decodes ibeacon inside apple manufacturer data`() {
        // iBeacon: type 0x02 len 0x15 + 16B uuid + major + minor + tx
        val payload = intArrayOf(
            0x4C, 0x00, // Apple company id LE
            0x02, 0x15, // iBeacon subtype + length
            0xE2, 0xC5, 0x6D, 0xB5, 0xDF, 0xFB, 0x48, 0xD2,
            0xB0, 0x60, 0xD0, 0xF5, 0xA7, 0x10, 0x96, 0xE0,
            0x00, 0x01, // major 1
            0x00, 0x02, // minor 2
            0xC5, // tx -59
        )
        val info = BleAdvertiser.decode(ad(0xFF, *payload))
        assertEquals("iBeacon", info.beaconHint)
        val ibeacon = info.fields.first { it.first == "iBeacon" }.second
        assertTrue(ibeacon.contains("e2c56db5-dffb-48d2-b060-d0f5a71096e0"))
        assertTrue(ibeacon.contains("major=1"))
        assertTrue(ibeacon.contains("minor=2"))
        assertTrue(ibeacon.contains("tx=-59"))
    }

    @Test
    fun `stops cleanly on truncated structure`() {
        val bytes = ad(0x09, *"OK".toByteArray().map { it.toInt() }.toIntArray()) +
            byteArrayOf(0x10, 0x09, 'x'.code.toByte()) // claims 16 bytes, has 1
        val info = BleAdvertiser.decode(bytes)
        assertEquals("OK", info.name)
        assertEquals(1, info.fields.size)
    }

    @Test
    fun `decodes service data`() {
        val info = BleAdvertiser.decode(ad(0x16, 0xAA, 0xFE, 0x10, 0x20)) // Eddystone svc data
        val (k, v) = info.fields.first { it.first.startsWith("Service data") }
        assertTrue(k.contains("Eddystone"))
        assertEquals("10 20", v)
    }

    @Test
    fun `handles empty and null input`() {
        assertEquals(emptyList<Pair<String, String>>(), BleAdvertiser.decode(null).fields)
        assertEquals(emptyList<Pair<String, String>>(), BleAdvertiser.decode(ByteArray(0)).fields)
        assertNull(BleAdvertiser.decode(byteArrayOf(0x00)).name)
    }

    @Test
    fun `uuidName labels known and unknown`() {
        assertTrue(BleAdvertiser.uuidName(0x180F).contains("Battery"))
        assertEquals("0x9999", BleAdvertiser.uuidName(0x9999))
    }
}

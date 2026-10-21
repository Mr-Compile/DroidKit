package io.github.droidkit.feature.bluetooth

import java.util.UUID

/**
 * Decoded view of one BLE scan record. All parsing is defensive — a
 * truncated or malformed structure stops decoding rather than failing.
 */
data class AdvertisementInfo(
    val name: String?,
    /** Human-readable "Label: value" rows in wire order. */
    val fields: List<Pair<String, String>>,
    /** "iBeacon"/"Eddystone" style hint when a known layout is detected. */
    val beaconHint: String?,
)

/**
 * BLE advertisement (AD structure) decoder — pure Kotlin, unit-testable.
 * Wire format: repeating [len][type][len-1 bytes of data]. Covers the
 * common AD types plus iBeacon detection inside manufacturer data.
 */
object BleAdvertiser {

    // AD structure type codes
    private const val AD_FLAGS = 0x01
    private const val AD_UUID16_INCOMPLETE = 0x02
    private const val AD_UUID16_COMPLETE = 0x03
    private const val AD_UUID32_INCOMPLETE = 0x04
    private const val AD_UUID32_COMPLETE = 0x05
    private const val AD_UUID128_INCOMPLETE = 0x06
    private const val AD_UUID128_COMPLETE = 0x07
    private const val AD_NAME_SHORT = 0x08
    private const val AD_NAME_COMPLETE = 0x09
    private const val AD_TX_POWER = 0x0A
    private const val AD_SERVICE_DATA16 = 0x16
    private const val AD_APPEARANCE = 0x19
    private const val AD_SERVICE_DATA32 = 0x20
    private const val AD_SERVICE_DATA128 = 0x21
    private const val AD_URI = 0x24
    private const val AD_MFG = 0xFF

    /** Well-known 16-bit service UUIDs — Bluetooth SIG assigned numbers. */
    val SERVICE_NAMES = mapOf(
        0x1800 to "Generic Access", 0x1801 to "Generic Attribute",
        0x1802 to "Immediate Alert", 0x1805 to "Current Time",
        0x1808 to "Glucose", 0x1809 to "Health Thermometer",
        0x180A to "Device Information", 0x180D to "Heart Rate",
        0x180E to "Phone Alert Status", 0x180F to "Battery",
        0x1810 to "Blood Pressure", 0x1812 to "HID",
        0x1816 to "Cycling Speed & Cadence", 0x1818 to "Cycling Power",
        0x1819 to "Location & Navigation", 0x181C to "User Data",
        0x181D to "Weight Scale", 0x181E to "Bond Management",
        0x181F to "Continuous Glucose", 0x1822 to "Pulse Oximeter",
        0x1825 to "Object Transfer", 0x183A to "Mesh Provisioning",
        0x183B to "Mesh Proxy", 0xFEAA to "Eddystone (Google)",
        0xFE9F to "Google", 0xFE2C to "Fast Pair (Google)",
        0xFD6F to "Exposure Notification", 0xFD64 to "Samsung Companion",
        0xFD5A to "Samsung Wearable", 0xFEBE to "Bose",
        0xFE78 to "HP", 0xFE0F to "Philips",
    )

    /** Common Bluetooth SIG company identifiers — best-effort labels. */
    val COMPANY_NAMES = mapOf(
        0x0004 to "Toshiba", 0x0006 to "Microsoft", 0x000A to "Qualcomm",
        0x000F to "Broadcom", 0x004C to "Apple", 0x0059 to "Nordic Semiconductor",
        0x0075 to "Samsung", 0x00E0 to "Google", 0x0131 to "Cypress",
        0x0157 to "Anhui Huami (Mi)", 0x02D5 to "Nordic Semiconductor (legacy)",
        0x02E5 to "Espressif", 0x0399 to "Nintendo", 0x013A to "Tencent",
        0x03C2 to "Snapchat", 0x0499 to "Ruuvi", 0x02E0 to "Google (legacy)",
    )

    fun decode(bytes: ByteArray?): AdvertisementInfo {
        if (bytes == null || bytes.isEmpty()) {
            return AdvertisementInfo(null, emptyList(), null)
        }
        val fields = mutableListOf<Pair<String, String>>()
        var name: String? = null
        var beacon: String? = null
        var pos = 0
        while (pos < bytes.size) {
            val len = bytes[pos].toInt() and 0xFF
            if (len == 0) break // padding / terminator
            if (pos + 1 + len > bytes.size) break // truncated — stop cleanly
            val type = bytes[pos + 1].toInt() and 0xFF
            val data = bytes.copyOfRange(pos + 2, pos + 1 + len)
            when (type) {
                AD_FLAGS -> if (data.isNotEmpty()) {
                    fields += "Flags" to flagNames(data[0].toInt() and 0xFF)
                }
                AD_UUID16_INCOMPLETE, AD_UUID16_COMPLETE ->
                    fields += "Service UUIDs (16-bit" +
                        (if (type == AD_UUID16_INCOMPLETE) ", incomplete" else "") + ")" to
                        uuid16List(data)
                AD_UUID32_INCOMPLETE, AD_UUID32_COMPLETE ->
                    fields += "Service UUIDs (32-bit)" to uuid32List(data)
                AD_UUID128_INCOMPLETE, AD_UUID128_COMPLETE ->
                    fields += "Service UUIDs (128-bit)" to uuid128List(data)
                AD_NAME_SHORT, AD_NAME_COMPLETE -> {
                    val n = runCatching { String(data, Charsets.UTF_8) }
                        .getOrNull()?.ifBlank { null }
                    if (n != null) {
                        name = n
                        fields += (if (type == AD_NAME_COMPLETE) "Complete name" else "Short name") to n
                    }
                }
                AD_TX_POWER -> if (data.isNotEmpty()) {
                    fields += "TX power" to "${data[0].toInt()} dBm"
                }
                AD_APPEARANCE -> if (data.size >= 2) {
                    fields += "Appearance" to
                        "0x%04X".format(le16(data, 0))
                }
                AD_SERVICE_DATA16 -> if (data.size >= 2) {
                    val uuid = le16(data, 0)
                    fields += "Service data (${uuidName(uuid)})" to
                        hex(data.copyOfRange(2, data.size))
                }
                AD_SERVICE_DATA32 -> if (data.size >= 4) {
                    fields += "Service data (0x%08X)".format(le32(data, 0)) to
                        hex(data.copyOfRange(4, data.size))
                }
                AD_SERVICE_DATA128 -> if (data.size >= 16) {
                    fields += "Service data" to
                        "${uuid128(data)} — ${hex(data.copyOfRange(16, data.size))}"
                }
                AD_URI -> fields += "URI" to String(data, Charsets.UTF_8)
                AD_MFG -> if (data.size >= 2) {
                    val company = le16(data, 0)
                    val label = "Manufacturer (" + (COMPANY_NAMES[company] ?: "0x%04X".format(company)) + ")"
                    val payload = data.copyOfRange(2, data.size)
                    fields += label to hex(payload)
                    if (company == 0x004C && payload.size >= 2) {
                        decodeAppleSubType(payload)?.let { fields += it; beacon = "iBeacon" }
                    }
                }
            }
            pos += 1 + len
        }
        return AdvertisementInfo(name, fields, beacon)
    }

    private fun flagNames(f: Int): String {
        val names = buildList {
            if (f and 0x01 != 0) add("LE Limited Discoverable")
            if (f and 0x02 != 0) add("LE General Discoverable")
            if (f and 0x04 != 0) add("BR/EDR not supported")
            if (f and 0x08 != 0) add("LE+BR/EDR controller")
            if (f and 0x10 != 0) add("LE+BR/EDR host")
        }
        return if (names.isEmpty()) "0x%02X".format(f) else names.joinToString(", ")
    }

    private fun uuid16List(data: ByteArray): String =
        (0 until data.size / 2).joinToString(", ") { i ->
            uuidName(le16(data, i * 2))
        }

    private fun uuid32List(data: ByteArray): String =
        (0 until data.size / 4).joinToString(", ") { i ->
            "0x%08X".format(le32(data, i * 4))
        }

    private fun uuid128List(data: ByteArray): String =
        (0 until data.size / 16).joinToString(", ") { i ->
            uuid128(data.copyOfRange(i * 16, i * 16 + 16))
        }

    /** 16-bit service UUID → "Name (0x180F)" or bare hex. */
    fun uuidName(uuid: Int): String =
        SERVICE_NAMES[uuid]?.let { "$it (0x%04X)".format(uuid) } ?: "0x%04X".format(uuid)

    /** Apple iBeacon subtype (0x02, len 0x15): UUID + major + minor + txPower. */
    private fun decodeAppleSubType(payload: ByteArray): Pair<String, String>? {
        if (payload[0].toInt() and 0xFF != 0x02 || payload.size < 23) return null
        if (payload[1].toInt() and 0xFF != 0x15) return null
        val uuidBytes = payload.copyOfRange(2, 18)
        val major = ((payload[18].toInt() and 0xFF) shl 8) or (payload[19].toInt() and 0xFF)
        val minor = ((payload[20].toInt() and 0xFF) shl 8) or (payload[21].toInt() and 0xFF)
        val tx = payload[22].toInt()
        return "iBeacon" to "uuid=${uuidFromBigEndian(uuidBytes)} major=$major minor=$minor tx=$tx"
    }

    fun hex(data: ByteArray): String =
        data.joinToString(" ") { "%02X".format(it) }

    private fun le16(d: ByteArray, off: Int): Int =
        (d[off].toInt() and 0xFF) or ((d[off + 1].toInt() and 0xFF) shl 8)

    private fun le32(d: ByteArray, off: Int): Long =
        le16(d, off).toLong() or (le16(d, off + 2).toLong() shl 16)

    /** BLE UUID bytes are little-endian on the wire — reverse into standard text form. */
    private fun uuid128(d: ByteArray): String {
        val r = d.reversedArray()
        return UUID(be64(r, 0), be64(r, 8)).toString()
    }

    private fun be64(d: ByteArray, off: Int): Long {
        var v = 0L
        for (i in 0 until 8) v = (v shl 8) or (d[off + i].toLong() and 0xFF)
        return v
    }

    /** iBeacon UUIDs are stored network-order (big-endian), unlike BLE service UUIDs. */
    private fun uuidFromBigEndian(d: ByteArray): String =
        UUID(be64(d, 0), be64(d, 8)).toString()
}

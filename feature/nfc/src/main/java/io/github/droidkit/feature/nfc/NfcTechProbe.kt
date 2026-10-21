package io.github.droidkit.feature.nfc

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import java.io.IOException

/** One technology's read-only detail section for the inspector card. */
data class TechSection(
    val tech: String,
    val fields: List<Pair<String, String>>,
    val pageDumpHex: List<String> = emptyList(),
    val truncated: Boolean = false,
)

/**
 * Read-only per-technology probing. Only framework getters plus
 * `MifareUltralight.readPages` (unprotected pages) — never key auth,
 * never writes. Everything is runCatching-guarded: partial data is
 * reported, never fabricated.
 */
object NfcTechProbe {

    /** Max Ultralight pages attempted — stops earlier on read failure. */
    const val ULTRALIGHT_MAX_PAGES = 64

    fun probe(tag: Tag): List<TechSection> = tag.techList.mapNotNull { qualified ->
        runCatching { sectionFor(tag, qualified) }.getOrNull()
    }

    private fun sectionFor(tag: Tag, qualified: String): TechSection? = when (qualified) {
        "android.nfc.tech.NfcA" -> NfcA.get(tag)?.let {
            TechSection(
                "NfcA",
                listOf(
                    "ATQA" to (NfcTagParser.hex(it.atqa) ?: "—"),
                    "SAK" to "0x%02X".format(it.sak.toInt() and 0xFF),
                    "Max transceive" to "${it.maxTransceiveLength} bytes",
                    "Timeout" to "${it.timeout} ms",
                ),
            )
        }
        "android.nfc.tech.NfcB" -> NfcB.get(tag)?.let {
            TechSection(
                "NfcB",
                listOf(
                    "Application data" to (NfcTagParser.hex(it.applicationData) ?: "—"),
                    "Protocol info" to (NfcTagParser.hex(it.protocolInfo) ?: "—"),
                    "Max transceive" to "${it.maxTransceiveLength} bytes",
                ),
            )
        }
        "android.nfc.tech.NfcF" -> NfcF.get(tag)?.let {
            TechSection(
                "NfcF",
                listOf(
                    "Manufacturer" to (NfcTagParser.hex(it.manufacturer) ?: "—"),
                    "System code" to (NfcTagParser.hex(it.systemCode) ?: "—"),
                    "Max transceive" to "${it.maxTransceiveLength} bytes",
                ),
            )
        }
        "android.nfc.tech.NfcV" -> NfcV.get(tag)?.let {
            TechSection(
                "NfcV",
                listOf(
                    "DSFID" to "0x%02X".format(it.dsfId.toInt() and 0xFF),
                    "Response flags" to "0x%02X".format(it.responseFlags.toInt() and 0xFF),
                    "Max transceive" to "${it.maxTransceiveLength} bytes",
                ),
            )
        }
        "android.nfc.tech.IsoDep" -> IsoDep.get(tag)?.let {
            TechSection(
                "IsoDep",
                listOf(
                    "Historical bytes" to (NfcTagParser.hex(it.historicalBytes) ?: "—"),
                    "Hi-layer response" to (NfcTagParser.hex(it.hiLayerResponse) ?: "—"),
                    "Extended APDU" to it.isExtendedLengthApduSupported.toString(),
                    "Max transceive" to "${it.maxTransceiveLength} bytes",
                ),
            )
        }
        "android.nfc.tech.MifareClassic" -> MifareClassic.get(tag)?.let {
            TechSection(
                "MifareClassic",
                listOf(
                    "Type" to classicTypeName(it.type),
                    "Sectors" to it.sectorCount.toString(),
                    "Blocks" to it.blockCount.toString(),
                    "Size" to "${it.size} bytes",
                    "Sectors data" to "key-protected — not read",
                ),
            )
        }
        "android.nfc.tech.MifareUltralight" -> MifareUltralight.get(tag)?.let { probeUltralight(it) }
        else -> null
    }

    fun classicTypeName(type: Int): String = when (type) {
        MifareClassic.TYPE_CLASSIC -> "Classic"
        MifareClassic.TYPE_PLUS -> "Plus"
        MifareClassic.TYPE_PRO -> "Pro"
        else -> "Unknown ($type)"
    }

    fun ultralightTypeName(type: Int): String = when (type) {
        MifareUltralight.TYPE_ULTRALIGHT -> "Ultralight"
        MifareUltralight.TYPE_ULTRALIGHT_C -> "Ultralight C"
        else -> "Unknown ($type)"
    }

    /**
     * Reads pages in 4-page chunks (one readPages call = 16 bytes) until the
     * tag stops responding or [ULTRALIGHT_MAX_PAGES] is reached. Locked or
     * tag-family-different regions surface as a truncated dump — honest, not
     * an error.
     */
    private fun probeUltralight(ul: MifareUltralight): TechSection {
        val pages = mutableListOf<String>()
        var truncated = false
        val fields = mutableListOf("Type" to ultralightTypeName(ul.type))
        runCatching {
            ul.connect()
            try {
                var offset = 0
                while (offset < ULTRALIGHT_MAX_PAGES) {
                    try {
                        val block = ul.readPages(offset)
                        for (p in 0 until 4) {
                            val from = p * 4
                            pages += "p${offset + p}: " +
                                block.sliceArray(from until from + 4)
                                    .joinToString(" ") { "%02X".format(it) }
                        }
                        offset += 4
                    } catch (e: IOException) {
                        truncated = offset > 0
                        break
                    }
                }
                fields += "Pages read" to "${pages.size}"
            } finally {
                runCatching { ul.close() }
            }
        }.onFailure {
            fields += "Pages read" to "unavailable (${it.javaClass.simpleName})"
            truncated = true
        }
        if (pages.size >= ULTRALIGHT_MAX_PAGES) truncated = true
        return TechSection("MifareUltralight", fields, pages, truncated)
    }
}

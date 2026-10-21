package io.github.droidkit.feature.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable

data class NdefRecordInfo(
    val tnfName: String,
    val typeHex: String,
    val payloadHex: String,
    val decoded: String?,
    val decodedKind: String?,
)

data class NfcTagInfo(
    val idHex: String?,
    val techList: List<String>,
    val ndefCapable: Boolean,
    val ndefWritable: Boolean?,
    val ndefMaxSize: Int?,
    val ndefType: String?,
    val canMakeReadOnly: Boolean?,
    val formatable: Boolean,
    val records: List<NdefRecordInfo>,
    val parseError: String?,
    /** Raw message retained in memory for the duplicate-tag flow; never persisted. */
    val rawMessage: NdefMessage? = null,
)

/** Tag/NDEF parsing and record construction — pure decoders are unit-testable. */
object NfcTagParser {

    fun hex(bytes: ByteArray?): String? =
        bytes?.takeIf { it.isNotEmpty() }?.joinToString(" ") { "%02X".format(it) }

    fun techSimpleName(qualifiedName: String): String = qualifiedName.substringAfterLast('.')

    fun tnfName(tnf: Short): String = when (tnf) {
        NdefRecord.TNF_EMPTY -> "Empty"
        NdefRecord.TNF_WELL_KNOWN -> "Well-known"
        NdefRecord.TNF_MIME_MEDIA -> "MIME media"
        NdefRecord.TNF_ABSOLUTE_URI -> "Absolute URI"
        NdefRecord.TNF_EXTERNAL_TYPE -> "External"
        NdefRecord.TNF_UNKNOWN -> "Unknown"
        NdefRecord.TNF_UNCHANGED -> "Unchanged"
        else -> "Reserved"
    }

    private val URI_PREFIXES = mapOf(
        0x01 to "http://www.", 0x02 to "https://www.", 0x03 to "http://", 0x04 to "https://",
        0x05 to "tel:", 0x06 to "mailto:", 0x07 to "ftp://anonymous:anonymous@",
        0x08 to "ftp://ftp.", 0x09 to "ftps://", 0x0A to "sftp://", 0x0B to "smb://",
        0x0C to "nfs://", 0x0D to "ftp://", 0x0E to "dav://", 0x0F to "news:",
        0x10 to "telnet://", 0x11 to "imap:", 0x12 to "rtsp://", 0x13 to "urn:",
        0x14 to "pop:", 0x15 to "sip:", 0x16 to "sips:", 0x17 to "tftp:",
        0x18 to "btspp://", 0x19 to "btl2cap://", 0x1A to "btgoep://", 0x1B to "tcpobex://",
        0x1C to "irdaobex://", 0x1D to "file://", 0x1E to "urn:epc:id:", 0x1F to "urn:epc:tag:",
        0x20 to "urn:epc:pat:", 0x21 to "urn:epc:raw:", 0x22 to "urn:epc:", 0x23 to "urn:nfc:",
    )

    /** RTD_TEXT: byte0 = UTF-16 flag (bit 7) + language length (bits 5..0). */
    fun decodeTextRecord(payload: ByteArray): String? {
        if (payload.isEmpty()) return null
        val status = payload[0].toInt() and 0xFF
        val utf16 = status and 0x80 != 0
        val langLen = status and 0x3F
        if (payload.size < 1 + langLen) return null
        return runCatching {
            String(
                payload, 1 + langLen, payload.size - 1 - langLen,
                if (utf16) Charsets.UTF_16 else Charsets.UTF_8,
            )
        }.getOrNull()
    }

    /** RTD_URI: byte0 = prefix-table index, remainder = UTF-8 suffix. */
    fun decodeUriRecord(payload: ByteArray): String? {
        if (payload.isEmpty()) return null
        val prefix = URI_PREFIXES[payload[0].toInt() and 0xFF] ?: return null
        return runCatching { prefix + String(payload, 1, payload.size - 1, Charsets.UTF_8) }
            .getOrNull()
    }

    /** Loose hex ("04 A3" / "04a3") → bytes; null on malformed input. */
    fun hexToBytes(input: String): ByteArray? {
        val clean = input.filter { !it.isWhitespace() }
        if (clean.isEmpty()) return ByteArray(0)
        if (clean.length % 2 != 0 || clean.any { it !in '0'..'9' && it !in 'a'..'f' && it !in 'A'..'F' }) {
            return null
        }
        return ByteArray(clean.length / 2) { i ->
            ((Character.digit(clean[i * 2], 16) shl 4) or Character.digit(clean[i * 2 + 1], 16)).toByte()
        }
    }

    /** Record type field as printable ASCII (MIME/external types), or null. */
    fun typeAsAscii(record: NdefRecord): String? =
        record.type?.takeIf { it.isNotEmpty() }
            ?.let { t -> String(t, Charsets.US_ASCII).takeIf { s -> s.all { it in ' '..'~' } } }

    /** WSC TLV walker → (ssid, authLabel, hasKey); null on malformed payload. */
    fun decodeWsc(payload: ByteArray): Triple<String, String, Boolean>? {
        fun tlvAt(b: ByteArray, pos: Int): Triple<Int, Int, Int>? {
            if (pos + 4 > b.size) return null
            val id = ((b[pos].toInt() and 0xFF) shl 8) or (b[pos + 1].toInt() and 0xFF)
            val len = ((b[pos + 2].toInt() and 0xFF) shl 8) or (b[pos + 3].toInt() and 0xFF)
            if (pos + 4 + len > b.size) return null
            return Triple(id, pos + 4, pos + 4 + len)
        }
        val outer = tlvAt(payload, 0) ?: return null
        if (outer.first != 0x100E) return null
        var ssid: String? = null
        var authLabel: String? = null
        var hasKey = false
        var pos = outer.second
        while (pos < outer.third) {
            val (id, vStart, vEnd) = tlvAt(payload, pos) ?: return null
            when (id) {
                0x1045 -> ssid = String(payload, vStart, vEnd - vStart, Charsets.UTF_8)
                0x1003 -> if (vEnd - vStart >= 2) {
                    authLabel = when (
                        ((payload[vStart].toInt() and 0xFF) shl 8) or
                            (payload[vStart + 1].toInt() and 0xFF)
                    ) {
                        0x0001 -> "Open"; 0x0002 -> "WPA"; 0x0004 -> "WEP/Shared"
                        0x0020 -> "WPA2"; else -> "Other"
                    }
                }
                0x1027 -> hasKey = true
            }
            pos = vEnd
        }
        return Triple(ssid ?: "(hidden)", authLabel ?: "unknown", hasKey)
    }

    /** vCard → "name · phone · email" summary. */
    fun decodeVcard(payload: ByteArray): String? {
        val text = runCatching { String(payload, Charsets.UTF_8) }.getOrNull() ?: return null
        if (!text.contains("BEGIN:VCARD", ignoreCase = true)) return null
        fun field(vararg names: String): String? = text.lineSequence()
            .map { it.substringBefore(';').substringBefore(':').uppercase() to it.substringAfter(':', "") }
            .firstOrNull { (k, _) -> k in names.map(String::uppercase) }?.second?.trim()
            ?.takeIf { it.isNotEmpty() }
        return listOfNotNull(field("FN", "N"), field("TEL"), field("EMAIL"))
            .joinToString(" · ").takeIf { it.isNotEmpty() }
    }

    /** Smart Poster: payload is a nested NDEF message (title + URI). */
    fun decodeSmartPoster(payload: ByteArray): String? {
        val nested = runCatching { NdefMessage(payload) }.getOrNull() ?: return null
        var title: String? = null
        var uri: String? = null
        for (r in nested.records) {
            val p = r.payload ?: continue
            when {
                r.tnf == NdefRecord.TNF_WELL_KNOWN &&
                    r.type?.contentEquals(NdefRecord.RTD_TEXT) == true ->
                    title = decodeTextRecord(p)
                r.tnf == NdefRecord.TNF_WELL_KNOWN &&
                    r.type?.contentEquals(NdefRecord.RTD_URI) == true ->
                    uri = decodeUriRecord(p)
            }
        }
        return listOfNotNull(title, uri).joinToString(" — ").takeIf { it.isNotEmpty() }
    }

    fun decodeRecord(record: NdefRecord): NdefRecordInfo {
        var decoded: String? = null
        var kind: String? = null
        val payload = record.payload
        when (record.tnf) {
            NdefRecord.TNF_WELL_KNOWN -> if (payload != null) {
                when {
                    record.type?.contentEquals(NdefRecord.RTD_TEXT) == true -> {
                        decoded = decodeTextRecord(payload); kind = "text"
                    }
                    record.type?.contentEquals(NdefRecord.RTD_URI) == true -> {
                        decoded = decodeUriRecord(payload); kind = "uri"
                    }
                    record.type?.contentEquals(NdefRecord.RTD_SMART_POSTER) == true -> {
                        decoded = decodeSmartPoster(payload) ?: "Smart Poster (nested records)"
                        kind = "smart-poster"
                    }
                }
            }
            NdefRecord.TNF_MIME_MEDIA -> {
                val mime = typeAsAscii(record)
                if (payload != null) {
                    when (mime?.lowercase()) {
                        "text/x-vcard", "text/vcard" -> {
                            decoded = decodeVcard(payload) ?: "vCard"; kind = "vcard"
                        }
                        "application/vnd.wfa.wsc" -> {
                            decoded = decodeWsc(payload)?.let { (ssid, auth, hasKey) ->
                                "Wi-Fi \"$ssid\" ($auth)" + if (hasKey) " — key stored" else ""
                            } ?: "Wi-Fi credential"
                            kind = "wifi"
                        }
                        else -> {
                            decoded = mime?.let { "MIME $it (${payload.size} bytes)" }
                            kind = "mime"
                        }
                    }
                }
            }
            NdefRecord.TNF_EXTERNAL_TYPE -> {
                val ext = typeAsAscii(record)
                decoded = if (ext?.lowercase() == "android.com:pkg") {
                    "App: ${payload?.let { String(it, Charsets.US_ASCII) } ?: "?"}"
                } else {
                    ext?.let { "External $it" }
                }
                kind = "external"
            }
            NdefRecord.TNF_ABSOLUTE_URI -> {
                decoded = typeAsAscii(record); kind = "uri"
            }
            NdefRecord.TNF_EMPTY -> {
                decoded = "(empty record)"; kind = "empty"
            }
        }
        return NdefRecordInfo(
            tnfName = tnfName(record.tnf),
            typeHex = hex(record.type) ?: "—",
            payloadHex = hex(record.payload) ?: "—",
            decoded = decoded,
            decodedKind = kind,
        )
    }

    fun parse(tag: Tag): NfcTagInfo {
        var error: String? = null
        var records = emptyList<NdefRecordInfo>()
        var writable: Boolean? = null
        var maxSize: Int? = null
        var ndefType: String? = null
        var makeReadOnly: Boolean? = null
        var message: NdefMessage? = null

        val ndef = Ndef.get(tag)
        if (ndef != null) {
            writable = runCatching { ndef.isWritable }.getOrNull()
            maxSize = runCatching { ndef.maxSize }.getOrNull()
            ndefType = runCatching { ndef.type }.getOrNull()
            makeReadOnly = runCatching { ndef.canMakeReadOnly() }.getOrNull()
            message = runCatching {
                ndef.cachedNdefMessage ?: run {
                    ndef.connect()
                    try { ndef.ndefMessage } finally { runCatching { ndef.close() } }
                }
            }.onFailure { error = it.message ?: it.javaClass.simpleName }.getOrNull()
            records = message?.records?.map { decodeRecord(it) }.orEmpty()
        }

        return NfcTagInfo(
            idHex = hex(tag.id),
            techList = tag.techList.map { techSimpleName(it) },
            ndefCapable = ndef != null,
            ndefWritable = writable,
            ndefMaxSize = maxSize,
            ndefType = ndefType,
            canMakeReadOnly = makeReadOnly,
            formatable = NdefFormatable.get(tag) != null,
            records = records,
            parseError = error,
            rawMessage = message,
        )
    }

    // ---- write helpers ----

    /** RTD_TEXT payload bytes — pure, unit-testable. */
    fun textRecordPayload(text: String, language: String = "en"): ByteArray {
        val lang = language.toByteArray(Charsets.US_ASCII)
        val body = text.toByteArray(Charsets.UTF_8)
        val payload = ByteArray(1 + lang.size + body.size)
        payload[0] = lang.size.toByte() // UTF-8, language length in low 6 bits
        lang.copyInto(payload, 1)
        body.copyInto(payload, 1 + lang.size)
        return payload
    }

    fun textRecord(text: String, language: String = "en"): NdefRecord =
        NdefRecord(
            NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT,
            ByteArray(0), textRecordPayload(text, language),
        )

    fun uriRecord(uri: String): NdefRecord = NdefRecord.createUri(uri)

    fun messageOf(record: NdefRecord): NdefMessage = NdefMessage(arrayOf(record))

    /** Empty-TNF message — the standard "erased" NDEF content. */
    fun emptyMessage(): NdefMessage =
        NdefMessage(arrayOf(NdefRecord(NdefRecord.TNF_EMPTY, null, null, null)))
}

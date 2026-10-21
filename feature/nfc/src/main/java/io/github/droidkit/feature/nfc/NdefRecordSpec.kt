package io.github.droidkit.feature.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord

/**
 * A record the user can compose on the write screen — holds field values,
 * produces a validated [NdefRecord]. Pure building blocks (no Android UI)
 * so every builder is unit-testable.
 */
sealed interface NdefRecordSpec {
    /** Short picker-tile label. */
    val typeName: String

    /** One-line preview for the message list. */
    fun summary(): String

    fun build(): NdefRecord

    data class Text(val text: String, val language: String = "en") : NdefRecordSpec {
        override val typeName = "Text"
        override fun summary() = "Text: ${text.take(40)}"
        override fun build() = NfcTagParser.textRecord(text, language)
    }

    data class Uri(val uri: String) : NdefRecordSpec {
        override val typeName = "Link"
        override fun summary() = "Link: $uri"
        override fun build() = NfcTagParser.uriRecord(uri)
    }

    data class Phone(val number: String) : NdefRecordSpec {
        override val typeName = "Phone"
        override fun summary() = "Call $number"
        override fun build() = NfcTagParser.uriRecord("tel:${number.trim()}")
    }

    data class Sms(val number: String, val body: String) : NdefRecordSpec {
        override val typeName = "SMS"
        override fun summary() = "SMS to $number"
        override fun build() = NfcTagParser.uriRecord(
            "sms:${number.trim()}" +
                if (body.isNotBlank()) "?body=${android.net.Uri.encode(body)}" else "",
        )
    }

    data class Email(val to: String, val subject: String, val body: String) : NdefRecordSpec {
        override val typeName = "Email"
        override fun summary() = "Email to $to"
        override fun build(): NdefRecord {
            val query = buildList {
                if (subject.isNotBlank()) add("subject=${android.net.Uri.encode(subject)}")
                if (body.isNotBlank()) add("body=${android.net.Uri.encode(body)}")
            }.joinToString("&")
            return NfcTagParser.uriRecord(
                "mailto:${to.trim()}" + if (query.isNotEmpty()) "?$query" else "",
            )
        }
    }

    data class Geo(val latitude: String, val longitude: String) : NdefRecordSpec {
        override val typeName = "Location"
        override fun summary() = "Geo $latitude,$longitude"
        override fun build() = NfcTagParser.uriRecord("geo:${latitude.trim()},${longitude.trim()}")
    }

    /** WiFi Simple Config credential — phones auto-offer to join on tap. */
    data class Wifi(val ssid: String, val security: WifiSecurity, val key: String) : NdefRecordSpec {
        override val typeName = "Wi-Fi"
        override fun summary() = "Wi-Fi \"$ssid\" (${security.label})"
        override fun build(): NdefRecord = NdefRecord.createMime(
            "application/vnd.wfa.wsc",
            wifiCredentialPayload(ssid, security, key),
        )
    }

    data class Contact(
        val name: String,
        val phone: String,
        val email: String,
        val org: String,
    ) : NdefRecordSpec {
        override val typeName = "Contact"
        override fun summary() = "Contact: $name"
        override fun build(): NdefRecord = NdefRecord.createMime(
            "text/x-vcard",
            vcard(name, phone, email, org).toByteArray(Charsets.UTF_8),
        )
    }

    /** Android Application Record — opens the app (or its store page) on tap. */
    data class App(val packageName: String) : NdefRecordSpec {
        override val typeName = "App"
        override fun summary() = "App: $packageName"
        override fun build(): NdefRecord = NdefRecord.createExternal(
            "android.com", "pkg", packageName.trim().toByteArray(Charsets.US_ASCII),
        )
    }

    data class Mime(val mimeType: String, val payload: String) : NdefRecordSpec {
        override val typeName = "MIME"
        override fun summary() = "$mimeType (${payload.length} chars)"
        override fun build(): NdefRecord =
            NdefRecord.createMime(mimeType.trim(), payload.toByteArray(Charsets.UTF_8))
    }

    /** Technician record — explicit TNF, type and payload bytes as hex. */
    data class Raw(val tnf: Short, val typeHex: String, val payloadHex: String) : NdefRecordSpec {
        override val typeName = "Raw"
        override fun summary() = "Raw TNF $tnf (${(payloadHex.length / 2)} bytes)"
        override fun build(): NdefRecord = NdefRecord(
            tnf,
            NfcTagParser.hexToBytes(typeHex) ?: ByteArray(0),
            null,
            NfcTagParser.hexToBytes(payloadHex) ?: ByteArray(0),
        )
    }
}

enum class WifiSecurity(val label: String, val authType: Int, val encType: Int) {
    OPEN("Open", 0x0001, 0x0001),
    WEP("WEP", 0x0004, 0x0002),
    WPA_PSK("WPA", 0x0002, 0x0004),
    WPA2_PSK("WPA2", 0x0020, 0x0008),
}

/** Append one WSC TLV attribute: 2-byte id + 2-byte length + value. */
private fun ByteArray.appendTlv(id: Int, value: ByteArray): ByteArray =
    this + byteArrayOf(
        (id shr 8).toByte(), id.toByte(),
        (value.size shr 8).toByte(), value.size.toByte(),
    ) + value

/**
 * WiFi Simple Config credential blob for `application/vnd.wfa.wsc` records.
 * Structure: 0x100E credential container holding network index, SSID,
 * auth type, encryption type, broadcast MAC and (when secured) network key.
 */
fun wifiCredentialPayload(ssid: String, security: WifiSecurity, key: String): ByteArray {
    var cred = ByteArray(0)
        .appendTlv(0x1026, byteArrayOf(0x01)) // network index
        .appendTlv(0x1045, ssid.toByteArray(Charsets.UTF_8)) // SSID
        .appendTlv(
            0x1003,
            byteArrayOf((security.authType shr 8).toByte(), security.authType.toByte()),
        )
        .appendTlv(
            0x100F,
            byteArrayOf((security.encType shr 8).toByte(), security.encType.toByte()),
        )
        .appendTlv(0x1020, ByteArray(6) { 0xFF.toByte() }) // broadcast MAC = any AP
    if (security != WifiSecurity.OPEN) {
        cred = cred.appendTlv(0x1027, key.toByteArray(Charsets.UTF_8)) // network key
    }
    return ByteArray(0).appendTlv(0x100E, cred)
}

/** Minimal vCard 3.0 — CR/LF and vCard-reserved chars neutralized. */
fun vcard(name: String, phone: String, email: String, org: String): String {
    fun esc(v: String) = v.replace(Regex("[\\s,;\\\\]+"), " ").trim()
    return buildString {
        append("BEGIN:VCARD\r\nVERSION:3.0\r\n")
        append("FN:").append(esc(name)).append("\r\n")
        if (phone.isNotBlank()) append("TEL;TYPE=CELL:").append(esc(phone)).append("\r\n")
        if (email.isNotBlank()) append("EMAIL:").append(esc(email)).append("\r\n")
        if (org.isNotBlank()) append("ORG:").append(esc(org)).append("\r\n")
        append("END:VCARD\r\n")
    }
}

fun ndefMessageOf(specs: List<NdefRecordSpec>): NdefMessage =
    NdefMessage(specs.map { it.build() }.toTypedArray())

/** URI preset chips for the Link record form. */
val LINK_PRESETS = listOf(
    "Web" to "https://",
    "X" to "https://x.com/",
    "Instagram" to "https://instagram.com/",
    "LinkedIn" to "https://linkedin.com/in/",
    "YouTube" to "https://youtube.com/@",
    "Facebook" to "https://facebook.com/",
)

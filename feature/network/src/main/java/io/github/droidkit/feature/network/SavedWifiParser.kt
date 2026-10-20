package io.github.droidkit.feature.network

/**
 * One saved Wi-Fi network recovered from a privileged config file.
 *
 * [psk] holds the stored secret verbatim. When [pskIsHex] the value is the
 * 64-char derived PSK — it is NOT the human passphrase and the UI labels it
 * "PSK (hex)". Secrets are display-only: never logged, persisted, exported,
 * or sent to AI context.
 */
data class SavedNetwork(
    val ssid: String,
    val security: String,
    val psk: String?,
    val pskIsHex: Boolean,
)

data class SavedWifiResult(
    /** Which store file produced the list, when identifiable. */
    val sourcePath: String?,
    val networks: List<SavedNetwork>,
)

/**
 * Parses `ReadWifiConfigStore` output — `== path` markers separate the
 * `WifiConfigStore.xml` (Android 10+, including the apexdata location) and
 * `wpa_supplicant.conf` (pre-10) sections. Tolerant by design: anything
 * unrecognised is skipped rather than guessed.
 */
object SavedWifiParser {

    private val NETWORK_XML_BLOCK = Regex("<Network>.*?</Network>", RegexOption.DOT_MATCHES_ALL)
    private val STRING_ENTRY = Regex("<string name=\"([A-Za-z0-9_]+)\">(.*?)</string>", RegexOption.DOT_MATCHES_ALL)
    private val CONF_BLOCK = Regex("network=\\{(.*?)\\}", RegexOption.DOT_MATCHES_ALL)
    private val CONFIG_KEY = Regex("^\"(.*)\"\\s*([A-Za-z0-9_\\-]+)$")
    private val KEY_VALUE = Regex("^([A-Za-z0-9_]+)=(.*)$")
    private val HEX_PSK = Regex("^[0-9a-fA-F]{64}$")

    fun parse(stdout: String): SavedWifiResult {
        val sections = splitSections(stdout)
        val networks = mutableListOf<SavedNetwork>()
        var source: String? = null
        sections.forEach { (path, body) ->
            val parsed = when {
                "<WifiConfiguration" in body || "<Network>" in body -> parseXmlStore(body)
                "network={" in body -> parseSupplicantConf(body)
                else -> emptyList()
            }
            if (parsed.isNotEmpty() && source == null) source = path
            networks += parsed
        }
        return SavedWifiResult(source, networks)
    }

    private data class FileSection(val path: String?, val body: String)

    /** Splits `== path`-marked output; unmarked input becomes one anonymous section. */
    private fun splitSections(stdout: String): List<FileSection> {
        val sections = mutableListOf<FileSection>()
        var path: String? = null
        var seenMarker = false
        val body = StringBuilder()
        fun flush() {
            if (body.isNotEmpty() || seenMarker) sections += FileSection(path, body.toString())
            body.setLength(0)
        }
        stdout.lineSequence().forEach { line ->
            if (line.startsWith("== ")) {
                if (seenMarker || body.isNotEmpty()) flush()
                seenMarker = true
                path = line.removePrefix("== ").trim()
            } else {
                body.appendLine(line)
            }
        }
        flush()
        return sections
    }

    // ---------- WifiConfigStore.xml ----------

    private fun parseXmlStore(body: String): List<SavedNetwork> =
        NETWORK_XML_BLOCK.findAll(body).mapNotNull { m -> parseXmlNetwork(m.value) }.toList()

    private fun parseXmlNetwork(block: String): SavedNetwork? {
        val entries = STRING_ENTRY.findAll(block)
            .associate { it.groupValues[1] to decodeXmlEntities(it.groupValues[2]) }
        val configKey = entries["ConfigKey"]?.trim()
        val ssid = unquote(entries["SSID"]?.trim())
            ?: configKey?.let { key -> CONFIG_KEY.matchEntire(key)?.groupValues?.get(1) }
            ?: return null
        val security = configKey?.let { key ->
            CONFIG_KEY.matchEntire(key)?.groupValues?.get(2) ?: key.substringAfterLast('"')
        } ?: "NONE"
        val rawPsk = entries["PreSharedKey"]?.trim()
        return when {
            rawPsk == null || rawPsk == "null" -> SavedNetwork(ssid, security, null, pskIsHex = false)
            HEX_PSK.matches(rawPsk) -> SavedNetwork(ssid, security, rawPsk, pskIsHex = true)
            else -> SavedNetwork(ssid, security, unquote(rawPsk), pskIsHex = false)
        }
    }

    // ---------- wpa_supplicant.conf (API 28 and older) ----------

    private fun parseSupplicantConf(body: String): List<SavedNetwork> =
        CONF_BLOCK.findAll(body).mapNotNull { m -> parseConfNetwork(m.groupValues[1]) }.toList()

    private fun parseConfNetwork(block: String): SavedNetwork? {
        val kv = mutableMapOf<String, String>()
        block.lineSequence().forEach { line ->
            KEY_VALUE.matchEntire(line.trim())?.let { m ->
                kv.putIfAbsent(m.groupValues[1], m.groupValues[2].trim())
            }
        }
        val ssid = kv["ssid"]?.let { unquote(decodeEscapes(it)) } ?: return null
        val security = kv["key_mgmt"] ?: "NONE"
        val rawPsk = kv["psk"] ?: kv["wep_key0"]
        return when {
            rawPsk == null -> SavedNetwork(ssid, security, null, pskIsHex = false)
            HEX_PSK.matches(rawPsk) -> SavedNetwork(ssid, security, rawPsk, pskIsHex = true)
            else -> SavedNetwork(ssid, security, decodeEscapes(unquote(rawPsk) ?: rawPsk), pskIsHex = false)
        }
    }

    // ---------- shared helpers ----------

    private fun decodeXmlEntities(s: String): String = s
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")

    /** wpa_supplicant escapes: \" \n \xNN inside quoted strings. */
    private fun decodeEscapes(s: String): String {
        if (!s.contains('\\')) return s
        val out = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                when (s[i + 1]) {
                    'x' -> {
                        val hex = s.substring(i + 2).take(2)
                        val v = hex.toIntOrNull(16)
                        if (v != null) { out.append(v.toChar()); i += 4 } else { out.append(c); i += 1 }
                    }
                    'n' -> { out.append('\n'); i += 2 }
                    else -> { out.append(s[i + 1]); i += 2 }
                }
            } else {
                out.append(c); i += 1
            }
        }
        return out.toString()
    }

    private fun unquote(s: String?): String? =
        s?.let { if (it.length >= 2 && it.startsWith('"') && it.endsWith('"')) it.substring(1, it.length - 1) else it }
}

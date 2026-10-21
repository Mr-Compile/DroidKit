package io.github.droidkit.feature.deviceinfo

import io.github.droidkit.core.system.DeviceSystemProbe.CodecEntry

/** Pure mappers for the Device Overview extras — JVM-testable. */
object DeviceInfoExtras {

    /**
     * Decode a Vulkan-style version bitfield: `(major shl 22) or (minor shl 12) or patch`.
     * Used by `android.hardware.vulkan.version` and `android.hardware.vulkan.level`.
     */
    fun vulkanVersionName(bits: Int): String =
        "${(bits ushr 22)}.${(bits ushr 12) and 0x3FF}.${bits and 0xFFF}"

    /**
     * Decode a dEQP-level field — a date-based spec level packed as
     * `(year shl 16) or (month shl 8) or day` (e.g. 0x07E30301 = 2019-03-01).
     */
    fun deqpDateName(bits: Int): String =
        "%04d-%02d-%02d".format(bits ushr 16, (bits ushr 8) and 0xFF, bits and 0xFF)

    /** Implementation family inferred from the codec name — best-effort label. */
    fun codecSourceName(name: String): String = when {
        name.startsWith("c2.") -> "Codec 2.0"
        name.startsWith("OMX.google.") -> "Android platform (SW)"
        name.startsWith("OMX.") -> "OMX vendor"
        else -> "Other"
    }

    /** Decoder/Encoder role label. */
    fun codecRole(isEncoder: Boolean): String = if (isEncoder) "Encoder" else "Decoder"

    /** Short flag chips for a codec entry — omits unknown (API-28) values. */
    fun codecFlags(codec: CodecEntry): List<String> = buildList {
        codec.isHardwareAccelerated?.let { add(if (it) "Hardware" else "Not hardware") }
        if (codec.isSoftwareOnly == true) add("Software")
        if (codec.isVendor == true) add("Vendor")
        if (codec.isAlias == true) add("Alias")
    }

    /** Filter the codec list by role ("all" | "decoders" | "encoders") + free-text. */
    fun filterCodecs(
        codecs: List<CodecEntry>,
        role: String,
        query: String,
    ): List<CodecEntry> {
        val q = query.trim().lowercase()
        return codecs
            .filter { c ->
                when (role) {
                    "decoders" -> !c.isEncoder
                    "encoders" -> c.isEncoder
                    else -> true
                }
            }
            .filter { c ->
                q.isEmpty() || c.name.lowercase().contains(q) ||
                    c.types.any { it.lowercase().contains(q) }
            }
            .sortedWith(compareBy({ it.isEncoder }, { it.name }))
    }
}

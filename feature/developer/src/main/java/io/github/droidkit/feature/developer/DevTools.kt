package io.github.droidkit.feature.developer

/** Pure helpers for the Developer Lab — JVM-testable. */
object DevTools {

    private val PROP_LINE = Regex("""^\s*\[([^\]]+)\]\s*:\s*\[([^\]]*)\]\s*$""")

    /** Parse `getprop` output (`[key]: [value]` lines) into ordered pairs. */
    fun parseProps(stdout: String): List<Pair<String, String>> =
        stdout.lineSequence()
            .mapNotNull { line ->
                PROP_LINE.matchEntire(line)?.let { it.groupValues[1] to it.groupValues[2] }
            }
            .toList()

    /** Plain-language note for well-known keys; null for the rest. */
    fun propNote(key: String): String? = when (key) {
        "ro.build.fingerprint" -> "Build identity — used by CTS/Play Integrity"
        "ro.product.model", "ro.product.device" -> "Marketing / device codename"
        "ro.build.version.sdk" -> "API level this build reports"
        "ro.build.version.security_patch" -> "Security patch level"
        "ro.crypto.state" -> "Device encryption state (encrypted/unencrypted)"
        "ro.debuggable" -> "1 = userdebug/eng build, adb root may be possible"
        "ro.secure" -> "0 = insecure build (adbd runs as root)"
        "ro.boot.verifiedbootstate" -> "Verified-boot state (green/orange/yellow/red)"
        "ro.boot.vbmeta.device_state" -> "Bootloader lock state"
        "ro.treble.enabled" -> "Project Treble support"
        "ro.ab_update" -> "A/B seamless-update support"
        "ro.sf.lcd_density" -> "Panel's default density"
        "persist.sys.timezone" -> "System timezone"
        "init.svc.adbd" -> "adbd daemon state (running/stopped)"
        "sys.usb.state" -> "Current USB gadget mode"
        "gsm.network.type" -> "Cellular radio access technology"
        else -> null
    }

    /** Bytes → "1,234 MB" — keeps 0 honest instead of formatting oddly. */
    fun mb(bytes: Long): String = "%,d MB".format(bytes / (1024 * 1024))
}

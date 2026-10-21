package io.github.droidkit.feature.apps

import java.security.MessageDigest

/** Detail data that needs heavier PM calls — loaded lazily by the screen. */
data class AppExtras(
    val signingSha256: String?,       // colon-separated hex digest, null if unsigned/hidden
    val signingScheme: String?,       // "v1/v2/v3" style hint from SigningInfo
    val installer: String?,           // installing package (Play Store etc.)
    val originating: String?,         // package that initiated the install (browser…)
    val baseApkBytes: Long?,
    val splitCount: Int,
    val totalApkBytes: Long?,
    val nativeLibs: List<String>,     // names of bundled .so files
)

/** Pure helpers for the apps feature — JVM-testable. */
object AppInfo {

    fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString(":") { "%02X".format(it) }

    fun formatBytes(bytes: Long?): String {
        if (bytes == null || bytes < 0) return "Not exposed"
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB")
        var v = bytes / 1024.0
        var i = 0
        while (v >= 1024 && i < units.size - 1) { v /= 1024; i++ }
        return "%.1f %s".format(v, units[i])
    }

    /** Friendly label for a known installer package, else the package name. */
    fun installerLabel(pkg: String?): String = when (pkg) {
        null -> "Unknown / sideloaded"
        "com.android.vending" -> "Google Play Store"
        "com.android.packageinstaller" -> "Package installer (sideload)"
        "com.google.android.packageinstaller" -> "Package installer (sideload)"
        "com.amazon.venezia" -> "Amazon Appstore"
        "com.sec.android.app.samsungapps" -> "Galaxy Store"
        else -> pkg
    }

    /** Abi-style grouping for native lib names like `lib/arm64-v8a/libx.so`. */
    fun libAbi(libPath: String): String? =
        Regex("(?:arm64-v8a|armeabi-v7a|armeabi|x86_64|x86)").find(libPath)?.value

    fun summarizeLibs(libs: List<String>): String =
        if (libs.isEmpty()) "None bundled" else
            libs.groupingBy { libAbi(it) ?: "other" }.eachCount()
                .entries.joinToString(" · ") { "${it.key}: ${it.value}" }
}

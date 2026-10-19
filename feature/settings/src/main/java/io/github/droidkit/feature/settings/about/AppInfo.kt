package io.github.droidkit.feature.settings.about

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Application metadata read from the package manager at runtime.
 * Version/build always reflect the installed APK — never hardcoded.
 */
data class AppInfo(
    val versionName: String,
    val versionCode: Long,
    val packageName: String,
) {
    companion object {
        fun read(context: Context): AppInfo = runCatching {
            val pm = context.packageManager
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(context.packageName, 0)
            }
            AppInfo(
                versionName = info.versionName ?: "unknown",
                versionCode = info.longVersionCode,
                packageName = context.packageName,
            )
        }.getOrElse {
            AppInfo(versionName = "unknown", versionCode = 0L, packageName = context.packageName)
        }
    }
}

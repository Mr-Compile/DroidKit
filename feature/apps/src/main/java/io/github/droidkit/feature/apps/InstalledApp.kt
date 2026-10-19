package io.github.droidkit.feature.apps

data class InstalledApp(
    val packageName: String,
    val label: String,
    val versionName: String?,
    val versionCode: Long,
    val isSystem: Boolean,
    val targetSdk: Int,
    val minSdk: Int?,
    val installer: String?,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val sourceDir: String?,
)

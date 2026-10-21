package io.github.droidkit.feature.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppFilter { ALL, USER, SYSTEM }

@HiltViewModel
class AppsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val pm = context.packageManager

    private val _packages = MutableStateFlow<List<InstalledApp>>(emptyList())
    val loading = MutableStateFlow(true)
    val query = MutableStateFlow("")
    val filter = MutableStateFlow(AppFilter.ALL)

    val packages: StateFlow<List<InstalledApp>> =
        combine(_packages, query, filter) { apps, q, f ->
            apps
                .filter {
                    when (f) {
                        AppFilter.ALL -> true
                        AppFilter.USER -> !it.isSystem
                        AppFilter.SYSTEM -> it.isSystem
                    }
                }
                .filter {
                    q.isBlank() ||
                        it.label.contains(q, ignoreCase = true) ||
                        it.packageName.contains(q, ignoreCase = true)
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            _packages.value = loadVisiblePackages()
            loading.value = false
        }
    }

    private suspend fun loadVisiblePackages(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val flags = PackageManager.PackageInfoFlags.of(0)
        pm.getInstalledPackages(flags).mapNotNull { info -> info.toInstalledApp() }
            .sortedBy { it.label.lowercase() }
    }

    private fun PackageInfo.toInstalledApp(): InstalledApp? {
        val app = applicationInfo ?: return null
        return InstalledApp(
            packageName = packageName,
            label = app.loadLabel(pm).toString(),
            versionName = versionName,
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                longVersionCode
            } else {
                @Suppress("DEPRECATION")
                versionCode.toLong()
            },
            isSystem = app.flags and ApplicationInfo.FLAG_SYSTEM != 0,
            targetSdk = app.targetSdkVersion,
            minSdk = app.minSdkVersion.takeIf { it > 0 },
            installer = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    pm.getInstallSourceInfo(packageName).installingPackageName
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstallerPackageName(packageName)
                }
            }.getOrNull(),
            firstInstallTime = firstInstallTime,
            lastUpdateTime = lastUpdateTime,
            sourceDir = app.sourceDir,
        )
    }

    /** Full PackageInfo for the detail screen — component + permission flags. */
    suspend fun packageInfo(packageName: String): PackageInfo? = withContext(Dispatchers.IO) {
        runCatching {
            pm.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(
                    (PackageManager.GET_PERMISSIONS or PackageManager.GET_ACTIVITIES or
                        PackageManager.GET_SERVICES or PackageManager.GET_RECEIVERS or
                        PackageManager.GET_PROVIDERS or PackageManager.GET_META_DATA).toLong(),
                ),
            )
        }.getOrNull()
    }

    /**
     * Signing digest, install attribution and on-disk size. Everything here is
     * read via standard PackageManager/Zip APIs — all fields degrade to
     * honest "not exposed" values where the platform withholds them.
     */
    suspend fun appExtras(packageName: String): AppExtras = withContext(Dispatchers.IO) {
        val sigInfo = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(
                        PackageManager.GET_SIGNING_CERTIFICATES.toLong(),
                    ),
                ).signingInfo
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
            }
        }.getOrNull()
        val digest = sigInfo?.apkContentsSigners?.firstOrNull()
            ?.let { AppInfo.sha256Hex(it.toByteArray()) }
        val scheme = sigInfo?.let {
            buildList {
                if (!it.apkContentsSigners.isNullOrEmpty()) add("v2+")
                if (it.hasPastSigningCertificates()) add("rotated")
            }.joinToString("/").ifEmpty { null }
        }
        var originating: String? = null
        val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching { pm.getInstallSourceInfo(packageName) }.getOrNull()
                ?.let { originating = it.originatingPackageName; it.installingPackageName }
        } else {
            @Suppress("DEPRECATION")
            runCatching { pm.getInstallerPackageName(packageName) }.getOrNull()
        }
        val app = runCatching { pm.getApplicationInfo(packageName, 0) }.getOrNull()
        val base = app?.sourceDir?.let { java.io.File(it) }
        val splits = app?.splitSourceDirs?.map { java.io.File(it) } ?: emptyList()
        val libs = base?.let { apk ->
            runCatching {
                java.util.zip.ZipFile(apk).use { z ->
                    z.entries().asSequence()
                        .map { it.name }
                        .filter { it.startsWith("lib/") && it.endsWith(".so") }
                        .toList()
                }
            }.getOrNull()
        } ?: emptyList()
        AppExtras(
            signingSha256 = digest,
            signingScheme = scheme,
            installer = installer,
            originating = originating,
            baseApkBytes = base?.takeIf { it.canRead() }?.length(),
            splitCount = splits.size,
            totalApkBytes = (listOfNotNull(base) + splits)
                .mapNotNull { f -> f.takeIf { it.canRead() }?.length() }
                .takeIf { it.isNotEmpty() }?.sum(),
            nativeLibs = libs,
        )
    }

    /** Copy the base APK into shareable cache storage and return its file. */
    suspend fun exportApk(packageName: String): java.io.File? = withContext(Dispatchers.IO) {
        val app = runCatching { pm.getApplicationInfo(packageName, 0) }.getOrNull() ?: return@withContext null
        val src = java.io.File(app.sourceDir ?: return@withContext null)
        if (!src.canRead()) return@withContext null
        val dir = java.io.File(context.cacheDir, "shared").apply { mkdirs() }
        val dst = java.io.File(dir, "$packageName.apk")
        runCatching {
            src.inputStream().use { input -> dst.outputStream().use { input.copyTo(it) } }
        }.getOrNull()?.let { dst }
    }
}

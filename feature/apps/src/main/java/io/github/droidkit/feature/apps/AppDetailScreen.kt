package io.github.droidkit.feature.apps

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ComponentInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import io.github.droidkit.core.ai.AiAssistant
import io.github.droidkit.core.ai.AiExplainButton
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.adaptive.EmptyWindowInsets
import io.github.droidkit.core.ui.adaptive.contentWidthCap
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.SectionHeader
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    packageName: String,
    viewModel: AppsViewModel,
    onBack: () -> Unit,
    embedded: Boolean = false,
) {
    val context = LocalContext.current
    val pm = context.packageManager
    var info by remember { mutableStateOf<PackageInfo?>(null) }
    var extras by remember { mutableStateOf<AppExtras?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var launchError by remember { mutableStateOf<String?>(null) }
    var sharing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(packageName) {
        info = viewModel.packageInfo(packageName)
        extras = viewModel.appExtras(packageName)
        loaded = true
    }

    Scaffold(
        contentWindowInsets =
            if (embedded) EmptyWindowInsets else ScaffoldDefaults.contentWindowInsets,
        topBar = {
            DroidKitTopAppBar(
                "App detail",
                onBack = onBack,
                windowInsets =
                    if (embedded) EmptyWindowInsets else TopAppBarDefaults.windowInsets,
            )
        },
    ) { padding ->
        when {
            !loaded -> Text(
                "Loading…",
                modifier = Modifier.padding(padding).padding(16.dp),
            )
            info == null -> Text(
                "Package not visible to DroidKit: $packageName",
                modifier = Modifier.padding(padding).padding(16.dp),
            )
            else -> {
                val pkg = info!!
                val app = pkg.applicationInfo
                val icon = remember(packageName) {
                    runCatching { pm.getApplicationIcon(packageName).toBitmap().asImageBitmap() }
                        .getOrNull()
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().contentWidthCap(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = padding.calculateTopPadding() + 8.dp,
                        bottom = padding.calculateBottomPadding() + 16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (icon != null) {
                                Image(
                                    bitmap = icon,
                                    contentDescription = "${app?.loadLabel(pm) ?: packageName} icon",
                                    modifier = Modifier.size(48.dp).padding(end = 12.dp),
                                )
                            }
                            Column {
                                Text(
                                    app?.loadLabel(pm)?.toString() ?: packageName,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val intent = pm.getLaunchIntentForPackage(packageName)
                                    if (intent == null) {
                                        launchError = "No launchable activity"
                                    } else {
                                        runCatching { context.startActivity(intent) }
                                            .onFailure { launchError = it.message }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) { Text("Launch") }
                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", packageName, null),
                                            ),
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) { Text("App settings") }
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    sharing = true
                                    val apk = viewModel.exportApk(packageName)
                                    sharing = false
                                    if (apk == null) {
                                        launchError = "APK not readable — cannot share"
                                    } else {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            apk,
                                        )
                                        val share = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/vnd.android.package-archive"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        runCatching {
                                            context.startActivity(
                                                Intent.createChooser(share, "Share APK")
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                            )
                                        }.onFailure { launchError = it.message }
                                    }
                                }
                            },
                            enabled = !sharing,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(if (sharing) "Copying APK…" else "Share base APK") }
                    }
                    launchError?.let { err ->
                        item {
                            Text(
                                err,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    item { SectionHeader("Package") }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                InfoRow("Version", pkg.versionName ?: "Not exposed")
                                InfoRow("Version code", pkg.longVersionCode.toString())
                                InfoRow(
                                    "Target SDK",
                                    app?.targetSdkVersion?.let { "API $it" },
                                )
                                InfoRow(
                                    "Min SDK",
                                    app?.minSdkVersion?.takeIf { it > 0 }?.let { "API $it" },
                                )
                                InfoRow(
                                    "Installed",
                                    DateFormat.getDateInstance().format(Date(pkg.firstInstallTime)),
                                )
                                InfoRow(
                                    "Updated",
                                    DateFormat.getDateInstance().format(Date(pkg.lastUpdateTime)),
                                )
                                InfoRow("APK path", app?.sourceDir)
                            }
                        }
                    }

                    item { SectionHeader("Signing & install") }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val x = extras
                                if (x == null) {
                                    Text("Reading package metadata…")
                                } else {
                                    InfoRow("Signature SHA-256", x.signingSha256, copyable = true)
                                    InfoRow("Signing scheme", x.signingScheme, copyable = false)
                                    InfoRow(
                                        "Installed by",
                                        AppInfo.installerLabel(x.installer),
                                        copyable = false,
                                    )
                                    InfoRow(
                                        "Originating package",
                                        x.originating?.let { AppInfo.installerLabel(it) },
                                        copyable = false,
                                    )
                                }
                            }
                        }
                    }

                    item { SectionHeader("APK & libraries") }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val x = extras
                                if (x == null) {
                                    Text("Reading package metadata…")
                                } else {
                                    InfoRow("Base APK", AppInfo.formatBytes(x.baseApkBytes), copyable = false)
                                    InfoRow("Split APKs", x.splitCount.toString(), copyable = false)
                                    InfoRow("Total on disk", AppInfo.formatBytes(x.totalApkBytes), copyable = false)
                                    InfoRow("Native libs", AppInfo.summarizeLibs(x.nativeLibs), copyable = false)
                                }
                            }
                        }
                    }

                    val aiFields = buildMap<String, String?> {
                        put("Label", app?.loadLabel(pm)?.toString())
                        put("Package", packageName)
                        put("Version", pkg.versionName)
                        put("Target SDK", app?.targetSdkVersion?.toString())
                        put("Min SDK", app?.minSdkVersion?.takeIf { it > 0 }?.toString())
                        put(
                            "Requested permissions",
                            pkg.requestedPermissions?.joinToString(", "),
                        )
                        put("Installed by", extras?.installer?.let { AppInfo.installerLabel(it) })
                        put("Total APK size", extras?.let { AppInfo.formatBytes(it.totalApkBytes) })
                    }
                    item {
                        AiSummaryCard(
                            screenTitle = "App details",
                            fields = aiFields,
                        )
                    }
                    item {
                        AiExplainButton(
                            assistant = AiAssistant.APP_INSPECTOR,
                            fields = aiFields,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    item { SectionHeader("Permissions") }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val requested = pkg.requestedPermissions
                                if (requested == null) {
                                    Text("No permissions requested.")
                                } else {
                                    val flags = pkg.requestedPermissionsFlags
                                    val granted = requested.indices.filter {
                                        flags != null &&
                                            (flags[it] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
                                    }
                                    Text(
                                        "Granted (${granted.size})",
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                    granted.forEach {
                                        Text(
                                            requested[it].substringAfterLast('.'),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                    val denied = requested.indices - granted.toSet()
                                    Text(
                                        "Not granted (${denied.size})",
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                    denied.forEach {
                                        Text(
                                            requested[it].substringAfterLast('.'),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { SectionHeader("Components") }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                ComponentGroup("Activities", pkg.activities?.toList())
                                ComponentGroup("Services", pkg.services?.toList())
                                ComponentGroup("Receivers", pkg.receivers?.toList())
                                ComponentGroup("Providers", pkg.providers?.toList())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComponentGroup(title: String, components: List<ComponentInfo>?) {
    Text(
        "$title (${components?.size ?: 0})",
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 4.dp),
    )
    if (components == null) {
        Text("Not exposed by this package", style = MaterialTheme.typography.bodySmall)
        return
    }
    components.take(30).forEach { c ->
        Text(
            c.name.substringAfterLast('.') +
                if (c.exported) " (exported)" else "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (components.size > 30) {
        Text(
            "…and ${components.size - 30} more",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

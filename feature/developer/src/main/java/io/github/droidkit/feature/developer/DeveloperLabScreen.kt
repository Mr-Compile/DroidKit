package io.github.droidkit.feature.developer

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.droidkit.core.ai.AiSummaryContent
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.adaptive.contentWidthCap
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.SectionHeader

private data class IntentPreset(val label: String, val action: String)

private val presets = listOf(
    IntentPreset("Developer options", Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
    IntentPreset("Device info", Settings.ACTION_DEVICE_INFO_SETTINGS),
    IntentPreset("System settings", Settings.ACTION_SETTINGS),
    IntentPreset("Display settings", Settings.ACTION_DISPLAY_SETTINGS),
    IntentPreset("Battery saver", Settings.ACTION_BATTERY_SAVER_SETTINGS),
    IntentPreset("Battery optimization", Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
    IntentPreset("Wi-Fi settings", Settings.ACTION_WIFI_SETTINGS),
    IntentPreset("Bluetooth settings", Settings.ACTION_BLUETOOTH_SETTINGS),
    IntentPreset("NFC settings", Settings.ACTION_NFC_SETTINGS),
    IntentPreset("Location settings", Settings.ACTION_LOCATION_SOURCE_SETTINGS),
    IntentPreset("Date & time", Settings.ACTION_DATE_SETTINGS),
    IntentPreset("Usage access", Settings.ACTION_USAGE_ACCESS_SETTINGS),
    IntentPreset("Accessibility", Settings.ACTION_ACCESSIBILITY_SETTINGS),
    IntentPreset("Storage", Settings.ACTION_INTERNAL_STORAGE_SETTINGS),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperLabScreen(
    onBack: () -> Unit,
    viewModel: DeveloperViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activeProvider by viewModel.activeProvider.collectAsStateWithLifecycle()
    val logState by viewModel.logState.collectAsStateWithLifecycle()
    var launchError by remember { mutableStateOf<String?>(null) }
    var customAction by remember { mutableStateOf("") }
    var customData by remember { mutableStateOf("") }
    var customPackage by remember { mutableStateOf("") }
    var deepLink by remember { mutableStateOf("") }
    var deepLinkInfo by remember { mutableStateOf<String?>(null) }
    var logTag by remember { mutableStateOf("") }
    var logLines by remember { mutableStateOf("200") }

    fun launch(intent: Intent, label: String) {
        launchError = try {
            context.startActivity(intent)
            null
        } catch (e: ActivityNotFoundException) {
            "$label: no activity on this device handles ${intent.action}"
        } catch (e: SecurityException) {
            "$label: launch blocked by this Android build"
        }
    }

    Scaffold(topBar = { DroidKitTopAppBar("Developer Lab", onBack = onBack) }) { padding ->
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
                Text(
                    "Launch system screens, test deep links, and inspect device state. " +
                        "Elevated log access uses whichever provider is connected " +
                        "(Shizuku, root, or wireless ADB).",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            launchError?.let { error ->
                item {
                    Text(
                        error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            item { SectionHeader("Display metrics") }
            item { DisplayMetricsCard() }

            item { SectionHeader("Intent launcher") }
            items(presets, key = { it.action }) { preset ->
                Card(
                    onClick = { launch(Intent(preset.action), preset.label) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(preset.label, style = MaterialTheme.typography.titleSmall)
                        Text(
                            preset.action,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Custom intent", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(
                            value = customAction,
                            onValueChange = { customAction = it },
                            label = { Text("Action (e.g. android.intent.action.VIEW)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = customData,
                            onValueChange = { customData = it },
                            label = { Text("Data URI (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = customPackage,
                            onValueChange = { customPackage = it },
                            label = { Text("Package (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            "Intent(action=${customAction.ifBlank { "…" }}" +
                                customData.takeIf { it.isNotBlank() }
                                    ?.let { ", data=${it.trim()}" }.orEmpty() +
                                customPackage.takeIf { it.isNotBlank() }
                                    ?.let { ", package=${it.trim()}" }.orEmpty() +
                                ")",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(
                            onClick = {
                                val intent = Intent(customAction.trim())
                                customData.takeIf { it.isNotBlank() }
                                    ?.let { intent.data = Uri.parse(it.trim()) }
                                customPackage.takeIf { it.isNotBlank() }
                                    ?.let { intent.setPackage(it.trim()) }
                                launch(intent, "Custom intent")
                            },
                            enabled = customAction.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Launch")
                        }
                    }
                }
            }

            item { SectionHeader("Deep-link tester") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = deepLink,
                            onValueChange = { deepLink = it },
                            label = { Text("URI (e.g. https:// or myapp://…)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        deepLinkInfo?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Button(
                            onClick = {
                                val uri = Uri.parse(deepLink.trim())
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                val resolved = runCatching {
                                    context.packageManager.resolveActivity(intent, 0)
                                }.getOrNull()
                                deepLinkInfo = resolved?.let {
                                    "Handled by ${it.activityInfo.packageName}/${it.activityInfo.name}"
                                } ?: "No activity on this device handles this URI."
                                launch(intent, "Deep link")
                            },
                            enabled = deepLink.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Resolve & launch")
                        }
                    }
                }
            }

            item { SectionHeader("Log viewer") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            when (val p = activeProvider) {
                                null -> "No elevated provider connected — connect Shizuku or wireless ADB first."
                                else -> "Active provider: ${p.name.lowercase()}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedTextField(
                            value = logLines,
                            onValueChange = { logLines = it.filter(Char::isDigit).take(4) },
                            label = { Text("Lines (max 5000)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = logTag,
                            onValueChange = { logTag = it },
                            label = { Text("Tag filter (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = {
                                viewModel.readLog(
                                    logLines.toIntOrNull()?.coerceIn(1, 5000) ?: 200,
                                    logTag,
                                )
                            },
                            enabled = activeProvider != null &&
                                logState !is DeveloperViewModel.LogState.Loading,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Read logcat")
                        }
                        when (val s = logState) {
                            DeveloperViewModel.LogState.Idle -> {}
                            DeveloperViewModel.LogState.Loading ->
                                Text("Reading…", style = MaterialTheme.typography.bodyMedium)
                            DeveloperViewModel.LogState.NoProvider -> Text(
                                "No elevated provider available — open the ADB Lab or grant Shizuku first.",
                                color = MaterialTheme.colorScheme.error,
                            )
                            is DeveloperViewModel.LogState.Failed -> Text(
                                "Failed (${s.provider.name.lowercase()}): ${s.message}",
                                color = MaterialTheme.colorScheme.error,
                            )
                            is DeveloperViewModel.LogState.Loaded -> Column {
                                s.lines.takeLast(200).forEach { line ->
                                    Text(
                                        line,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// scaledDensity and supportedHdrTypes are deprecated on API 34+ but remain the
// only reads on API 28–33; values are still accurate where the field exists.
@Suppress("DEPRECATION")
@Composable
private fun DisplayMetricsCard() {
    val context = LocalContext.current
    val metrics = context.resources.displayMetrics
    val display = remember {
        (context as? Activity)?.display
            ?: context.getSystemService(DisplayManager::class.java)
                ?.displays?.firstOrNull()
    }
    val bounds = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching {
                context.getSystemService(android.view.WindowManager::class.java)
                    ?.currentWindowMetrics?.bounds
            }.getOrNull()
        } else null
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow(
                "Resolution",
                bounds?.let { "${it.width()} × ${it.height()} px" }
                    ?: "${metrics.widthPixels} × ${metrics.heightPixels} px",
            )
            InfoRow(
                "Size (dp)",
                "%.0f × %.0f dp".format(
                    metrics.widthPixels / metrics.density,
                    metrics.heightPixels / metrics.density,
                ),
            )
            InfoRow(
                "Density",
                "${metrics.densityDpi} dpi (×${metrics.density})",
            )
            InfoRow("Scaled density", "×${metrics.scaledDensity}")
            InfoRow("Physical dpi (x/y)", "${metrics.xdpi.toInt()} / ${metrics.ydpi.toInt()}")
            InfoRow(
                "Refresh rate",
                display?.refreshRate?.let { "%.1f Hz".format(it) } ?: "Not exposed",
            )
            InfoRow(
                "Supported modes",
                display?.supportedModes
                    ?.joinToString(", ") { "%d×%d@%.0f".format(it.physicalWidth, it.physicalHeight, it.refreshRate) }
                    ?: "Not exposed",
            )
            InfoRow(
                "HDR",
                display?.hdrCapabilities?.supportedHdrTypes
                    ?.takeIf { it.isNotEmpty() }
                    ?.joinToString(", ") { hdrTypeName(it) }
                    ?: "None reported",
            )
            AiSummaryContent(
                screenTitle = "Display metrics",
                fields = mapOf(
                    "Resolution" to (bounds?.let { "${it.width()} × ${it.height()} px" }
                        ?: "${metrics.widthPixels} × ${metrics.heightPixels} px"),
                    "Size (dp)" to "%.0f × %.0f dp".format(
                        metrics.widthPixels / metrics.density,
                        metrics.heightPixels / metrics.density,
                    ),
                    "Density" to "${metrics.densityDpi} dpi (×${metrics.density})",
                    "Scaled density" to "×${metrics.scaledDensity}",
                    "Physical dpi (x/y)" to "${metrics.xdpi.toInt()} / ${metrics.ydpi.toInt()}",
                    "Refresh rate" to (display?.refreshRate?.let { "%.1f Hz".format(it) }
                        ?: "Not exposed"),
                    "Supported modes" to (display?.supportedModes
                        ?.joinToString(", ") { "%d×%d@%.0f".format(it.physicalWidth, it.physicalHeight, it.refreshRate) }
                        ?: "Not exposed"),
                    "HDR" to (display?.hdrCapabilities?.supportedHdrTypes
                        ?.takeIf { it.isNotEmpty() }
                        ?.joinToString(", ") { hdrTypeName(it) }
                        ?: "None reported"),
                ),
            )
        }
    }
}

private fun hdrTypeName(type: Int): String = when (type) {
    android.view.Display.HdrCapabilities.HDR_TYPE_HDR10 -> "HDR10"
    android.view.Display.HdrCapabilities.HDR_TYPE_HLG -> "HLG"
    android.view.Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> "HDR10+"
    android.view.Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> "Dolby Vision"
    else -> "type $type"
}

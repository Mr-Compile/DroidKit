package io.github.droidkit.feature.developer

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
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
import io.github.droidkit.core.ui.components.DroidKitButton
import io.github.droidkit.core.ui.components.DroidKitCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import io.github.droidkit.core.ui.components.DroidKitTextField
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
import io.github.droidkit.core.ui.CopyAllAction
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.adaptive.contentWidthCap
import io.github.droidkit.core.ui.formatSectionsForCopy
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
    IntentPreset("Notification listener", Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
    IntentPreset("Security", Settings.ACTION_SECURITY_SETTINGS),
    IntentPreset("Privacy", Settings.ACTION_PRIVACY_SETTINGS),
    IntentPreset("VPN", Settings.ACTION_VPN_SETTINGS),
    IntentPreset("Manage apps", Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS),
    IntentPreset("Default apps", Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
    IntentPreset("Do Not Disturb", Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS),
    IntentPreset("Input methods", Settings.ACTION_INPUT_METHOD_SETTINGS),
    IntentPreset("Print services", "android.settings.ACTION_PRINT_SETTINGS"),
    IntentPreset("Cast / media output", Settings.ACTION_CAST_SETTINGS),
    IntentPreset("WebView implementation", Settings.ACTION_WEBVIEW_SETTINGS),
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
    val propsState by viewModel.propsState.collectAsStateWithLifecycle()
    val memory by viewModel.memory.collectAsStateWithLifecycle()
    val propQuery by viewModel.propQuery.collectAsStateWithLifecycle()
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

    Scaffold(
        topBar = {
            DroidKitTopAppBar(
                "Developer Lab",
                onBack = onBack,
                actions = {
                    CopyAllAction {
                        formatSectionsForCopy(
                            buildList {
                                add("Display metrics" to displayMetricsRows(context))
                                memory?.let { m ->
                                    val usedPct = if (m.totalMem > 0) {
                                        ((m.totalMem - m.availMem) * 100 / m.totalMem)
                                    } else {
                                        0
                                    }
                                    add(
                                        "Memory" to listOf(
                                            "System RAM" to "${DevTools.mb(m.totalMem)} total",
                                            "Available" to
                                                "${DevTools.mb(m.availMem)} ($usedPct% used)",
                                            "Low-memory threshold" to
                                                DevTools.mb(m.lowMemThreshold),
                                            "Low-memory state" to
                                                if (m.lowMemory) {
                                                    "Yes — device is under memory pressure"
                                                } else {
                                                    "No"
                                                },
                                            "App heap limit" to DevTools.mb(m.heapMax),
                                            "App heap in use" to DevTools.mb(m.heapUsed),
                                        ),
                                    )
                                }
                            },
                        ).takeIf { it.isNotBlank() }
                    }
                },
            )
        },
    ) { padding ->
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
                        "(root or wireless ADB).",
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

            item { SectionHeader("Memory") }
            item { MemoryCard(memory = memory, onRefresh = viewModel::refreshMemory) }

            item { SectionHeader("Intent launcher") }
            items(presets, key = { it.action }) { preset ->
                DroidKitCard(
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
                DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Custom intent", style = MaterialTheme.typography.titleSmall)
                        DroidKitTextField(
                            value = customAction,
                            onValueChange = { customAction = it },
                            label = { Text("Action (e.g. android.intent.action.VIEW)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        DroidKitTextField(
                            value = customData,
                            onValueChange = { customData = it },
                            label = { Text("Data URI (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        DroidKitTextField(
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
                        DroidKitButton(
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
                DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DroidKitTextField(
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
                        DroidKitButton(
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

            item { SectionHeader("System properties") }
            item {
                DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            "Read the full getprop table through the elevated provider, " +
                                "then filter it here. Known keys get a plain-language note.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        DroidKitTextField(
                            value = propQuery,
                            onValueChange = { viewModel.propQuery.value = it },
                            label = { Text("Filter keys or values") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        DroidKitButton(
                            onClick = viewModel::readProps,
                            enabled = activeProvider != null &&
                                propsState !is DeveloperViewModel.PropsState.Loading,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Read getprop") }
                        when (val s = propsState) {
                            DeveloperViewModel.PropsState.Idle -> {}
                            DeveloperViewModel.PropsState.Loading ->
                                Text("Reading…", style = MaterialTheme.typography.bodyMedium)
                            DeveloperViewModel.PropsState.NoProvider -> Text(
                                "No elevated provider available — open the ADB Lab or grant root first.",
                                color = MaterialTheme.colorScheme.error,
                            )
                            is DeveloperViewModel.PropsState.Failed -> Text(
                                "Failed (${s.provider.name.lowercase()}): ${s.message}",
                                color = MaterialTheme.colorScheme.error,
                            )
                            is DeveloperViewModel.PropsState.Loaded -> {
                                val q = propQuery.trim().lowercase()
                                val shown = s.props.filter {
                                    q.isEmpty() || it.first.lowercase().contains(q) ||
                                        it.second.lowercase().contains(q)
                                }
                                Text(
                                    "${shown.size} of ${s.props.size} properties",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                shown.take(200).forEach { (k, v) ->
                                    Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(
                                            "$k = $v",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                        DevTools.propNote(k)?.let {
                                            Text(
                                                it,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                                if (shown.size > 200) {
                                    Text(
                                        "… +${shown.size - 200} more (refine the filter)",
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { SectionHeader("Log viewer") }
            item {
                DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            when (val p = activeProvider) {
                                null -> "No elevated provider connected — connect wireless ADB or grant root first."
                                else -> "Active provider: ${p.name.lowercase()}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        DroidKitTextField(
                            value = logLines,
                            onValueChange = { logLines = it.filter(Char::isDigit).take(4) },
                            label = { Text("Lines (max 5000)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        DroidKitTextField(
                            value = logTag,
                            onValueChange = { logTag = it },
                            label = { Text("Tag filter (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        DroidKitButton(
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
                                "No elevated provider available — open the ADB Lab or grant root first.",
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

    DroidKitCard(modifier = Modifier.fillMaxWidth()) {
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

@Composable
private fun MemoryCard(memory: DeveloperViewModel.MemorySnapshot?, onRefresh: () -> Unit) {
    DroidKitCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (memory == null) {
                Text("Reading…", style = MaterialTheme.typography.bodyMedium)
            } else {
                val usedPct = if (memory.totalMem > 0) {
                    ((memory.totalMem - memory.availMem) * 100 / memory.totalMem)
                } else 0
                InfoRow("System RAM", "${DevTools.mb(memory.totalMem)} total")
                InfoRow(
                    "Available",
                    "${DevTools.mb(memory.availMem)} ($usedPct% used)",
                )
                InfoRow("Low-memory threshold", DevTools.mb(memory.lowMemThreshold))
                InfoRow(
                    "Low-memory state",
                    if (memory.lowMemory) "Yes — device is under memory pressure" else "No",
                )
                InfoRow("App heap limit", DevTools.mb(memory.heapMax))
                InfoRow("App heap in use", DevTools.mb(memory.heapUsed))
                Text(
                    "From ActivityManager + Runtime — standard APIs, no elevated access.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            androidx.compose.material3.TextButton(
                onClick = onRefresh,
                modifier = Modifier.padding(top = 4.dp),
            ) { Text("Refresh") }
        }
    }
}

@Suppress("DEPRECATION")
private fun displayMetricsRows(context: Context): List<Pair<String, String?>> {
    val metrics = context.resources.displayMetrics
    val display = (context as? Activity)?.display
        ?: context.getSystemService(DisplayManager::class.java)
            ?.displays?.firstOrNull()
    val bounds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        runCatching {
            context.getSystemService(android.view.WindowManager::class.java)
                ?.currentWindowMetrics?.bounds
        }.getOrNull()
    } else {
        null
    }
    return listOf(
        "Resolution" to (
            bounds?.let { "${it.width()} × ${it.height()} px" }
                ?: "${metrics.widthPixels} × ${metrics.heightPixels} px"
            ),
        "Size (dp)" to "%.0f × %.0f dp".format(
            metrics.widthPixels / metrics.density,
            metrics.heightPixels / metrics.density,
        ),
        "Density" to "${metrics.densityDpi} dpi (×${metrics.density})",
        "Scaled density" to "×${metrics.scaledDensity}",
        "Physical dpi (x/y)" to "${metrics.xdpi.toInt()} / ${metrics.ydpi.toInt()}",
        "Refresh rate" to (display?.refreshRate?.let { "%.1f Hz".format(it) } ?: "Not exposed"),
        "Supported modes" to (
            display?.supportedModes
                ?.joinToString(", ") {
                    "%d×%d@%.0f".format(it.physicalWidth, it.physicalHeight, it.refreshRate)
                }
                ?: "Not exposed"
            ),
        "HDR" to (
            display?.hdrCapabilities?.supportedHdrTypes
                ?.takeIf { it.isNotEmpty() }
                ?.joinToString(", ") { hdrTypeName(it) }
                ?: "None reported"
            ),
    )
}

private fun hdrTypeName(type: Int): String = when (type) {
    android.view.Display.HdrCapabilities.HDR_TYPE_HDR10 -> "HDR10"
    android.view.Display.HdrCapabilities.HDR_TYPE_HLG -> "HLG"
    android.view.Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> "HDR10+"
    android.view.Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> "Dolby Vision"
    else -> "type $type"
}

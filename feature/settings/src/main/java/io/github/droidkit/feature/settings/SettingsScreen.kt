package io.github.droidkit.feature.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import io.github.droidkit.core.ui.components.AiSurfaceCard
import io.github.droidkit.core.ui.components.ConfirmationSheet
import io.github.droidkit.core.ui.components.DroidKitAlertDialog
import io.github.droidkit.core.ui.components.DroidKitCard
import io.github.droidkit.core.ui.components.DroidKitSegmentedRow
import io.github.droidkit.core.ui.components.RiskLevel
import io.github.droidkit.core.ui.components.StatusDot
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.datastore.ThemeMode
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.ui.adaptive.contentWidthCap
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.SectionHeader
import io.github.droidkit.core.ui.theme.DroidKitMark
import io.github.droidkit.core.ui.theme.DroidKitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenAdb: () -> Unit = {},
    onOpenAiChat: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenPermissions: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onOpenCapabilities: () -> Unit = {},
    onOpenHelp: () -> Unit = {},
    onOpenWhatsNew: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val theme by viewModel.themeMode.collectAsState()
    val providerStatuses by viewModel.providerStatuses.collectAsState()
    val grantBusy by viewModel.grantBusy.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    var backupStatus by remember { mutableStateOf<String?>(null) }
    var pendingImport by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                backupStatus = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use {
                        it.write(viewModel.exportSettingsJson().toByteArray())
                    } ?: error("could not open destination")
                    "Backup exported"
                }.getOrElse { "Export failed — ${it.message}" }
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val text = runCatching {
                    context.contentResolver.openInputStream(uri)?.use {
                        it.readBytes().decodeToString()
                    }
                }.getOrNull()
                if (text == null) {
                    backupStatus = "Import failed — could not read file"
                } else {
                    pendingImport = text
                }
            }
        }
    }

    Scaffold(topBar = { DroidKitTopAppBar("Settings") }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().contentWidthCap(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader("Appearance") }
            item {
                DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // mockup .segcard theme control
                        DroidKitSegmentedRow(
                            options = ThemeMode.entries,
                            selected = theme,
                            onSelect = { viewModel.setTheme(it) },
                            label = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                        )
                        val dynamicColor by viewModel.dynamicColor.collectAsState()
                        val materialYouSupported =
                            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Material You colors")
                                Text(
                                    if (materialYouSupported) "Use your wallpaper palette"
                                    else "Requires Android 12 or later",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            androidx.compose.material3.Switch(
                                checked = dynamicColor && materialYouSupported,
                                onCheckedChange = { viewModel.setDynamicColor(it) },
                                enabled = materialYouSupported,
                            )
                        }
                    }
                }
            }

            item { SectionHeader("Reports") }
            item {
                val technician by viewModel.technicianName.collectAsState()
                var techInput by androidx.compose.runtime.remember(technician) {
                    androidx.compose.runtime.mutableStateOf(technician)
                }
                DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        io.github.droidkit.core.ui.components.DroidKitTextField(
                            value = techInput,
                            onValueChange = { techInput = it },
                            label = { Text("Technician name / signature") },
                            supportingText = {
                                Text("Stamped on new diagnostic sessions and reports. Stored locally.")
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        TextButton(
                            onClick = { viewModel.setTechnicianName(techInput) },
                            enabled = techInput.trim() != technician,
                        ) { Text("Save") }
                    }
                }
            }

            item { SectionHeader("Advanced access") }
            item {
                DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        providerStatuses.forEach { (kind, status) ->
                            // mockup provider row: .vdot + .provk + .t-mini.sub + Grant
                            val (dot, label) = when (status) {
                                ProviderStatus.Ready ->
                                    DroidKitTheme.colors.statusSuccess to "Ready"
                                is ProviderStatus.NeedsSetup ->
                                    DroidKitTheme.colors.statusSetupRequired to
                                        "Setup required — ${status.instructions}"
                                is ProviderStatus.Unavailable ->
                                    DroidKitTheme.colors.statusUnsupported to status.reason
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            ) {
                                StatusDot(dot, modifier = Modifier.padding(top = 3.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 9.dp),
                                ) {
                                    Text(
                                        kind.name.uppercase(),
                                        style = DroidKitTheme.extraTypography.microLabel,
                                        color = DroidKitTheme.colors.faint,
                                    )
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DroidKitTheme.colors.textSecondary,
                                    )
                                }
                                if (status is ProviderStatus.NeedsSetup) {
                                    TextButton(
                                        onClick = { viewModel.requestGrant(kind) },
                                        enabled = !grantBusy,
                                    ) {
                                        Text(if (grantBusy) "Working…" else "Grant")
                                    }
                                }
                            }
                        }
                        Row {
                            TextButton(onClick = onOpenCapabilities) {
                                Text("Manage capabilities")
                            }
                            TextButton(onClick = onOpenAdb) {
                                Text("Open ADB Lab for wireless pairing")
                            }
                        }
                    }
                }
            }

            item {
                val log by viewModel.elevatedLog.collectAsState()
                if (log.isNotEmpty()) {
                    SectionHeader("Elevated action history")
                    DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            log.take(10).forEach { entry ->
                                val time = java.text.SimpleDateFormat(
                                    "HH:mm:ss",
                                    java.util.Locale.US,
                                ).format(java.util.Date(entry.epochMs))
                                Text(
                                    "$time  ${entry.provider}  exit ${entry.exitCode}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (entry.exitCode == 0) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    },
                                )
                                Text(
                                    entry.command,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                )
                            }
                            TextButton(onClick = { viewModel.clearElevatedLog() }) {
                                Text("Clear history")
                            }
                        }
                    }
                }
            }

            item { SectionHeader("DroidKit Intelligence") }
            item {
                val aiEnabled by viewModel.aiEnabled.collectAsState()
                val aiAutoSummarize by viewModel.aiAutoSummarize.collectAsState()
                val aiModel by viewModel.aiModel.collectAsState()
                val aiHasKey by viewModel.aiHasKey.collectAsState()
                val aiStatus by viewModel.aiStatus.collectAsState()
                val aiTesting by viewModel.aiTesting.collectAsState()
                var keyInput by androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableStateOf("")
                }
                AiSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Enable AI assistant", modifier = Modifier.weight(1f))
                            androidx.compose.material3.Switch(
                                checked = aiEnabled,
                                onCheckedChange = { viewModel.setAiEnabled(it) },
                            )
                        }
                        Text(
                            "Optional. Send minimal device context to Groq for explanations " +
                                "and report summaries. Secrets and tokens are stripped before " +
                                "anything leaves the device. Core tools work with AI off.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (aiEnabled) {
                            io.github.droidkit.core.ui.components.DroidKitTextField(
                                value = keyInput,
                                onValueChange = { keyInput = it },
                                label = { Text(if (aiHasKey) "Replace Groq API key" else "Groq API key") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 8.dp),
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.saveAiKey(keyInput)
                                        keyInput = ""
                                    },
                                    enabled = keyInput.isNotBlank(),
                                ) { Text("Save key") }
                                if (aiHasKey) {
                                    TextButton(onClick = { viewModel.clearAiKey() }) {
                                        Text("Disconnect")
                                    }
                                }
                                TextButton(
                                    onClick = { viewModel.testAiConnection() },
                                    enabled = !aiTesting,
                                ) { Text(if (aiTesting) "Testing…" else "Test connection") }
                            }
                            io.github.droidkit.core.ai.AiSettingsStore.MODEL_CHOICES.forEach { m ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = aiModel == m,
                                        onClick = { viewModel.setAiModel(m) },
                                    )
                                    Text(m, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp),
                            ) {
                                Text("Auto-summarize results", modifier = Modifier.weight(1f))
                                androidx.compose.material3.Switch(
                                    checked = aiAutoSummarize,
                                    onCheckedChange = { viewModel.setAiAutoSummarize(it) },
                                )
                            }
                            Text(
                                "When on, result screens send the values they show to Groq " +
                                    "for a plain-language summary as soon as they appear. " +
                                    "Uses your key more often.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            aiStatus?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (it == "Connected") MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error,
                                )
                            }
                            TextButton(onClick = onOpenAiChat) {
                                Text("Open DroidKit Chat")
                            }
                        }
                    }
                }
            }

            item { SectionHeader("Backup") }
            item {
                DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Export your preferences to a small JSON file and restore " +
                                "them later. Device pairing state, the AI key and " +
                                "onboarding are never included.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            TextButton(
                                onClick = { exportLauncher.launch("droidkit-settings.json") },
                            ) { Text("Export settings") }
                            TextButton(
                                onClick = {
                                    importLauncher.launch(
                                        arrayOf("application/json", "text/*", "application/octet-stream"),
                                    )
                                },
                            ) { Text("Import settings") }
                        }
                        backupStatus?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (it.contains("failed")) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }

            item { SectionHeader("Safety") }
            item {
                DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "High-impact actions always show their exact consequence and ask " +
                                "for explicit confirmation. DroidKit never performs security " +
                                "or credential bypass operations.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item { SectionHeader("Support & privacy") }
            item {
                DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingsNavRow(
                            title = "Permissions & Access",
                            subtitle = "Android permissions and elevated providers",
                            onClick = onOpenPermissions,
                        )
                        SettingsNavRow(
                            title = "Privacy Center",
                            subtitle = "What stays on-device and what can go online",
                            onClick = onOpenPrivacy,
                        )
                        SettingsNavRow(
                            title = "Help & FAQ",
                            subtitle = "Guides, permissions and troubleshooting",
                            onClick = onOpenHelp,
                        )
                        SettingsNavRow(
                            title = "What's New",
                            subtitle = "Release notes for this build",
                            onClick = onOpenWhatsNew,
                        )
                        SettingsNavRow(
                            title = "Reset settings",
                            subtitle = "Theme, favorites, recents and preferences",
                            showChevron = false,
                            onClick = { showResetDialog = true },
                        )
                    }
                }
            }

            item { SectionHeader("About") }
            item {
                DroidKitCard(onClick = onOpenAbout, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            imageVector = DroidKitMark,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 12.dp).size(24.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "About DroidKit",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                "App information, support and legal information",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    pendingImport?.let { json ->
        ConfirmationSheet(
            title = "Restore settings backup?",
            consequence = "Replaces the theme, technician name, favorites, recents, " +
                "usage counters and the ADB auto-connect preference on this device. " +
                "Device pairing state, the AI key and onboarding are not affected.",
            confirmLabel = "Restore",
            risk = RiskLevel.System,
            onConfirm = {
                scope.launch {
                    backupStatus = if (viewModel.importSettingsJson(json)) {
                        "Backup restored"
                    } else {
                        "Import failed — not a valid DroidKit backup"
                    }
                }
                pendingImport = null
            },
            onDismiss = { pendingImport = null },
        )
    }

    if (showResetDialog) {
        DroidKitAlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset settings?") },
            text = {
                Text(
                    "Resets the theme, technician name, favorite tools, recent " +
                        "tools and usage counters to defaults. Your AI key and " +
                        "provider setup are not affected, and the intro will not " +
                        "play again.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSettings()
                        showResetDialog = false
                    },
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            },
        )
    }
}

/** Tappable settings row: whole area is one click target with merged semantics. */
@Composable
private fun SettingsNavRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showChevron: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

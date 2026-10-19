package io.github.droidkit.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.datastore.ThemeMode
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenAdb: () -> Unit = {},
    onOpenAiChat: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val theme by viewModel.themeMode.collectAsState()
    val providerStatuses by viewModel.providerStatuses.collectAsState()
    val grantBusy by viewModel.grantBusy.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader("Appearance") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = theme == mode,
                                    onClick = { viewModel.setTheme(mode) },
                                )
                                Text(mode.name)
                            }
                        }
                    }
                }
            }

            item { SectionHeader("Advanced access") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        providerStatuses.forEach { (kind, status) ->
                            val label = when (status) {
                                ProviderStatus.Ready -> "Ready"
                                is ProviderStatus.NeedsSetup -> "Setup required — ${status.instructions}"
                                is ProviderStatus.Unavailable -> status.reason
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            ) {
                                Text(
                                    "${kind.name} — $label",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                )
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
                        TextButton(onClick = onOpenAdb) {
                            Text("Open ADB Lab for wireless pairing")
                        }
                    }
                }
            }

            item {
                val log by viewModel.elevatedLog.collectAsState()
                if (log.isNotEmpty()) {
                    SectionHeader("Elevated action history")
                    Card(modifier = Modifier.fillMaxWidth()) {
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
                val aiModel by viewModel.aiModel.collectAsState()
                val aiHasKey by viewModel.aiHasKey.collectAsState()
                val aiStatus by viewModel.aiStatus.collectAsState()
                val aiTesting by viewModel.aiTesting.collectAsState()
                var keyInput by androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableStateOf("")
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                            androidx.compose.material3.OutlinedTextField(
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

            item { SectionHeader("Safety") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
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

            item { SectionHeader("About") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("DroidKit ${viewModel.versionName}", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Android technician & power toolkit.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

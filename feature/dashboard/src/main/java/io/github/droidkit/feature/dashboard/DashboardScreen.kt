package io.github.droidkit.feature.dashboard

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.DroidKitWordmark
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.SectionHeader
import io.github.droidkit.core.ui.components.ToolCard
import io.github.droidkit.core.ui.components.ToolInfoDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun fmtDay(epochMs: Long): String =
    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epochMs))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTools: () -> Unit,
    onNavigateToDiagnose: () -> Unit,
    onOpenTool: (String) -> Unit,
    onOpenInsights: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val latestInsight by viewModel.latestInsight.collectAsStateWithLifecycle(initialValue = null)
    val recents by viewModel.recentToolIds.collectAsStateWithLifecycle(initialValue = emptyList())
    val recentTools = recents.mapNotNull { viewModel.toolById(it) }
    val favorites by viewModel.favoriteToolIds.collectAsStateWithLifecycle(initialValue = emptySet())
    val pinnedTools = viewModel.pinned(favorites)
    val providerStatuses by viewModel.providerStatuses.collectAsStateWithLifecycle()
    // Recomputed when provider status changes — drives the "Ready now" section.
    val quickTools = remember(providerStatuses) { viewModel.quickTools() }
    var infoTool by remember { mutableStateOf<Tool?>(null) }

    Scaffold(topBar = { DroidKitTopAppBar(title = { DroidKitWordmark() }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("This device", style = MaterialTheme.typography.titleSmall)
                        Text(viewModel.probe.summary(), style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Elevated access", style = MaterialTheme.typography.titleSmall)
                        providerStatuses.forEach { (kind, status) ->
                            val label = when (status) {
                                ProviderStatus.Ready -> "ready"
                                is ProviderStatus.NeedsSetup -> "setup required"
                                is ProviderStatus.Unavailable -> status.reason
                            }
                            Text("${kind.name}: $label",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                AiSummaryCard(
                    screenTitle = "Dashboard",
                    fields = buildMap {
                        put("Device", viewModel.probe.summary())
                        providerStatuses.forEach { (kind, status) ->
                            put(
                                "Elevated ${kind.name}",
                                when (status) {
                                    ProviderStatus.Ready -> "ready"
                                    is ProviderStatus.NeedsSetup -> "setup required"
                                    is ProviderStatus.Unavailable -> status.reason
                                },
                            )
                        }
                        latestInsight?.let { h ->
                            put("Latest diagnostic", "${h.pass}/${h.total} passed, ${h.fail} failed")
                        }
                    },
                )
            }

            latestInsight?.let { h ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenInsights),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Insights", style = MaterialTheme.typography.titleSmall)
                            Text(
                                buildString {
                                    append("Latest diagnostic: ${h.pass}/${h.total} passed")
                                    if (h.fail > 0) append(" · ${h.fail} failed")
                                    append(" · ${fmtDay(h.startedAtEpochMs)} →")
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }

            item {
                Button(onClick = onNavigateToDiagnose, modifier = Modifier.fillMaxWidth()) {
                    Text("Start Full Diagnostic")
                }
            }

            if (pinnedTools.isNotEmpty()) {
                item { SectionHeader("Pinned tools") }
                items(pinnedTools, key = { "pin_${it.id}" }) { tool ->
                    ToolCard(
                        tool = tool,
                        resolution = remember(tool, providerStatuses) { viewModel.resolve(tool) },
                        onInfo = { infoTool = tool },
                        onClick = { viewModel.recordOpen(tool); onOpenTool(tool.route) },
                    )
                }
            }

            item { SectionHeader("Recent tools") }
            if (recentTools.isEmpty()) {
                item { EmptyState("No recent tools yet — browse the Tools tab.") }
            } else {
                items(recentTools, key = { it.id }) { tool ->
                    ToolCard(
                        tool = tool,
                        resolution = remember(tool, providerStatuses) { viewModel.resolve(tool) },
                        onInfo = { infoTool = tool },
                        onClick = { viewModel.recordOpen(tool); onOpenTool(tool.route) },
                    )
                }
            }

            item { SectionHeader("Ready now") }
            items(quickTools, key = { it.id }) { tool ->
                ToolCard(
                    tool = tool,
                    resolution = remember(tool, providerStatuses) { viewModel.resolve(tool) },
                    onInfo = { infoTool = tool },
                    onClick = { viewModel.recordOpen(tool); onOpenTool(tool.route) },
                )
            }
        }
    }

    infoTool?.let { tool ->
        ToolInfoDialog(
            tool = tool,
            resolution = viewModel.resolve(tool),
            onDismiss = { infoTool = null },
        )
    }
}

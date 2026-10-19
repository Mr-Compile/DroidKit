package io.github.droidkit.feature.dashboard

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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.SectionHeader
import io.github.droidkit.core.ui.components.ToolCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTools: () -> Unit,
    onNavigateToDiagnose: () -> Unit,
    onOpenTool: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val recents by viewModel.recentToolIds.collectAsState(initial = emptyList())
    val recentTools = recents.mapNotNull { viewModel.toolById(it) }

    Scaffold(topBar = { TopAppBar(title = { Text("DroidKit") }) }) { padding ->
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
                        viewModel.providerStatuses().forEach { (kind, status) ->
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
                Button(onClick = onNavigateToDiagnose, modifier = Modifier.fillMaxWidth()) {
                    Text("Start Full Diagnostic")
                }
            }

            item { SectionHeader("Recent tools") }
            if (recentTools.isEmpty()) {
                item { EmptyState("No recent tools yet — browse the Tools tab.") }
            } else {
                items(recentTools, key = { it.id }) { tool ->
                    ToolCard(
                        tool = tool,
                        resolution = viewModel.resolve(tool),
                        onClick = { onOpenTool(tool.route) },
                    )
                }
            }

            item { SectionHeader("Ready now") }
            items(viewModel.quickTools(), key = { it.id }) { tool ->
                ToolCard(
                    tool = tool,
                    resolution = viewModel.resolve(tool),
                    onClick = { onOpenTool(tool.route) },
                )
            }
        }
    }
}

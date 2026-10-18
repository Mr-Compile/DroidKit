package io.github.droidkit.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.components.BlockedReasonList
import io.github.droidkit.core.ui.components.ToolCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureStubScreen(
    title: String,
    subtitle: String,
    tools: List<Tool>,
    resolve: (Tool) -> ToolResolution,
) {
    var selected by remember { mutableStateOf<Tool?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { padding ->
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
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            items(tools, key = { it.id }) { tool ->
                ToolCard(
                    tool = tool,
                    resolution = resolve(tool),
                    onClick = { selected = tool },
                )
            }
        }
    }

    selected?.let { tool ->
        val resolution = resolve(tool)
        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("OK") } },
            title = { Text(tool.title) },
            text = {
                when (resolution) {
                    ToolResolution.Ready ->
                        Text("Available — implementation lands in phase ${tool.phase.name}.")
                    is ToolResolution.Blocked -> BlockedReasonList(resolution.reasons)
                }
            },
        )
    }
}

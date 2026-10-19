package io.github.droidkit.feature.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.ToolCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onOpenTool: (String) -> Unit,
    viewModel: ToolsViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsState()
    val category by viewModel.category.collectAsState()
    val favorites by viewModel.favorites.collectAsState(initial = emptySet())

    Scaffold(topBar = { TopAppBar(title = { Text("Tools") }) }) { padding ->
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
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::setQuery,
                    label = { Text("Search tools") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = category == null,
                            onClick = { viewModel.setCategory(null) },
                            label = { Text("All") },
                        )
                    }
                    items(viewModel.categories) { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { viewModel.setCategory(if (category == c) null else c) },
                            label = { Text(c.name) },
                        )
                    }
                }
            }
            val tools = viewModel.visibleTools()
            if (tools.isEmpty()) {
                item { EmptyState("No tools match.") }
            } else {
                items(tools, key = { it.id }) { tool ->
                    ToolCard(
                        tool = tool,
                        resolution = viewModel.resolve(tool),
                        isFavorite = tool.id in favorites,
                        onFavorite = { viewModel.toggleFavorite(tool) },
                        onClick = {
                            viewModel.recordOpen(tool)
                            onOpenTool(tool.route)
                        },
                    )
                }
            }
        }
    }
}

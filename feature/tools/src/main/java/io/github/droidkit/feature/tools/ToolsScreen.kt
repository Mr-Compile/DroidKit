package io.github.droidkit.feature.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.common.RecoveryAction
import io.github.droidkit.core.common.SearchEntry
import io.github.droidkit.core.common.SearchResultType
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.privileges.toUiProblem
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.PermissionRequestEffect
import io.github.droidkit.core.ui.components.SectionHeader
import io.github.droidkit.core.ui.components.ToolCard
import io.github.droidkit.core.ui.components.ToolIcon
import io.github.droidkit.core.ui.components.ToolIcons
import io.github.droidkit.core.ui.components.ToolInfoDialog
import io.github.droidkit.core.ui.components.UiProblemCard
import io.github.droidkit.core.ui.components.openAppSettings
import io.github.droidkit.core.ui.theme.categoryAccent

/** Browse order for the grouped list — same families as the product map/mockup. */
private val CATEGORY_ORDER = listOf(
    ToolCategory.DEVICE,
    ToolCategory.DIAGNOSTICS,
    ToolCategory.CONNECTIVITY,
    ToolCategory.HARDWARE,
    ToolCategory.APPS,
    ToolCategory.DEVELOPER,
    ToolCategory.REPORTS,
)

private fun categoryLabel(c: ToolCategory) =
    c.name.lowercase().replaceFirstChar { it.uppercase() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onOpenTool: (String) -> Unit,
    viewModel: ToolsViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsState()
    val category by viewModel.category.collectAsState()
    val favorites by viewModel.favorites.collectAsState(initial = emptySet())
    val providerStatuses by viewModel.providerStatuses.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var blockedTool by remember { mutableStateOf<Tool?>(null) }
    var infoTool by remember { mutableStateOf<Tool?>(null) }
    var pendingPermission by remember { mutableStateOf<String?>(null) }

    pendingPermission?.let { permission ->
        PermissionRequestEffect(permission) { pendingPermission = null }
    }

    Scaffold(topBar = { DroidKitTopAppBar("Tools") }) { padding ->
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
                    label = { Text("Search tools and pages") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { focusManager.clearFocus() },
                    ),
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
                            label = { Text(categoryLabel(c)) },
                        )
                    }
                }
            }
            val tools = viewModel.visibleTools()
            val entries = if (category == null) viewModel.searchEntries(query) else emptyList()
            val grouped = entries.groupBy { it.type }
            if (grouped.isNotEmpty()) {
                SearchResultType.entries.forEach { type ->
                    val group = grouped[type].orEmpty()
                    if (group.isNotEmpty()) {
                        item { SectionHeader(type.label) }
                        items(group, key = { "entry_${it.id}" }) { entry ->
                            SearchResultRow(
                                entry = entry,
                                onClick = { onOpenTool(entry.route) },
                            )
                        }
                    }
                }
                item { SectionHeader("Tools") }
            }
            if (tools.isEmpty() && entries.isEmpty()) {
                item {
                    EmptyState(
                        text = "No tools or pages match.",
                        actionLabel = if (query.isNotBlank()) "Clear search" else null,
                        onAction = if (query.isNotBlank()) ({ viewModel.setQuery("") }) else null,
                    )
                }
            } else {
                val toolItem: @Composable (Tool) -> Unit = { tool ->
                    val resolution = remember(tool, providerStatuses) { viewModel.resolve(tool) }
                    ToolCard(
                        tool = tool,
                        resolution = resolution,
                        isFavorite = tool.id in favorites,
                        onFavorite = { viewModel.toggleFavorite(tool) },
                        onInfo = { infoTool = tool },
                        onClick = {
                            if (resolution is ToolResolution.Blocked) {
                                blockedTool = tool
                            } else {
                                viewModel.recordOpen(tool)
                                onOpenTool(tool.route)
                            }
                        },
                    )
                }
                if (category == null && query.isBlank()) {
                    // Browse view: group by category so every lab family is visible.
                    CATEGORY_ORDER.forEach { cat ->
                        val inCategory = tools.filter { it.category == cat }
                        if (inCategory.isNotEmpty()) {
                            item(key = "cat_${cat.name}") { SectionHeader(categoryLabel(cat)) }
                            items(inCategory, key = { it.id }) { toolItem(it) }
                        }
                    }
                } else {
                    items(tools, key = { it.id }) { toolItem(it) }
                }
            }
        }
    }

    blockedTool?.let { tool ->
        // resolve() re-reads live state on every recomposition — a just-granted
        // permission or a provider going Ready dismisses this automatically.
        val resolution = viewModel.resolve(tool)
        if (resolution is ToolResolution.Blocked) {
            Dialog(onDismissRequest = { blockedTool = null }) {
                UiProblemCard(
                    problem = resolution.toUiProblem(),
                    onRecovery = { action ->
                        when (action) {
                            is RecoveryAction.RequestPermission ->
                                pendingPermission = action.permission
                            RecoveryAction.OpenAppSettings -> context.openAppSettings()
                            RecoveryAction.RequestElevatedAccess -> {
                                blockedTool = null
                                onOpenTool("settings")
                            }
                            is RecoveryAction.Navigate -> {
                                blockedTool = null
                                onOpenTool(action.route)
                            }
                            RecoveryAction.Retry,
                            RecoveryAction.Dismiss,
                            -> blockedTool = null
                        }
                    },
                )
            }
        } else {
            SideEffect { blockedTool = null }
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

/** Accent hue for non-tool search rows — grouping cue only, HELP stays neutral. */
private fun SearchResultType.accentCategory(): ToolCategory? = when (this) {
    SearchResultType.PAGE -> ToolCategory.DEVICE
    SearchResultType.SETTING -> ToolCategory.DEVELOPER
    SearchResultType.DIAGNOSTIC -> ToolCategory.DIAGNOSTICS
    SearchResultType.REPORT -> ToolCategory.REPORTS
    SearchResultType.HELP -> null
}

/** Uniform search-result row: icon, title, contextual subtitle (plan file 08). */
@Composable
private fun SearchResultRow(entry: SearchEntry, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolIcon(
                ToolIcons.forEntry(entry),
                accent = entry.type.accentCategory()?.let { categoryAccent(it) },
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(entry.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    entry.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

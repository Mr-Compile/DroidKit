package io.github.droidkit.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.components.EmptyState
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onOpenDetail: (String) -> Unit,
    onCompare: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val rows by viewModel.rows.collectAsState()
    val filter by viewModel.statusFilter.collectAsState()
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDeleteAll by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                actions = {
                    TextButton(onClick = { menuOpen = true }) { Text("⋮") }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete all reports") },
                            onClick = {
                                menuOpen = false
                                confirmDeleteAll = true
                            },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { viewModel.statusFilter.value = f },
                        label = { Text(f.name.replace('_', ' ').lowercase().replaceFirstChar { c -> c.uppercase() }) },
                    )
                }
                FilterChip(
                    selected = false,
                    onClick = onCompare,
                    label = { Text("Compare") },
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (rows.isEmpty()) {
                    item {
                        EmptyState(
                            if (filter == StatusFilter.ALL) "No saved reports. Run a diagnostic first."
                            else "No ${filter.name.lowercase().replace('_', ' ')} reports.",
                        )
                    }
                }
                items(rows, key = { it.session.id }) { row ->
                    Card(
                        onClick = { onOpenDetail(row.session.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                DateFormat.getDateTimeInstance().format(Date(row.session.startedAtEpochMs)),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                "${row.session.overallStatus} — app v${row.session.appVersion}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            val counts = row.results.groupingBy { it.state }.eachCount()
                            Text(
                                if (counts.isEmpty()) "No test results recorded"
                                else counts.entries.joinToString { "${it.key}: ${it.value}" },
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text("Delete all reports?") },
            text = { Text("Every diagnostic session, result and saved report entry is deleted permanently.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAll()
                        confirmDeleteAll = false
                    },
                ) { Text("Delete all", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAll = false }) { Text("Cancel") }
            },
        )
    }
}

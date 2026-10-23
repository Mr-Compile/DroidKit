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
import io.github.droidkit.core.ui.components.DroidKitAlertDialog
import io.github.droidkit.core.ui.components.DroidKitCard
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import io.github.droidkit.core.ui.components.DroidKitFilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.adaptive.DroidKitListDetail
import io.github.droidkit.core.ui.adaptive.rememberDroidKitWindowInfo
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onOpenDetail: (String) -> Unit,
    onCompare: () -> Unit,
    onBack: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val rows by viewModel.rows.collectAsStateWithLifecycle()
    val filter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val deviceFilter by viewModel.deviceFilter.collectAsStateWithLifecycle()
    val customerFilter by viewModel.customerFilter.collectAsStateWithLifecycle()
    val deviceLabels by viewModel.deviceLabels.collectAsStateWithLifecycle()
    val customerLabels by viewModel.customerLabels.collectAsStateWithLifecycle()
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDeleteAll by remember { mutableStateOf(false) }
    val windowInfo = rememberDroidKitWindowInfo()

    Scaffold(
        topBar = {
            DroidKitTopAppBar(
                title = "Reports",
                onBack = onBack,
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
        if (windowInfo.isExpandedWidth) {
            // Two-pane at expanded: the detail route stays navigable for deep
            // links, but row selection opens the report inside the pane.
            DroidKitListDetail<String>(
                modifier = Modifier.fillMaxSize().padding(padding),
                listPane = { onSelect ->
                    ReportsListContent(
                        filter = filter,
                        rows = rows,
                        onFilter = { viewModel.statusFilter.value = it },
                        deviceFilter = deviceFilter,
                        customerFilter = customerFilter,
                        deviceLabels = deviceLabels,
                        customerLabels = customerLabels,
                        onDeviceFilter = { viewModel.deviceFilter.value = it },
                        onCustomerFilter = { viewModel.customerFilter.value = it },
                        onCompare = onCompare,
                        onSelect = onSelect,
                    )
                },
                detailPane = { sessionId, back ->
                    ReportDetailScreen(
                        sessionId = sessionId,
                        onDeleted = back,
                        onBack = back,
                        viewModel = viewModel,
                        embedded = true,
                    )
                },
            )
        } else {
            ReportsListContent(
                filter = filter,
                rows = rows,
                onFilter = { viewModel.statusFilter.value = it },
                deviceFilter = deviceFilter,
                customerFilter = customerFilter,
                deviceLabels = deviceLabels,
                customerLabels = customerLabels,
                onDeviceFilter = { viewModel.deviceFilter.value = it },
                onCustomerFilter = { viewModel.customerFilter.value = it },
                onCompare = onCompare,
                onSelect = onOpenDetail,
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (confirmDeleteAll) {
        DroidKitAlertDialog(
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

@Composable
private fun ReportsListContent(
    filter: StatusFilter,
    rows: List<ReportRow>,
    onFilter: (StatusFilter) -> Unit,
    deviceFilter: String?,
    customerFilter: String?,
    deviceLabels: List<String>,
    customerLabels: List<String>,
    onDeviceFilter: (String?) -> Unit,
    onCustomerFilter: (String?) -> Unit,
    onCompare: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusFilter.entries.forEach { f ->
                DroidKitFilterChip(
                    selected = filter == f,
                    onClick = { onFilter(f) },
                    label = { Text(f.name.replace('_', ' ').lowercase().replaceFirstChar { c -> c.uppercase() }) },
                )
            }
            DroidKitFilterChip(
                selected = false,
                onClick = onCompare,
                label = { Text("Compare") },
            )
        }
        if (deviceLabels.isNotEmpty() || customerLabels.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (deviceLabels.isNotEmpty()) {
                    LabelFilterChip(
                        title = "Device",
                        options = deviceLabels,
                        selected = deviceFilter,
                        onSelect = onDeviceFilter,
                    )
                }
                if (customerLabels.isNotEmpty()) {
                    LabelFilterChip(
                        title = "Customer",
                        options = customerLabels,
                        selected = customerFilter,
                        onSelect = onCustomerFilter,
                    )
                }
            }
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
                DroidKitCard(
                    onClick = { onSelect(row.session.id) },
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
                        listOfNotNull(
                            row.session.customerLabel,
                            row.session.deviceLabel,
                        ).takeIf { it.isNotEmpty() }?.let {
                            Text(
                                it.joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
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

/** "Device · Shelf A3" chip that opens a dropdown of distinct intake labels. */
@Composable
private fun LabelFilterChip(
    title: String,
    options: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    DroidKitFilterChip(
        selected = selected != null,
        onClick = { open = true },
        label = { Text("$title · ${selected ?: "All"}") },
    )
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        DropdownMenuItem(
            text = { Text("All") },
            onClick = {
                onSelect(null)
                open = false
            },
        )
        options.forEach { label ->
            DropdownMenuItem(
                text = { Text(label) },
                onClick = {
                    onSelect(label)
                    open = false
                },
            )
        }
    }
}

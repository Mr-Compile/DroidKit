package io.github.droidkit.feature.deviceinfo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import io.github.droidkit.core.system.formatBytes
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.SectionHeader
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageLabScreen(
    onBack: () -> Unit,
    viewModel: StorageLabViewModel = hiltViewModel(),
) {
    val live by viewModel.live.collectAsStateWithLifecycle()
    val snapshots by viewModel.snapshots.collectAsStateWithLifecycle()
    val diff by viewModel.diff.collectAsStateWithLifecycle()
    var beforeId by remember { mutableStateOf<String?>(null) }
    var afterId by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { DroidKitTopAppBar("Storage Lab", onBack = onBack) }) { padding ->
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
                Text(
                    "Free space is filesystem-visible space — not NAND health.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            items(live, key = { it.path }) { v ->
                SectionCard(title = v.name) {
                    InfoRow("Path", v.path)
                    InfoRow("State", v.state)
                    InfoRow("Total", formatBytes(v.totalBytes))
                    InfoRow("Free", formatBytes(v.freeBytes))
                    InfoRow("Used", formatBytes(v.totalBytes - v.freeBytes))
                    if (v.totalBytes > 0 && v.freeBytes * 100 / v.totalBytes < 10) {
                        Text(
                            "Low free space — under 10%",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            item { SectionHeader("Snapshots") }
            item {
                Text(
                    "Capture current free/used per volume, then compare two points in " +
                        "time to see what changed (e.g. before/after a cleanup).",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            item {
                OutlinedButton(
                    onClick = { viewModel.saveSnapshot(null) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Capture storage snapshot") }
            }
            if (snapshots.isEmpty()) {
                item { EmptyState("No snapshots yet.") }
            } else {
                item {
                    val df = DateFormat.getDateTimeInstance()
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        snapshots.forEach { s ->
                            FilterChip(
                                selected = s.id == beforeId || s.id == afterId,
                                onClick = {
                                    when {
                                        beforeId == null -> beforeId = s.id
                                        afterId == null && s.id != beforeId -> {
                                            afterId = s.id
                                            viewModel.compare(beforeId!!, s.id)
                                        }
                                        else -> {
                                            beforeId = s.id
                                            afterId = null
                                            viewModel.clearDiff()
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        when (s.id) {
                                            beforeId -> "Before · "
                                            afterId -> "After · "
                                            else -> ""
                                        } + df.format(Date(s.capturedAtEpochMs)),
                                    )
                                },
                            )
                        }
                    }
                }
                diff?.let { (afterSnap, deltas) ->
                    item {
                        SectionHeader("Delta → ${DateFormat.getTimeInstance().format(Date(afterSnap.capturedAtEpochMs))}")
                    }
                    items(deltas, key = { it.name }) { d ->
                        SectionCard(title = d.name) {
                            val delta = d.freeDeltaBytes
                            InfoRow(
                                "Free space",
                                if (delta == null) "—"
                                else "${if (delta >= 0) "+" else "−"}${formatBytes(kotlin.math.abs(delta))}",
                            )
                            if (d.beforeTotal != d.afterTotal) {
                                InfoRow(
                                    "Total changed",
                                    "${d.beforeTotal?.let { formatBytes(it) }} → ${d.afterTotal?.let { formatBytes(it) }}",
                                )
                            }
                        }
                    }
                    item {
                        TextButton(onClick = {
                            beforeId = null
                            afterId = null
                            viewModel.clearDiff()
                        }) { Text("Clear comparison") }
                    }
                }
            }
        }
    }
}

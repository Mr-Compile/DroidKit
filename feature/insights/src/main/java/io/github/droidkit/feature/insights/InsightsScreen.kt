package io.github.droidkit.feature.insights

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.database.InsightRange
import io.github.droidkit.core.database.SessionHealth
import io.github.droidkit.core.database.TestAttention
import io.github.droidkit.core.system.formatBytes
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.SectionHeader
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onOpenTool: (String) -> Unit,
    onStartDiagnostic: () -> Unit,
    onBack: () -> Unit,
    viewModel: InsightsViewModel = hiltViewModel(),
) {
    val health by viewModel.health.collectAsStateWithLifecycle()
    val activity by viewModel.activity.collectAsStateWithLifecycle()

    Scaffold(topBar = { DroidKitTopAppBar("Insights", onBack = onBack) }) { padding ->
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = health.range == InsightRange.DAYS_30,
                        onClick = { viewModel.setRange(InsightRange.DAYS_30) },
                        label = { Text("30 days") },
                    )
                    FilterChip(
                        selected = health.range == InsightRange.ALL,
                        onClick = { viewModel.setRange(InsightRange.ALL) },
                        label = { Text("All time") },
                    )
                }
            }

            item { SectionHeader("Device health") }

            val latest = health.latest
            if (latest == null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("No completed diagnostics in this range.",
                                style = MaterialTheme.typography.bodyMedium)
                            Text("Run a Full Diagnostic to start building health trends.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
                item {
                    Button(onClick = onStartDiagnostic, modifier = Modifier.fillMaxWidth()) {
                        Text("Start Full Diagnostic")
                    }
                }
            } else {
                item { LatestHealthCard(latest, health.previous, health.passDeltaVsPrev) }

                item { SectionHeader("Pass rate per session") }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            PassRateBars(points = health.sessions)
                            Text(
                                "Bar height = share of tests passed. Red = session had a failure.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                }

                item { SectionHeader("Tests needing attention") }
                if (health.attention.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "No failed or skipped tests in range.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                } else {
                    val maxSeverity = health.attention.maxOf { it.severity }
                    items(health.attention, key = { it.testId }) { att ->
                        AttentionCard(att, maxSeverity)
                    }
                }
            }

            item { SectionHeader("Storage") }
            item {
                if (health.storage.size >= 2) {
                    val first = health.storage.first()
                    val last = health.storage.last()
                    val delta = last.freeBytes - first.freeBytes
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            TrendSparkline(points = health.storage)
                            InfoRow("Free space now", formatBytes(last.freeBytes), copyable = false)
                            InfoRow(
                                "Change across ${health.storage.size} snapshots",
                                "${if (delta >= 0) "+" else "−"}${formatBytes(abs(delta))}",
                                copyable = false,
                            )
                            Text(
                                "Internal storage free space per snapshot — captured in Storage Lab.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("No storage trend yet.", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Capture two or more snapshots in Storage Lab to chart free space.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }

            item { SectionHeader("Activity") }
            val tiles = listOf(
                "Diagnostics" to health.completedCount,
                "Tests recorded" to activity.testsRecorded,
                "Tools used" to activity.toolsUsed,
                "NFC scans" to activity.nfcScans,
                "Snapshots" to health.snapshotCount,
                "Reports saved" to activity.reportsSaved,
                "Elevated actions" to activity.elevatedActions,
            )
            tiles.chunked(2).forEach { pair ->
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { (label, n) ->
                            StatTile(label = label, value = n, modifier = Modifier.weight(1f))
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item { SectionHeader("Most used tools") }
            if (activity.topTools.isEmpty()) {
                item { EmptyState("No tool usage yet — open a few tools first.") }
            } else {
                val topMax = activity.topTools.maxOf { it.second }
                items(activity.topTools, key = { it.first.id }) { (tool, n) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTool(tool.route) },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tool.title, modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge)
                                Text("${n}×", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            UsageBar(
                                fraction = n.toFloat() / topMax,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "All stats are computed on-device from your diagnostic sessions and labs. Nothing leaves this phone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun LatestHealthCard(
    latest: SessionHealth,
    previous: SessionHealth?,
    passDeltaVsPrev: Int?,
) {
    val deltaText = when {
        previous == null -> "First session in range"
        passDeltaVsPrev == null -> null
        passDeltaVsPrev > 0 -> "▲ $passDeltaVsPrev more passed than ${fmtDay(previous.startedAtEpochMs)}"
        passDeltaVsPrev < 0 -> "▼ ${-passDeltaVsPrev} fewer passed than ${fmtDay(previous.startedAtEpochMs)}"
        else -> "Same pass count as previous session"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            VerdictDonut(
                pass = latest.pass, fail = latest.fail,
                skip = latest.skip, total = latest.total,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Latest diagnostic", style = MaterialTheme.typography.titleSmall)
                Text(
                    "${latest.pass} of ${latest.total} tests passed",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Text(
                    listOfNotNull(fmtDay(latest.startedAtEpochMs), latest.preset).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
                deltaText?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            passDeltaVsPrev != null && passDeltaVsPrev > 0 -> MaterialTheme.colorScheme.primary
                            passDeltaVsPrev != null && passDeltaVsPrev < 0 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
                Text(
                    "PASS ${latest.pass} · FAIL ${latest.fail} · Skipped/other ${latest.skip}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun AttentionCard(att: TestAttention, maxSeverity: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(att.title, modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge)
                Text(
                    listOfNotNull(
                        if (att.fails > 0) "${att.fails}× fail" else null,
                        if (att.skips > 0) "${att.skips}× skipped/other" else null,
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (att.fails > 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            UsageBar(
                fraction = (att.severity.toFloat() / maxSeverity).coerceAtLeast(0.06f),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

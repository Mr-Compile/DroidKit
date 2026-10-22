package io.github.droidkit.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import io.github.droidkit.core.ui.components.DroidKitButton
import io.github.droidkit.core.ui.components.DroidKitCard
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.system.formatBytes
import io.github.droidkit.core.system.formatHz
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.DroidKitWordmark
import io.github.droidkit.core.ui.components.HeroCard
import io.github.droidkit.core.ui.components.SpecTile
import io.github.droidkit.core.ui.components.StatusDot
import io.github.droidkit.core.ui.theme.DroidKitTheme
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
    onOpenDeviceInfo: () -> Unit,
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
                // mockup .hero — device focus card with gradient hairline,
                // specgrid tiles and a radial brand glow.
                HeroCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenDeviceInfo,
                ) {
                    val probe = viewModel.probe
                    Text(
                        "THIS DEVICE",
                        style = DroidKitTheme.extraTypography.microLabel,
                        color = DroidKitTheme.colors.faint,
                    )
                    Text(
                        probe.model,
                        style = DroidKitTheme.extraTypography.brandDisplay.copy(fontSize = 22.sp),
                        color = DroidKitTheme.colors.textPrimary,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Text(
                        "${probe.manufacturer} ${probe.model} · Android ${probe.androidRelease} (API ${probe.sdkInt})",
                        style = MaterialTheme.typography.bodySmall,
                        color = DroidKitTheme.colors.textSecondary,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                    val soc = listOfNotNull(probe.socManufacturer, probe.socModel)
                        .joinToString(" ")
                        .ifBlank { probe.hardware }
                    val d = probe.displayInfo
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SpecTile("Chipset", soc, Modifier.weight(1f))
                            SpecTile(
                                "Display",
                                "${d.widthPx}×${d.heightPx} · ${formatHz(d.refreshHz)}",
                                Modifier.weight(1f),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SpecTile(
                                "Storage free",
                                formatBytes(probe.storage.freeBytes),
                                Modifier.weight(1f),
                            )
                            SpecTile(
                                "Security patch",
                                probe.securityPatch ?: "Not exposed",
                                Modifier.weight(1f),
                            )
                        }
                    }
                    Text(
                        "Open device overview →",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.4.sp,
                        ),
                        color = DroidKitTheme.colors.brandPrimary,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }

            item {
                DroidKitButton(onClick = onNavigateToDiagnose, modifier = Modifier.fillMaxWidth()) {
                    Text("Start Full Diagnostic")
                }
            }

            item {
                // mockup .hgrid — elevated access + insights side by side.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DroidKitCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "ELEVATED ACCESS",
                                style = DroidKitTheme.extraTypography.microLabel,
                                color = DroidKitTheme.colors.faint,
                            )
                            providerStatuses.forEach { (kind, status) ->
                                val (dot, label) = when (status) {
                                    ProviderStatus.Ready ->
                                        DroidKitTheme.colors.statusSuccess to "ready"
                                    is ProviderStatus.NeedsSetup ->
                                        DroidKitTheme.colors.statusWarning to "setup required"
                                    is ProviderStatus.Unavailable ->
                                        DroidKitTheme.colors.statusUnsupported to status.reason
                                }
                                Row(
                                    modifier = Modifier.padding(top = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    StatusDot(dot)
                                    Text(
                                        "${kind.name}: $label",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DroidKitTheme.colors.textSecondary,
                                        modifier = Modifier.padding(start = 7.dp),
                                    )
                                }
                            }
                        }
                    }
                    latestInsight?.let { h ->
                        DroidKitCard(
                            onClick = onOpenInsights,
                            modifier = Modifier.weight(1f),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "INSIGHTS",
                                    style = DroidKitTheme.extraTypography.microLabel,
                                    color = DroidKitTheme.colors.faint,
                                )
                                Text(
                                    buildString {
                                        append("Latest: ${h.pass}/${h.total} passed")
                                        if (h.fail > 0) append(" · ${h.fail} failed")
                                        append("\n${fmtDay(h.startedAtEpochMs)} →")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DroidKitTheme.colors.textSecondary,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
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
                items(recentTools, key = { "rec_${it.id}" }) { tool ->
                    ToolCard(
                        tool = tool,
                        resolution = remember(tool, providerStatuses) { viewModel.resolve(tool) },
                        onInfo = { infoTool = tool },
                        onClick = { viewModel.recordOpen(tool); onOpenTool(tool.route) },
                    )
                }
            }

            item { SectionHeader("Ready now") }
            items(quickTools, key = { "quick_${it.id}" }) { tool ->
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

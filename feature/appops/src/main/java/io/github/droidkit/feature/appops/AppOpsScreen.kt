package io.github.droidkit.feature.appops

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import io.github.droidkit.core.ui.components.DroidKitCard
import io.github.droidkit.core.ui.components.LoadingState
import androidx.compose.material3.ExperimentalMaterial3Api
import io.github.droidkit.core.ui.components.DroidKitFilterChip
import androidx.compose.material3.MaterialTheme
import io.github.droidkit.core.ui.components.DroidKitAlertDialog
import androidx.compose.material3.TextButton
import io.github.droidkit.core.ui.components.DroidKitTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.privileges.AppOpMode
import io.github.droidkit.core.ui.adaptive.contentWidthCap
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppOpsScreen(
    onOpenAdb: () -> Unit,
    onBack: () -> Unit,
    viewModel: AppOpsViewModel = hiltViewModel(),
) {
    val provider by viewModel.provider.collectAsStateWithLifecycle()
    val identity by viewModel.identity.collectAsStateWithLifecycle()
    val packages by viewModel.packages.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val ops by viewModel.ops.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val lastAction by viewModel.lastAction.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    var pending by remember { mutableStateOf<Triple<String, String, AppOpMode>?>(null) }

    BackHandler(enabled = selected != null) { viewModel.back() }

    Scaffold(
        topBar = {
            DroidKitTopAppBar(
                title = selected ?: "AppOps",
                onBack = if (selected != null) viewModel::back else onBack,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).contentWidthCap()) {
            when {
                provider == null -> NoProviderCard(onOpenAdb)
                selected != null -> OpsDetail(
                    packageName = selected!!,
                    ops = ops,
                    busy = busy,
                    lastAction = lastAction,
                    onPick = { op, mode -> pending = Triple(selected!!, op, mode) },
                )
                else -> PackageList(
                    identity = identity,
                    packages = packages?.filter {
                        query.isBlank() || it.contains(query.trim(), ignoreCase = true)
                    },
                    busy = busy,
                    error = error,
                    query = query,
                    onQuery = { viewModel.query.value = it },
                    onSelect = viewModel::select,
                    providerName = provider?.name ?: "",
                )
            }
        }
    }

    pending?.let { (pkg, op, mode) ->
        DroidKitAlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("Set $op?") },
            text = {
                Text(
                    "This changes how Android treats $pkg for the “$op” operation. " +
                        "Setting $op to ${mode.wire} ${consequence(mode)} " +
                        "The change applies immediately and persists until you change it back.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pending = null
                    viewModel.setMode(pkg, op, mode)
                }) { Text("Set to ${mode.wire}") }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) { Text("Cancel") }
            },
        )
    }
}

private fun consequence(mode: AppOpMode): String = when (mode) {
    AppOpMode.ALLOW -> "lets the app use it normally."
    AppOpMode.IGNORE -> "makes the operation silently fail — the app sees no error, it just gets nothing."
    AppOpMode.DENY -> "denies the operation — the app may crash or show a permission error."
    AppOpMode.DEFAULT -> "restores Android's default decision for this op."
}

@Composable
private fun NoProviderCard(onOpenAdb: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        DroidKitCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Elevated provider required", style = MaterialTheme.typography.titleSmall)
                Text(
                    "AppOps needs an elevated provider — root or a live " +
                        "wireless-ADB session. Grant access in Settings → Advanced access, or open " +
                        "Wireless ADB to pair and connect.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
                TextButton(onClick = onOpenAdb, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Open Wireless ADB")
                }
            }
        }
    }
}

@Composable
private fun PackageList(
    identity: String?,
    packages: List<String>?,
    busy: Boolean,
    error: String?,
    query: String,
    onQuery: (String) -> Unit,
    onSelect: (String) -> Unit,
    providerName: String,
) {
    DroidKitCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Provider: $providerName", style = MaterialTheme.typography.titleSmall)
            if (identity != null) {
                Text(
                    identity,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
    DroidKitTextField(
        value = query,
        onValueChange = onQuery,
        label = { Text("Search packages") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
    when {
        busy -> LoadingState(label = "Loading AppOps…")
        error != null -> EmptyState(error)
        packages == null -> EmptyState("Loading package list…")
        packages.isEmpty() -> EmptyState("No packages match.")
        else -> LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(packages, key = { it }) { pkg ->
                DroidKitCard(onClick = { onSelect(pkg) }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        pkg,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun OpsDetail(
    packageName: String,
    ops: List<AppOpEntry>?,
    busy: Boolean,
    lastAction: String?,
    onPick: (String, AppOpMode) -> Unit,
) {
    if (lastAction != null) {
        Text(
            lastAction,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }
    when {
        busy && ops == null -> LoadingState(label = "Loading ops…")
        ops == null -> EmptyState("No AppOps data for $packageName.")
        ops.isEmpty() -> EmptyState("$packageName has no recorded AppOps entries.")
        else -> {
            var modeFilter by remember { mutableStateOf<String?>(null) }
            val stats = AppOpsInfo.modeStats(ops)
            val shown = ops.filter { modeFilter == null || it.mode == modeFilter }
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Mode summary",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(
                                stats.joinToString("  ·  ") {
                                    "${it.second} ${AppOpsInfo.modeLabel(it.first).lowercase()}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DroidKitFilterChip(
                            selected = modeFilter == null,
                            onClick = { modeFilter = null },
                            label = { Text("All (${ops.size})") },
                        )
                        stats.forEach { (mode, count) ->
                            DroidKitFilterChip(
                                selected = modeFilter == mode,
                                onClick = { modeFilter = if (modeFilter == mode) null else mode },
                                label = { Text("${AppOpsInfo.modeLabel(mode)} ($count)") },
                            )
                        }
                    }
                }
                item {
                    AiSummaryCard(
                        screenTitle = "AppOps for $packageName",
                        fields = buildMap {
                            put("Package", packageName)
                            ops.forEach { put(it.op, it.mode) }
                        },
                    )
                }
                items(shown, key = { it.op }) { entry ->
                    DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    entry.op,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    AppOpsInfo.modeLabel(entry.mode),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Text(
                                AppOpsInfo.describe(entry.op),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                AppOpMode.entries.forEach { mode ->
                                    DroidKitFilterChip(
                                        selected = entry.mode == mode.wire,
                                        onClick = { if (entry.mode != mode.wire) onPick(entry.op, mode) },
                                        label = { Text(mode.wire) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package io.github.droidkit.feature.permissions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.components.DroidKitTopAppBar

/**
 * Special-access matrix — the elevated AppOps grants (overlay, all-files,
 * install-unknown, usage access …) and which apps hold them, mirroring the
 * system "Special app access" settings but consolidated on one screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialAccessScreen(
    onBack: () -> Unit,
    viewModel: PermissionCenterViewModel = hiltViewModel(),
) {
    var matrix by remember { mutableStateOf<Map<SpecialOp, List<PermAppEntry>>?>(null) }
    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) { matrix = viewModel.loadSpecialAccess() }

    Scaffold(topBar = { DroidKitTopAppBar("Special access", onBack = onBack) }) { padding ->
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
                    "AppOps checks report the effective mode — \"default\" for " +
                        "overlay-style ops counts as allowed, matching what the " +
                        "system shows in Special app access. Apps without a " +
                        "launcher icon are not visible to DroidKit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val m = matrix
            if (m == null) {
                item {
                    Text(
                        "Checking AppOps modes…",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            } else {
                items(m.entries.toList(), key = { it.key.op }) { (op, apps) ->
                    val open = expanded[op.op] == true
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded[op.op] = !open },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(op.label, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        op.blurb,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    "${apps.size}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (apps.isEmpty()) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                )
                            }
                            if (open && apps.isNotEmpty()) {
                                apps.forEach { app ->
                                    Column(modifier = Modifier.padding(top = 6.dp)) {
                                        Text(
                                            app.label,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        Text(
                                            app.packageName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
}

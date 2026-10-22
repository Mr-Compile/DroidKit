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
import io.github.droidkit.core.ui.components.DroidKitCard
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
 * Reverse permission view — which installed apps declare each dangerous
 * permission and whether it is currently granted. Read-only audit aid.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsByPermissionScreen(
    onBack: () -> Unit,
    viewModel: PermissionCenterViewModel = hiltViewModel(),
) {
    var groups by remember { mutableStateOf<List<PermGroup>?>(null) }
    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) { groups = viewModel.loadAppsByPermission() }

    Scaffold(topBar = { DroidKitTopAppBar("Apps by permission", onBack = onBack) }) { padding ->
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
                    "Dangerous permissions declared by installed apps — " +
                        "most-granted first. Tap a row to see which apps hold " +
                        "it. Apps without a launcher icon are not visible to " +
                        "DroidKit (Android 11+ package visibility).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val g = groups
            if (g == null) {
                item {
                    Text(
                        "Scanning installed packages…",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            } else {
                items(g, key = { it.permission }) { group ->
                    val open = expanded[group.permission] == true
                    DroidKitCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded[group.permission] = !open },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        PermissionInfo.shortName(group.permission),
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    Text(
                                        group.permission,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    "${group.grantedCount}/${group.apps.size}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            if (open) {
                                group.apps.forEach { app ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
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
                                        Text(
                                            if (app.granted) "Granted" else "Denied",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (app.granted) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
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

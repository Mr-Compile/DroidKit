package io.github.droidkit.feature.packages

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.adaptive.contentWidthCap
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagesScreen(
    onOpenAdb: () -> Unit,
    onBack: () -> Unit,
    viewModel: PackagesViewModel = hiltViewModel(),
) {
    val elevated by viewModel.elevated.collectAsStateWithLifecycle()
    val packages by viewModel.packages.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()

    var pending by remember { mutableStateOf<Pair<PackagesViewModel.Action, String>?>(null) }
    var permTarget by remember { mutableStateOf<String?>(null) }
    var restoreTarget by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(events.size) {
        events.firstOrNull()?.let {
            when (it) {
                is PackagesViewModel.Event.Message -> snackbar.showSnackbar(it.text)
            }
            viewModel.consumeEvent(it)
        }
    }

    Scaffold(
        topBar = { DroidKitTopAppBar("Package Controls", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).contentWidthCap(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SectionCard(title = "Elevated access") {
                    when (val e = elevated) {
                        PackagesViewModel.ElevatedState.Loading ->
                            Text("Checking providers…", style = MaterialTheme.typography.bodyMedium)
                        is PackagesViewModel.ElevatedState.Ready ->
                            InfoRow("Active provider", e.kind.name, copyable = false)
                        PackagesViewModel.ElevatedState.Unavailable -> {
                            Text(
                                "Package controls need an elevated provider — " +
                                    "root or a connected wireless ADB session.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    scope.launch { viewModel.requestAccess() }
                                }) { Text("Grant root access") }
                                OutlinedButton(onClick = onOpenAdb) { Text("Wireless ADB") }
                            }
                        }
                    }
                }
            }

            if (elevated is PackagesViewModel.ElevatedState.Ready) {
                item {
                    SectionCard(title = "Restore removed package") {
                        Text(
                            "Enter a package name removed for this user to restore it " +
                                "(pm install-existing).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = restoreTarget,
                                onValueChange = { restoreTarget = it },
                                label = { Text("package.name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            OutlinedButton(
                                onClick = {
                                    viewModel.runAction(
                                        PackagesViewModel.Action.RESTORE,
                                        restoreTarget.trim(),
                                    )
                                },
                                enabled = restoreTarget.isNotBlank() && !busy,
                            ) { Text("Restore") }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.query.value = it },
                        label = { Text("Filter packages") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }

                val filtered = packages.filter {
                    query.isBlank() ||
                        it.label.contains(query, true) ||
                        it.packageName.contains(query, true)
                }.take(30)
                items(filtered, key = { it.packageName }) { pkg ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(pkg.label, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                pkg.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 8.dp),
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        pending = PackagesViewModel.Action.DISABLE to pkg.packageName
                                    },
                                    enabled = !busy,
                                ) { Text("Disable") }
                                OutlinedButton(
                                    onClick = {
                                        pending = PackagesViewModel.Action.CLEAR_DATA to pkg.packageName
                                    },
                                    enabled = !busy,
                                ) { Text("Clear data") }
                                OutlinedButton(
                                    onClick = {
                                        pending = PackagesViewModel.Action.UNINSTALL to pkg.packageName
                                    },
                                    enabled = !busy,
                                ) { Text("Uninstall") }
                                OutlinedButton(
                                    onClick = {
                                        permTarget = pkg.packageName
                                        viewModel.loadPermissions(pkg.packageName)
                                    },
                                    enabled = !busy,
                                ) { Text("Perms") }
                            }
                        }
                    }
                }
            }
        }
    }

    // Every mutating action previews its consequence before running; the
    // confirm button names the action itself — no vague "Confirm"/"OK".
    pending?.let { (action, pkg) ->
        val (title, body, label, destructive) = when (action) {
            PackagesViewModel.Action.DISABLE ->
                ConfirmSpec(
                    "Disable $pkg?",
                    "The app will stop running and disappear from launchers. " +
                        "Reversible — re-enable it later from its App Info page.",
                    "Disable",
                    destructive = false,
                )
            PackagesViewModel.Action.ENABLE ->
                ConfirmSpec(
                    "Enable $pkg?",
                    "The app becomes runnable and appears in launchers again.",
                    "Enable",
                    destructive = false,
                )
            PackagesViewModel.Action.CLEAR_DATA ->
                ConfirmSpec(
                    "Clear data for $pkg?",
                    "Permanently wipes all of the app's data: accounts, settings, " +
                        "files, databases. This cannot be undone.",
                    "Clear data",
                    destructive = true,
                )
            PackagesViewModel.Action.UNINSTALL ->
                ConfirmSpec(
                    "Uninstall $pkg for this user?",
                    "Removed for the current user profile only; other users keep it. " +
                        "For preinstalled apps it can be restored with " +
                        "'pm install-existing'.",
                    "Uninstall",
                    destructive = true,
                )
            PackagesViewModel.Action.RESTORE ->
                ConfirmSpec(
                    "Restore $pkg for this user?",
                    "Reinstalls the preinstalled package for the current user " +
                        "profile ('pm install-existing').",
                    "Restore",
                    destructive = false,
                )
        }
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text(title) },
            text = { Text(body) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.runAction(action, pkg)
                        pending = null
                    },
                    enabled = !busy,
                ) {
                    Text(
                        label,
                        color = if (destructive) MaterialTheme.colorScheme.error
                        else Color.Unspecified,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) { Text("Cancel") }
            },
        )
    }

    // Runtime permission sheet — grant/revoke goes through the elevated provider.
    permTarget?.let { pkg ->
        val perms by viewModel.perms.collectAsStateWithLifecycle()
        AlertDialog(
            onDismissRequest = {
                permTarget = null
                viewModel.clearPermissions()
            },
            title = { Text("Runtime permissions — $pkg") },
            text = {
                Column {
                    when {
                        perms == null -> Text("Loading…")
                        perms!!.isEmpty() -> Text("No runtime permissions declared.")
                        else -> perms!!.forEach { (perm, granted) ->
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    perm.substringAfterLast('.'),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(
                                    onClick = {
                                        viewModel.setPermission(pkg, perm, !granted)
                                    },
                                    enabled = !busy,
                                ) { Text(if (granted) "Revoke" else "Grant") }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    permTarget = null
                    viewModel.clearPermissions()
                }) { Text("Close") }
            },
        )
    }
}

/** Title, consequence copy, explicit confirm label and destructive styling for a package action. */
private data class ConfirmSpec(
    val title: String,
    val body: String,
    val label: String,
    val destructive: Boolean,
)

package io.github.droidkit.feature.files

import android.content.Intent
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import io.github.droidkit.core.ui.components.DroidKitAlertDialog
import io.github.droidkit.core.ui.components.DroidKitCard
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import io.github.droidkit.core.ui.components.DroidKitOutlinedButton
import io.github.droidkit.core.ui.components.DroidKitTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.droidkit.core.ui.adaptive.contentWidthCap
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.LoadingState
import io.github.droidkit.core.ui.components.SetupRequiredState
import java.text.DateFormat
import java.util.Date

@Composable
fun FilesScreen(
    onBack: () -> Unit,
    onOpenAdbSetup: () -> Unit,
    viewModel: FilesViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val path by viewModel.path.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val providerReady by viewModel.providerReady.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val copying by viewModel.copying.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val search by viewModel.search.collectAsStateWithLifecycle()
    val preview by viewModel.preview.collectAsStateWithLifecycle()
    val safRoot by viewModel.safRoot.collectAsStateWithLifecycle()
    val safEntries by viewModel.safEntries.collectAsStateWithLifecycle()
    val safCanUp by viewModel.safCanUp.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val snackbar = remember { SnackbarHostState() }

    val safPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
            viewModel.safGrant(it)
        }
    }

    LaunchedEffect(events) {
        (events.firstOrNull() as? FilesViewModel.Event.Message)?.let {
            snackbar.showSnackbar(it.text)
            viewModel.consumeEvent(it)
        }
    }

    Scaffold(
        topBar = { DroidKitTopAppBar(title = "Storage browser", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .contentWidthCap(),
        ) {
            when {
                safRoot != null -> SafPane(
                    root = safRoot!!,
                    entries = safEntries,
                    canUp = safCanUp,
                    onUp = viewModel::safUp,
                    onOpen = viewModel::safOpen,
                    onClose = viewModel::safClose,
                )
                providerReady == false -> Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SetupRequiredState(
                        title = "Elevated access needed",
                        instructions = "Browsing Android/data runs as the shell user. " +
                            "Set up wireless ADB (or grant root) to continue — or browse " +
                            "shared storage through the system picker instead.",
                        actionLabel = "Set up wireless ADB",
                        onAction = onOpenAdbSetup,
                    )
                    DroidKitOutlinedButton(onClick = { safPicker.launch(null) }) {
                        Text("Browse via Storage Access Framework")
                    }
                    Text(
                        "SAF grants read access to a folder you pick — no ADB needed, " +
                            "but Android/data stays off-limits on Android 11+.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> {
                    PathBar(
                        path = path,
                        canGoUp = path != "/sdcard" && path != "/storage/emulated/0",
                        onUp = viewModel::up,
                        onJump = { viewModel.open(it) },
                        onSaf = { safPicker.launch(null) },
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DroidKitTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search names under here") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = { viewModel.runSearch(searchQuery) },
                                    enabled = searchQuery.isNotBlank() && !loading,
                                ) { Icon(Icons.Outlined.Search, contentDescription = "Search") }
                            },
                        )
                        if (search != null) {
                            TextButton(onClick = {
                                searchQuery = ""
                                viewModel.clearSearch()
                            }) { Text("Clear") }
                        }
                    }
                    if (selected.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "${selected.size} selected",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(
                                onClick = { viewModel.copySelected() },
                                enabled = copying == null,
                            ) { Text(if (copying == "batch") "Copying…" else "Copy all") }
                            TextButton(onClick = { viewModel.clearSelection() }) { Text("Clear") }
                        }
                    }
                    HorizontalDivider()
                    val shown = search ?: entries
                    when {
                        loading -> LoadingState(label = "Reading…")
                        error != null -> Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(error!!, style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { viewModel.open(path) }) { Text("Retry") }
                        }
                        shown.isEmpty() -> Text(
                            if (search != null) "No matches" else "Empty folder",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp),
                        )
                        else -> LazyColumn(
                            contentPadding = PaddingValues(vertical = 4.dp),
                        ) {
                            items(shown, key = { it.path }) { entry ->
                                EntryRow(
                                    entry = entry,
                                    checked = entry.path in selected,
                                    copying = copying == entry.path,
                                    previewable = !entry.isDir && FilesInfo.looksTextual(entry.name),
                                    sizeLabel = formatMeta(context, entry),
                                    onCheck = { viewModel.toggleSelect(entry) },
                                    onOpen = {
                                        if (entry.isDir) viewModel.open(entry.path)
                                        else viewModel.previewFile(entry)
                                    },
                                    onPreview = { viewModel.previewFile(entry) },
                                    onCopy = { viewModel.copyOut(entry) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    preview?.let { p ->
        DroidKitAlertDialog(
            onDismissRequest = { viewModel.clearPreview() },
            title = { Text(p.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            text = {
                if (p.binary) {
                    Text(
                        "Binary content — no text preview. Copy it out and open " +
                            "it in another app instead.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Column {
                        Text(
                            p.text,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .heightIn(max = 320.dp)
                                .verticalScroll(rememberScrollState()),
                        )
                        if (p.truncated) {
                            Text(
                                "Showing the first bytes only.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearPreview() }) { Text("Close") }
            },
        )
    }
}

@Composable
private fun SafPane(
    root: android.net.Uri,
    entries: List<FilesViewModel.SafEntry>,
    canUp: Boolean,
    onUp: () -> Unit,
    onOpen: (FilesViewModel.SafEntry) -> Unit,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onUp, enabled = canUp) {
            Icon(Icons.Outlined.ArrowUpward, contentDescription = "Up one level")
        }
        Text(
            root.lastPathSegment ?: "SAF tree",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onClose) { Text("Close SAF") }
    }
    Text(
        "Read-only system-picker browse — copy out uses the elevated browser.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
    HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    if (entries.isEmpty()) {
        Text(
            "Empty or unavailable",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(24.dp),
        )
    } else {
        val context = LocalContext.current
        LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
            items(entries, key = { it.uri.toString() }) { entry ->
                ListItem(
                    headlineContent = {
                        Text(entry.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    supportingContent = {
                        Text(
                            if (entry.isDir) "Folder"
                            else Formatter.formatShortFileSize(context, entry.size),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    leadingContent = {
                        Icon(
                            if (entry.isDir) Icons.Outlined.Folder
                            else Icons.AutoMirrored.Outlined.InsertDriveFile,
                            contentDescription = null,
                        )
                    },
                    modifier = Modifier.clickable(enabled = entry.isDir) { onOpen(entry) },
                )
            }
        }
    }
}

@Composable
private fun PathBar(
    path: String,
    canGoUp: Boolean,
    onUp: () -> Unit,
    onJump: (String) -> Unit,
    onSaf: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onUp, enabled = canGoUp) {
            Icon(Icons.Outlined.ArrowUpward, contentDescription = "Up one level")
        }
        Text(
            path,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = { onJump("/sdcard/Android/data") }) { Text("data") }
        TextButton(onClick = { onJump("/sdcard/Android/obb") }) { Text("obb") }
        TextButton(onClick = onSaf) { Text("SAF") }
    }
}

@Composable
private fun EntryRow(
    entry: FilesViewModel.Entry,
    checked: Boolean,
    copying: Boolean,
    previewable: Boolean,
    sizeLabel: String,
    onCheck: () -> Unit,
    onOpen: () -> Unit,
    onPreview: () -> Unit,
    onCopy: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(entry.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(
                if (entry.isDir) "Folder" else sizeLabel,
                style = MaterialTheme.typography.bodySmall,
            )
        },
        leadingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checked, onCheckedChange = { onCheck() })
                Icon(
                    if (entry.isDir) {
                        Icons.Outlined.Folder
                    } else {
                        Icons.AutoMirrored.Outlined.InsertDriveFile
                    },
                    contentDescription = null,
                )
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (copying) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(12.dp),
                        strokeWidth = 2.dp,
                    )
                }
                if (previewable) {
                    IconButton(onClick = onPreview) {
                        Icon(Icons.Outlined.Visibility, contentDescription = "Preview")
                    }
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Outlined.Download, contentDescription = "Copy to Downloads")
                }
            }
        },
        modifier = Modifier.clickable(onClick = onOpen),
    )
}

private fun formatMeta(context: android.content.Context, entry: FilesViewModel.Entry): String {
    val size = Formatter.formatShortFileSize(context, entry.size)
    return if (entry.modifiedEpochSec > 0) {
        val date = DateFormat.getDateInstance(DateFormat.MEDIUM)
            .format(Date(entry.modifiedEpochSec * 1000))
        "$size · $date"
    } else {
        size
    }
}

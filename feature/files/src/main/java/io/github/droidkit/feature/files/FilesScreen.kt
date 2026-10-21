package io.github.droidkit.feature.files

import android.text.format.Formatter
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val snackbar = remember { SnackbarHostState() }

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
                providerReady == false -> SetupRequiredState(
                    title = "Elevated access needed",
                    instructions = "Browsing Android/data runs as the shell user. " +
                        "Set up wireless ADB (or grant root) to continue.",
                    actionLabel = "Set up wireless ADB",
                    onAction = onOpenAdbSetup,
                )
                else -> {
                    PathBar(
                        path = path,
                        canGoUp = path != "/sdcard" && path != "/storage/emulated/0",
                        onUp = { viewModel.up() },
                        onJump = { viewModel.open(it) },
                    )
                    HorizontalDivider()
                    when {
                        loading -> LoadingState(label = "Reading $path")
                        error != null -> Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(error!!, style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { viewModel.open(path) }) { Text("Retry") }
                        }
                        entries.isEmpty() -> Text(
                            "Empty folder",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp),
                        )
                        else -> LazyColumn(
                            contentPadding = PaddingValues(vertical = 4.dp),
                        ) {
                            items(entries, key = { it.path }) { entry ->
                                EntryRow(
                                    entry = entry,
                                    copying = copying == entry.path,
                                    sizeLabel = formatMeta(context, entry),
                                    onOpen = { viewModel.open(entry.path) },
                                    onCopy = { viewModel.copyOut(entry) },
                                )
                            }
                        }
                    }
                }
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
    }
}

@Composable
private fun EntryRow(
    entry: FilesViewModel.Entry,
    copying: Boolean,
    sizeLabel: String,
    onOpen: () -> Unit,
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
            Icon(
                if (entry.isDir) {
                    Icons.Outlined.Folder
                } else {
                    Icons.AutoMirrored.Outlined.InsertDriveFile
                },
                contentDescription = null,
            )
        },
        trailingContent = {
            if (copying) {
                CircularProgressIndicator(modifier = Modifier.padding(12.dp), strokeWidth = 2.dp)
            } else {
                IconButton(onClick = onCopy) {
                    Icon(Icons.Outlined.Download, contentDescription = "Copy to Downloads")
                }
            }
        },
        modifier = Modifier.clickable(enabled = entry.isDir, onClick = onOpen),
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

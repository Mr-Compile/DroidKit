package io.github.droidkit.core.ui

import android.content.ClipData
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch

/**
 * Icon-only clipboard action — replaces former "Copy" text buttons.
 * [copyText] is evaluated on tap; a null result is a no-op.
 */
@Composable
fun CopyIconButton(copyText: () -> String?, description: String) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    IconButton(
        onClick = {
            copyText()?.let { text ->
                scope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(description, text)))
                }
            }
        },
    ) {
        Icon(Icons.Outlined.ContentCopy, contentDescription = description)
    }
}

/** App-bar "copy all" action for screens that present copyable sections. */
@Composable
fun CopyAllAction(copyText: () -> String?) {
    val description = stringResource(R.string.dk_copy_all)
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    IconButton(
        onClick = {
            copyText()?.let { text ->
                scope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(description, text)))
                }
            }
        },
    ) {
        Icon(Icons.Outlined.CopyAll, contentDescription = description)
    }
}

/**
 * Serializes titled label/value sections into plain text for "copy all":
 * `[Title]` header line followed by `label: value` rows, blank-line separated.
 */
fun formatSectionsForCopy(
    sections: List<Pair<String, List<Pair<String, String?>>>>,
    notExposed: String = "Not exposed",
): String = sections.joinToString("\n\n") { (title, rows) ->
    buildString {
        appendLine("[$title]")
        rows.forEach { (label, value) -> appendLine("$label: ${value ?: notExposed}") }
    }.trimEnd()
}

package io.github.droidkit.core.ui

import android.content.ClipData
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun InfoRow(label: String, value: String?, copyable: Boolean = true) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val shown = value ?: stringResource(R.string.dk_not_exposed)
    val copyDescription = stringResource(R.string.dk_copy_to_clipboard, label)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(
            shown,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
        )
        if (copyable && value != null) {
            TextButton(
                onClick = {
                    scope.launch {
                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(label, "$label: $value")))
                    }
                },
                modifier = Modifier.semantics { contentDescription = copyDescription },
            ) {
                Text(stringResource(R.string.dk_copy))
            }
        }
    }
}

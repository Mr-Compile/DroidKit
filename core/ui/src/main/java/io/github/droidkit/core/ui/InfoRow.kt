package io.github.droidkit.core.ui

import android.content.ClipData
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.droidkit.core.ui.theme.DroidKitTheme
import kotlinx.coroutines.launch

/**
 * Key/value row (mockup `.inforow`): subdued label, right-aligned monospace
 * technical value, bordered `.copybtn` affordance.
 */
@Composable
fun InfoRow(label: String, value: String?, copyable: Boolean = true) {
    val c = DroidKitTheme.colors
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val shown = value ?: stringResource(R.string.dk_not_exposed)
    val copyDescription = stringResource(R.string.dk_copy_to_clipboard, label)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = c.textSecondary,
        )
        Text(
            shown,
            modifier = Modifier.weight(1.2f),
            style = DroidKitTheme.extraTypography.technicalValue.let {
                if (value == null) it.copy(fontStyle = FontStyle.Italic) else it
            },
            color = if (value == null) c.faint else c.textPrimary,
            textAlign = TextAlign.End,
        )
        if (copyable && value != null) {
            Surface(
                onClick = {
                    scope.launch {
                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(label, "$label: $value")))
                    }
                },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .semantics { contentDescription = copyDescription },
                shape = RoundedCornerShape(6.dp),
                color = androidx.compose.ui.graphics.Color.Transparent,
                border = BorderStroke(1.dp, c.lineNeutral),
            ) {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    tint = c.brandPrimary,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .size(13.dp),
                )
            }
        }
    }
}

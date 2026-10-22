package io.github.droidkit.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.privileges.BlockedReason
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * Section header (mockup `.sec-head`): 11px gradient tick, letterspaced
 * uppercase faint label. Still exposes a semantic heading for accessibility.
 */
@Composable
fun SectionHeader(text: String) {
    val c = DroidKitTheme.colors
    Row(
        modifier = Modifier
            .padding(start = 2.dp, end = 2.dp, top = 12.dp, bottom = 6.dp)
            .semantics { heading() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(11.dp)
                .height(2.dp)
                .background(c.brandGradient, RoundedCornerShape(1.dp)),
        )
        Text(
            text = text.uppercase(),
            style = DroidKitTheme.extraTypography.sectionLabel,
            color = c.faint,
            modifier = Modifier.padding(start = 9.dp),
        )
    }
}

/** Empty state (mockup `.empty`); optionally offers the one correct next action. */
@Composable
fun EmptyState(text: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    val c = DroidKitTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder(c.lineNeutral, cornerRadius = 12.dp)
            .padding(horizontal = 16.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = c.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

fun BlockedReason.describe(): String = when (this) {
    is BlockedReason.MissingPermission -> "Permission required: $permission"
    is BlockedReason.MissingHardware -> "Hardware missing: $feature"
    is BlockedReason.RequiresApi -> "Requires Android API $minSdk+"
    is BlockedReason.NeedsProvider -> buildString {
        append("Needs ${kind.name} provider: ")
        append(
            when (val s = status) {
                ProviderStatus.Ready -> "ready"
                is ProviderStatus.NeedsSetup -> "setup required — ${s.instructions}"
                is ProviderStatus.Unavailable -> s.reason
            },
        )
    }
}

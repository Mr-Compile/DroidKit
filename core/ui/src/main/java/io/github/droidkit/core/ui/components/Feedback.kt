package io.github.droidkit.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.privileges.BlockedReason
import io.github.droidkit.core.privileges.ProviderStatus

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics { heading() },
    )
}

@Composable
fun EmptyState(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
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


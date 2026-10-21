package io.github.droidkit.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.BlockedReason
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.theme.categoryAccent

/**
 * Reason-aware availability badge (UI v6 plan files 11/22): the label names
 * the dominant blocking reason instead of a generic "Blocked", mapped through
 * the same priority as the problem dialog (provider > permission > device).
 */
@Composable
fun AvailabilityBadge(resolution: ToolResolution, modifier: Modifier = Modifier) {
    val (kind, label) = availabilityPresentation(resolution)
    StatusBadge(kind, label, modifier)
}

private fun availabilityPresentation(resolution: ToolResolution): Pair<StatusKind, String> =
    when (resolution) {
        ToolResolution.Ready -> StatusKind.Info to "Ready"
        is ToolResolution.Blocked -> when (val reason = dominantReason(resolution.reasons)) {
            is BlockedReason.NeedsProvider -> when (reason.status) {
                is ProviderStatus.Unavailable ->
                    StatusKind.Unsupported to "Unavailable"
                else -> StatusKind.SetupRequired to "Setup required"
            }
            is BlockedReason.MissingPermission ->
                StatusKind.SetupRequired to "Permission required"
            is BlockedReason.MissingHardware ->
                StatusKind.Unsupported to "Not supported"
            is BlockedReason.RequiresApi ->
                StatusKind.Unsupported to "Needs API ${reason.minSdk}+"
            null -> StatusKind.Neutral to "Unavailable"
        }
    }

/** Same ordering the UiProblem mapper uses: provider > permission > device. */
private fun dominantReason(reasons: List<BlockedReason>): BlockedReason? =
    reasons.maxByOrNull {
        when (it) {
            is BlockedReason.NeedsProvider -> 3
            is BlockedReason.MissingPermission -> 2
            else -> 1
        }
    }

/**
 * Standard tool card (UI v6 plan file 05): semantic icon, name, short
 * description, category/release subtitle, one primary status badge and the
 * affordances. Detailed requirements live in [ToolInfoDialog], not the card.
 */
@Composable
fun ToolCard(
    tool: Tool,
    resolution: ToolResolution,
    isFavorite: Boolean = false,
    onFavorite: (() -> Unit)? = null,
    onInfo: (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    // stateDescription lets TalkBack announce e.g. "NFC Lab, not supported"
    // (plan file 19); the visible badge label shares the same mapping.
    val availabilityLabel = availabilityPresentation(resolution).second.lowercase()
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { stateDescription = availabilityLabel },
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolIcon(ToolIcons.forTool(tool), accent = categoryAccent(tool.category))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(tool.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    buildString {
                        append(
                            tool.category.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                        )
                        if (tool.phase != ToolPhase.V1) append(" · ${tool.phase.name}")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // The badge lives inside the weighted column so it can never
                // starve the text: it only makes the card taller, not narrower.
                AvailabilityBadge(
                    resolution,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (onInfo != null) {
                IconButton(onClick = onInfo) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "About ${tool.title}",
                    )
                }
            }
            if (onFavorite != null) {
                IconButton(onClick = onFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = if (isFavorite) {
                            "Remove ${tool.title} from favorites"
                        } else {
                            "Add ${tool.title} to favorites"
                        },
                    )
                }
            }
        }
    }
}

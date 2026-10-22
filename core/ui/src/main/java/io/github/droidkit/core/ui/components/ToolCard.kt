package io.github.droidkit.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.ui.unit.sp
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.BlockedReason
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.theme.DroidKitTheme
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
        ToolResolution.Ready -> StatusKind.Success to "Ready"
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
 * Standard tool card (mockup `.card` tool rows + plan file 05): `.tic` icon,
 * title + availability badge on one row, short description, letterspaced
 * `.toolmeta` category/release line, info + favorite affordances.
 * Detailed requirements live in [ToolInfoDialog], not the card.
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
    val c = DroidKitTheme.colors
    // stateDescription lets TalkBack announce e.g. "NFC Lab, not supported"
    // (plan file 19); the visible badge label shares the same mapping.
    val availabilityLabel = availabilityPresentation(resolution).second.lowercase()
    DroidKitCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { stateDescription = availabilityLabel },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolIcon(ToolIcons.forTool(tool), accent = categoryAccent(tool.category))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        tool.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.5.sp),
                        color = c.textPrimary,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    AvailabilityBadge(
                        resolution,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Text(
                    tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    buildString {
                        append(
                            tool.category.name.uppercase(),
                        )
                        if (tool.phase != ToolPhase.V1) append(" · ${tool.phase.name}")
                    },
                    style = DroidKitTheme.extraTypography.microLabel,
                    color = c.faint,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
            if (onInfo != null) {
                IconButton(onClick = onInfo) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "About ${tool.title}",
                        tint = c.textSecondary,
                        modifier = Modifier.size(18.dp),
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
                        tint = if (isFavorite) c.statusWarning else c.raise,
                    )
                }
            }
        }
    }
}

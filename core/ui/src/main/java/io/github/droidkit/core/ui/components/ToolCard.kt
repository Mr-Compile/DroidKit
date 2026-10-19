package io.github.droidkit.core.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution

/**
 * Non-interactive label chip. Real chips advertise click semantics to
 * assistive tech — these are status tags, so they must not.
 */
@Composable
private fun TagChip(label: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun AvailabilityBadge(resolution: ToolResolution) {
    val (label, color) = when (resolution) {
        ToolResolution.Ready -> "Ready" to MaterialTheme.colorScheme.primary
        is ToolResolution.Blocked -> "Blocked" to MaterialTheme.colorScheme.error
    }
    TagChip(label, color)
}

@Composable
fun ToolCard(
    tool: Tool,
    resolution: ToolResolution,
    isFavorite: Boolean = false,
    onFavorite: (() -> Unit)? = null,
    onInfo: (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tool.title, style = MaterialTheme.typography.titleMedium)
                Text(tool.description, style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TagChip(tool.category.name, MaterialTheme.colorScheme.onSurfaceVariant)
                    if (tool.safetyLevel != SafetyLevel.SAFE) {
                        TagChip("L${tool.safetyLevel.ordinal}", MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (tool.phase != ToolPhase.V1) {
                        TagChip(tool.phase.name, MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            AvailabilityBadge(resolution)
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

package io.github.droidkit.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * Per-tool "Learn more" surface — every field is rendered from real [Tool]
 * metadata and the live [ToolResolution], so nothing here can advertise
 * behavior the tool does not have.
 */
@Composable
fun ToolInfoDialog(
    tool: Tool,
    resolution: ToolResolution,
    onDismiss: () -> Unit,
) {
    val c = DroidKitTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        DroidKitCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = c.raise),
            border = BorderStroke(1.dp, c.lineNeutral),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(tool.title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                Text(tool.description, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                DroidKitHairline()

                ToolInfoRow("Category", tool.category.name.lowercase().replaceFirstChar { it.uppercase() })
                ToolInfoRow(
                    "Availability",
                    when (resolution) {
                        ToolResolution.Ready -> "Ready to use"
                        is ToolResolution.Blocked ->
                            resolution.reasons.joinToString("\n") { "• ${it.describe()}" }
                    },
                )
                if (tool.safetyLevel != SafetyLevel.SAFE) {
                    ToolInfoRow(
                        "Impact level",
                        "Level ${tool.safetyLevel.ordinal} — ${tool.safetyLevel.name.lowercase().replace('_', ' ')}",
                    )
                }
                if (tool.requiredCapabilities.any { it != Capability.STANDARD_API }) {
                    ToolInfoRow(
                        "Elevated access",
                        "Uses " + tool.requiredCapabilities
                            .filter { it != Capability.STANDARD_API }
                            .joinToString { it.name.lowercase().replace('_', ' ') },
                    )
                }
                if (tool.requiredPermissions.isNotEmpty()) {
                    ToolInfoRow(
                        "Permissions",
                        tool.requiredPermissions.joinToString("\n") {
                            it.substringAfterLast('.')
                        },
                    )
                }
                if (tool.requiredFeatures.isNotEmpty()) {
                    ToolInfoRow(
                        "Hardware required",
                        tool.requiredFeatures.joinToString("\n") { it.substringAfterLast('.') },
                    )
                }
                tool.minSdk?.let { ToolInfoRow("Android version", "API $it or newer") }
                ToolInfoRow(
                    "Internet",
                    if ("android.permission.INTERNET" in tool.requiredPermissions) {
                        "Used by this tool"
                    } else {
                        "Not required — works offline"
                    },
                )
                if (tool.phase != ToolPhase.V1) {
                    ToolInfoRow("Release", tool.phase.name)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}

@Composable
private fun ToolInfoRow(label: String, value: String) {
    val c = DroidKitTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label.uppercase(),
            style = DroidKitTheme.extraTypography.microLabel,
            color = c.faint,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = c.textSecondary,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

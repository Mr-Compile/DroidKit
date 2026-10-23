package io.github.droidkit.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * V5 risk components (design-system Task 5) — risk classification, consequence
 * surfacing and the shared confirmation sheet for mutating operations.
 */

/**
 * Risk level per `playbook/24`: 0 safe/read-only, 1 minor, 2 system change,
 * 3 high impact/destructive, 4 advanced. Levels ≥ [High] render error styling.
 */
enum class RiskLevel(val level: Int, val label: String, val kind: StatusKind) {
    Safe(0, "Safe · read-only", StatusKind.Neutral),
    Minor(1, "Minor", StatusKind.Info),
    System(2, "System change", StatusKind.Warning),
    High(3, "High impact", StatusKind.Error),
    Advanced(4, "Advanced", StatusKind.Error),
    ;

    val destructive: Boolean get() = this >= High

    companion object {
        fun of(level: Int): RiskLevel = entries.firstOrNull { it.level == level } ?: Safe
    }
}

/** "L2 · System change" pill — wording always accompanies color. */
@Composable
fun RiskBadge(level: RiskLevel, modifier: Modifier = Modifier) {
    StatusBadge(level.kind, "L${level.level} · ${level.label}", modifier)
}

/**
 * Consequence panel (mockup `.risk`): tinted card listing what an action will
 * do — shown inside [ConfirmationSheet] before any mutating operation runs.
 */
@Composable
fun ConsequencePanel(
    bullets: List<String>,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
    title: String = "What this does",
) {
    val c = DroidKitTheme.colors
    val accent = if (danger) c.statusError else c.statusWarning
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = 0.07f), RoundedCornerShape(10.dp))
            .border(
                BorderStroke(1.dp, accent.copy(alpha = 0.30f)),
                RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            title.uppercase(),
            style = DroidKitTheme.extraTypography.microLabel,
            color = accent,
        )
        bullets.forEach { b ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("•", color = accent, style = MaterialTheme.typography.bodySmall)
                Text(
                    b,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 17.5.sp,
                    ),
                    color = c.textPrimary,
                )
            }
        }
    }
}

/**
 * Shared confirmation sheet for mutating operations. Renders the consequence
 * copy (plus optional bullet panel and [risk] badge) and an action-named
 * confirm button — never a vague "Confirm"/"OK". Destructive styling is
 * derived from [risk] (≥ High) or overridden by [danger].
 */
@Composable
fun ConfirmationSheet(
    title: String,
    consequence: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    risk: RiskLevel = RiskLevel.Minor,
    bullets: List<String> = emptyList(),
    danger: Boolean = risk.destructive,
    confirmEnabled: Boolean = true,
    dismissLabel: String = "Cancel",
) {
    val c = DroidKitTheme.colors
    DroidKitAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title)
                RiskBadge(risk)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(consequence)
                if (bullets.isNotEmpty()) {
                    ConsequencePanel(bullets, danger = danger)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = confirmEnabled) {
                Text(
                    confirmLabel,
                    color = if (danger) c.statusError else Color.Unspecified,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissLabel) }
        },
        danger = danger,
    )
}

/**
 * AI insight card (V5 Task 5) — `AiSurfaceCard` with the "AI insight" header,
 * so AI-generated content is always labelled and visually distinct from device
 * truth. Prefer [AiSurfaceCard] directly when a custom header is needed.
 */
@Composable
fun AIInsightCard(
    modifier: Modifier = Modifier,
    title: String = "AI insight",
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = DroidKitTheme.colors
    AiSurfaceCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.SmartToy,
                contentDescription = null,
                tint = c.brandSecondary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = c.brandSecondary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Column(modifier = Modifier.padding(top = 8.dp)) { content() }
    }
}

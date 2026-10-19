package io.github.droidkit.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.RecoveryAction
import io.github.droidkit.core.common.UiProblem

/** Human label for a recovery action — rendered on the button. */
fun RecoveryAction.label(): String = when (this) {
    RecoveryAction.Retry -> "Retry"
    RecoveryAction.Dismiss -> "Dismiss"
    RecoveryAction.OpenAppSettings -> "Open app settings"
    RecoveryAction.RequestElevatedAccess -> "Set up access"
    is RecoveryAction.RequestPermission -> "Grant permission"
    is RecoveryAction.Navigate -> "Open"
}

/**
 * Uniform problem card (DK-020): friendly title + message, severity accent,
 * technical details behind an expandable, recovery buttons. The screen owns
 * what each [RecoveryAction] does via [onRecovery].
 */
@Composable
fun UiProblemCard(
    problem: UiProblem,
    onRecovery: (RecoveryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = when (problem.severity) {
        UiProblem.Severity.INFO -> MaterialTheme.colorScheme.primary
        UiProblem.Severity.WARNING -> MaterialTheme.colorScheme.tertiary
        UiProblem.Severity.ERROR -> MaterialTheme.colorScheme.error
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(problem.title, style = MaterialTheme.typography.titleSmall, color = accent)
            Text(problem.message, style = MaterialTheme.typography.bodyMedium)
            problem.technicalDetails?.let { details ->
                var expanded by remember { mutableStateOf(false) }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide technical details" else "Technical details")
                }
                if (expanded) {
                    Text(
                        details,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (problem.recoveries.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    problem.recoveries.forEach { action ->
                        TextButton(onClick = { onRecovery(action) }) {
                            Text(action.label())
                        }
                    }
                }
            }
        }
    }
}

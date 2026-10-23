package io.github.droidkit.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.RecoveryAction
import io.github.droidkit.core.common.UiProblem
import io.github.droidkit.core.ui.R
import io.github.droidkit.core.ui.theme.DroidKitTheme

/** Human label for a recovery action — rendered on the button. */
@Composable
fun RecoveryAction.label(): String = when (this) {
    RecoveryAction.Retry -> stringResource(R.string.dk_recovery_retry)
    RecoveryAction.Dismiss -> stringResource(R.string.dk_recovery_dismiss)
    RecoveryAction.OpenAppSettings -> stringResource(R.string.dk_recovery_open_app_settings)
    RecoveryAction.RequestElevatedAccess -> stringResource(R.string.dk_recovery_set_up_access)
    is RecoveryAction.RequestPermission -> stringResource(R.string.dk_recovery_grant_permission)
    is RecoveryAction.Navigate -> stringResource(R.string.dk_recovery_open)
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
    val c = DroidKitTheme.colors
    val accent = when (problem.severity) {
        UiProblem.Severity.INFO -> c.statusInfo
        UiProblem.Severity.WARNING -> c.statusWarning
        UiProblem.Severity.ERROR -> c.statusError
    }
    DroidKitCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.06f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Catalog strings (strings_errors.xml) win over baked copy.
            val friendly = problem.friendlyCatalog()
            Text(
                friendly?.first ?: problem.title,
                style = MaterialTheme.typography.titleSmall,
                color = accent,
            )
            Text(
                friendly?.second ?: problem.message,
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
            )
            problem.technicalDetails?.let { details ->
                var expanded by remember { mutableStateOf(false) }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        stringResource(
                            if (expanded) R.string.dk_problem_hide_details
                            else R.string.dk_problem_show_details,
                        ),
                    )
                }
                if (expanded) {
                    Text(
                        details,
                        style = DroidKitTheme.extraTypography.technicalValue,
                        color = c.textSecondary,
                    )
                }
            }
            if (problem.recoveries.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    problem.recoveries.forEach { action ->
                        DroidKitOutlinedButton(onClick = { onRecovery(action) }) {
                            Text(action.label())
                        }
                    }
                }
            }
        }
    }
}

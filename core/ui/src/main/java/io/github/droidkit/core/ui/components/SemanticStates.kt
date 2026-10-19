package io.github.droidkit.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.droidkit.core.common.RecoveryAction
import io.github.droidkit.core.common.UiProblem
import io.github.droidkit.core.ui.theme.DroidKitSpacing
import io.github.droidkit.core.ui.theme.DroidKitStatusColors

/**
 * V5 semantic state components (docs/03_design_system/uiux_v5/semantics/03_SEMANTIC_STATE_MODEL.md).
 * Each state explains what happened and offers the next action where one exists.
 * Shared UI never calls repositories or system APIs — screens own the actions.
 */

/** Indeterminate loading with an optional friendly label. */
@Composable
fun LoadingState(modifier: Modifier = Modifier, label: String? = null) {
    Column(
        modifier = modifier.fillMaxWidth().padding(DroidKitSpacing.Xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DroidKitSpacing.Md),
    ) {
        CircularProgressIndicator()
        label?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

/** Connectivity-dependent content that cannot load while offline. */
@Composable
fun OfflineState(modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    SemanticPanel(
        title = "You're offline",
        message = "This needs a network connection. Reconnect and try again.",
        accent = DroidKitStatusColors.Info,
        actionLabel = if (onRetry != null) "Retry" else null,
        onAction = onRetry,
        modifier = modifier,
    )
}

/** The device lacks the hardware/API the feature needs — never shown as a failure. */
@Composable
fun UnsupportedState(title: String, modifier: Modifier = Modifier, detail: String? = null) {
    SemanticPanel(
        title = title,
        message = detail ?: "This device doesn't support it.",
        accent = DroidKitStatusColors.Unsupported,
        modifier = modifier,
    )
}

/** A provider or account must be set up before the feature can run. */
@Composable
fun SetupRequiredState(
    title: String,
    modifier: Modifier = Modifier,
    instructions: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    SemanticPanel(
        title = title,
        message = instructions ?: "Setup is required before this can run.",
        accent = DroidKitStatusColors.SetupRequired,
        actionLabel = actionLabel,
        onAction = onAction,
        modifier = modifier,
    )
}

/** Friendly, actionable error — delegates to the shared UiProblemCard. */
@Composable
fun FriendlyErrorState(
    problem: UiProblem,
    onRecovery: (RecoveryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    UiProblemCard(problem = problem, onRecovery = onRecovery, modifier = modifier)
}

/** Session progress; [progress] null renders indeterminate. */
@Composable
fun DiagnosticProgress(progress: Float?, modifier: Modifier = Modifier, label: String? = null) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DroidKitSpacing.Xs),
    ) {
        label?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
        if (progress == null) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SemanticPanel(
    title: String,
    message: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(DroidKitSpacing.Xxl),
        verticalArrangement = Arrangement.spacedBy(DroidKitSpacing.Sm),
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = accent)
        Text(message, style = MaterialTheme.typography.bodyMedium)
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

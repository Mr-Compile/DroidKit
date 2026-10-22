package io.github.droidkit.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import io.github.droidkit.core.common.RecoveryAction
import io.github.droidkit.core.common.UiProblem
import io.github.droidkit.core.ui.R
import io.github.droidkit.core.ui.theme.DroidKitSpacing
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * V5 semantic state components (docs/03_design_system/uiux_v5/semantics/03_SEMANTIC_STATE_MODEL.md),
 * styled per `mockups/improved-index.html` (`.gatecard`, `.spin`, `.progress-line`).
 * Each state explains what happened and offers the next action where one exists.
 * Shared UI never calls repositories or system APIs — screens own the actions.
 */

/** Indeterminate loading (mockup `.spin`); optional friendly label. */
@Composable
fun LoadingState(modifier: Modifier = Modifier, label: String? = null) {
    Column(
        modifier = modifier.fillMaxWidth().padding(DroidKitSpacing.Xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DroidKitSpacing.Md),
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            color = DroidKitTheme.colors.brandPrimary,
        )
        label?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = DroidKitTheme.colors.textSecondary)
        }
    }
}

/** Connectivity-dependent content that cannot load while offline. */
@Composable
fun OfflineState(modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    SemanticPanel(
        title = stringResource(R.string.dk_state_offline_title),
        message = stringResource(R.string.dk_state_offline_message),
        accent = DroidKitTheme.colors.statusInfo,
        icon = Icons.Outlined.CloudOff,
        actionLabel = if (onRetry != null) stringResource(R.string.dk_state_offline_retry) else null,
        onAction = onRetry,
        modifier = modifier,
    )
}

/** The device lacks the hardware/API the feature needs — never shown as a failure. */
@Composable
fun UnsupportedState(title: String, modifier: Modifier = Modifier, detail: String? = null) {
    SemanticPanel(
        title = title,
        message = detail ?: stringResource(R.string.dk_state_unsupported_fallback),
        accent = DroidKitTheme.colors.statusUnsupported,
        icon = Icons.Outlined.SentimentDissatisfied,
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
        message = instructions ?: stringResource(R.string.dk_state_setup_fallback),
        accent = DroidKitTheme.colors.statusSetupRequired,
        icon = Icons.Outlined.Lock,
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

/** Session progress (mockup `.progress-line`); [progress] null renders indeterminate. */
@Composable
fun DiagnosticProgress(progress: Float?, modifier: Modifier = Modifier, label: String? = null) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DroidKitSpacing.Xs),
    ) {
        label?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = DroidKitTheme.colors.textSecondary,
            )
        }
        DroidKitProgressLine(progress)
    }
}

/** Shared restriction/gate surface — renders as the mockup `.gatecard`. */
@Composable
private fun SemanticPanel(
    title: String,
    message: String,
    accent: androidx.compose.ui.graphics.Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    GateCard(
        title = title,
        message = message,
        icon = icon,
        accent = accent,
        modifier = modifier,
    ) {
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

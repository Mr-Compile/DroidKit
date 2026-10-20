package io.github.droidkit.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.core.ui.theme.DroidKitSpacing
import io.github.droidkit.core.ui.theme.DroidKitStatusColors

/** V5 semantic status kinds — wording/icon always accompanies color. */
enum class StatusKind { Success, Warning, Error, Info, Unsupported, SetupRequired, Neutral }

@Composable
fun StatusKind.color(): Color = when (this) {
    StatusKind.Success -> DroidKitStatusColors.Success
    StatusKind.Warning -> DroidKitStatusColors.Warning
    StatusKind.Error -> MaterialTheme.colorScheme.error
    StatusKind.Info -> DroidKitStatusColors.Info
    StatusKind.Unsupported -> DroidKitStatusColors.Unsupported
    StatusKind.SetupRequired -> DroidKitStatusColors.SetupRequired
    StatusKind.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
}

/** Non-interactive status pill: tinted container, label carries the meaning. */
@Composable
fun StatusBadge(kind: StatusKind, label: String, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = kind.color().copy(alpha = 0.16f),
        modifier = modifier,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = kind.color(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                horizontal = DroidKitSpacing.Sm,
                vertical = DroidKitSpacing.Xs,
            ),
        )
    }
}

/**
 * Badge for a diagnostic [TestResultState]. Maps the domain result onto the
 * semantic model: NOT_SUPPORTED/PERMISSION_REQUIRED are never rendered as FAIL.
 */
@Composable
fun ResultBadge(state: TestResultState, modifier: Modifier = Modifier) {
    val (kind, label) = when (state) {
        TestResultState.PASS -> StatusKind.Success to "Pass"
        TestResultState.FAIL -> StatusKind.Error to "Fail"
        TestResultState.NOT_TESTED -> StatusKind.Neutral to "Not tested"
        TestResultState.NOT_SUPPORTED -> StatusKind.Unsupported to "Not supported"
        TestResultState.PERMISSION_REQUIRED -> StatusKind.SetupRequired to "Permission required"
        TestResultState.UNKNOWN -> StatusKind.Neutral to "Unknown"
    }
    StatusBadge(kind, label, modifier)
}

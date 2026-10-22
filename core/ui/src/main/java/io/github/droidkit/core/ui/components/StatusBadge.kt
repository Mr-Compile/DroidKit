package io.github.droidkit.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.core.ui.theme.DroidKitTheme

/** V5 semantic status kinds — wording/icon always accompanies color. */
enum class StatusKind { Success, Warning, Error, Info, Unsupported, SetupRequired, Neutral }

@Composable
fun StatusKind.color(): Color = with(DroidKitTheme.colors) {
    when (this@color) {
        StatusKind.Success -> statusSuccess
        StatusKind.Warning -> statusWarning
        StatusKind.Error -> statusError
        StatusKind.Info -> statusInfo
        StatusKind.Unsupported -> statusUnsupported
        StatusKind.SetupRequired -> statusSetupRequired
        StatusKind.Neutral -> textSecondary
    }
}

private data class BadgeStyle(val borderAlpha: Float, val backgroundAlpha: Float)

@Composable
private fun StatusKind.badgeStyle(): BadgeStyle = when (this) {
    // mockup .badge.ok/.ready — brand hairline + 6% fill
    StatusKind.Success -> BadgeStyle(0.16f, 0.06f)
    // mockup .badge.setup/.warn/.blocked — 35–40% border, 7–8% fill
    StatusKind.SetupRequired -> BadgeStyle(0.40f, 0.08f)
    StatusKind.Warning -> BadgeStyle(0.40f, 0.08f)
    StatusKind.Error -> BadgeStyle(0.35f, 0.07f)
    StatusKind.Info -> BadgeStyle(0.35f, 0.08f)
    // mockup .badge.dim — neutral, no tint
    StatusKind.Unsupported -> BadgeStyle(0f, 0f)
    StatusKind.Neutral -> BadgeStyle(0f, 0f)
}

/**
 * Availability badge (mockup `.badge`): hairline pill with a 6dp status dot
 * and wording — never color-only.
 */
@Composable
fun StatusBadge(kind: StatusKind, label: String, modifier: Modifier = Modifier) {
    val c = DroidKitTheme.colors
    val accent = kind.color()
    val style = kind.badgeStyle()
    Surface(
        shape = RoundedCornerShape(7.dp),
        color = if (style.backgroundAlpha > 0f) accent.copy(alpha = style.backgroundAlpha) else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (style.borderAlpha > 0f) accent.copy(alpha = style.borderAlpha) else c.lineNeutral,
        ),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
        ) {
            StatusDot(accent, glow = false)
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Verdict pill for a diagnostic [TestResultState] (mockup `.res`): compact
 * letterspaced pill. NOT_SUPPORTED/PERMISSION_REQUIRED are never rendered as
 * FAIL.
 */
@Composable
fun ResultBadge(state: TestResultState, modifier: Modifier = Modifier) {
    val c = DroidKitTheme.colors
    val (accent, label, bordered) = when (state) {
        TestResultState.PASS -> Triple(c.statusSuccess, "PASS", true)
        TestResultState.FAIL -> Triple(c.statusError, "FAIL", true)
        TestResultState.NOT_TESTED -> Triple(c.statusUnsupported, "SKIPPED", false)
        TestResultState.NOT_SUPPORTED -> Triple(c.statusUnsupported, "N/A", false)
        TestResultState.PERMISSION_REQUIRED -> Triple(c.statusSetupRequired, "NEEDS PERMISSION", true)
        TestResultState.UNKNOWN -> Triple(c.statusUnsupported, "UNKNOWN", false)
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = accent.copy(alpha = if (bordered) 0.08f else 0.07f),
        border = BorderStroke(
            1.dp,
            if (bordered) accent.copy(alpha = 0.35f) else c.lineNeutral,
        ),
        modifier = modifier,
    ) {
        Text(
            label,
            style = DroidKitTheme.extraTypography.verdictPill,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

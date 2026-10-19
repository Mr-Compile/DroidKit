package io.github.droidkit.core.privileges

import io.github.droidkit.core.common.RecoveryAction
import io.github.droidkit.core.common.UiProblem

/** Maps a blocked tool resolution to a friendly problem with recovery actions. */
fun BlockedReason.toUiProblem(): UiProblem = when (this) {
    is BlockedReason.MissingPermission -> UiProblem(
        title = "Permission required",
        message = "This tool needs the $permission permission to work.",
        recoveries = listOf(
            RecoveryAction.RequestPermission(permission),
            RecoveryAction.Dismiss,
        ),
    )
    is BlockedReason.MissingHardware -> UiProblem(
        title = "Not supported on this device",
        message = "The required hardware ($feature) is not present on this device.",
        severity = UiProblem.Severity.INFO,
    )
    is BlockedReason.RequiresApi -> UiProblem(
        title = "Android version too old",
        message = "This tool needs Android API $minSdk or newer.",
        severity = UiProblem.Severity.INFO,
    )
    is BlockedReason.NeedsProvider -> UiProblem(
        title = "Elevated access needed",
        message = when (val s = status) {
            ProviderStatus.Ready -> "Provider ${kind.name} is ready."
            is ProviderStatus.NeedsSetup -> "${kind.name}: ${s.instructions}"
            is ProviderStatus.Unavailable -> "${kind.name}: ${s.reason}"
        },
        recoveries = listOf(
            RecoveryAction.RequestElevatedAccess,
            RecoveryAction.Dismiss,
        ),
    )
}

/** Fold a multi-reason block into the most actionable problem (provider > permission > hardware/api). */
fun ToolResolution.Blocked.toUiProblem(): UiProblem =
    reasons.maxByOrNull {
        when (it) {
            is BlockedReason.NeedsProvider -> 3
            is BlockedReason.MissingPermission -> 2
            else -> 1
        }
    }?.toUiProblem()
        ?: UiProblem(title = "Unavailable", message = "This tool is currently unavailable.")

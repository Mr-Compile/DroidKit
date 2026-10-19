package io.github.droidkit.core.common

/**
 * Unified user-facing problem/recovery model (DK-020). Subsystem-specific
 * typed errors (ToolResolution, AiError, ElevatedResult) map INTO this at the
 * UI boundary — they are never erased, just translated for display.
 */
data class UiProblem(
    val title: String,
    val message: String,
    val severity: Severity = Severity.ERROR,
    /** Raw/technical detail (stderr, exception type) shown behind an expand toggle. */
    val technicalDetails: String? = null,
    val recoveries: List<RecoveryAction> = listOf(RecoveryAction.Dismiss),
) {
    enum class Severity { INFO, WARNING, ERROR }

    companion object {
        /** Fallback for unexpected throwables — details go to the technical slot, not the user message. */
        fun unexpected(t: Throwable): UiProblem = UiProblem(
            title = "Operation failed",
            message = "The action did not complete. Expand details for the technical cause.",
            severity = Severity.ERROR,
            technicalDetails = "${t::class.qualifiedName}: ${t.message}",
            recoveries = listOf(RecoveryAction.Retry, RecoveryAction.Dismiss),
        )
    }
}

/** What the user can do about a [UiProblem]. Screens render buttons for these. */
sealed interface RecoveryAction {
    data object Retry : RecoveryAction
    data object Dismiss : RecoveryAction

    /** Runtime permission must be (re-)requested in context. */
    data class RequestPermission(val permission: String) : RecoveryAction

    /** Permission was permanently denied — only system Settings can fix it. */
    data object OpenAppSettings : RecoveryAction

    /** An elevated provider needs setup/grant (Shizuku, ADB, root). */
    data object RequestElevatedAccess : RecoveryAction

    /** Recovery lives on another in-app screen (e.g. Settings). Route is opaque to the model. */
    data class Navigate(val route: String) : RecoveryAction
}

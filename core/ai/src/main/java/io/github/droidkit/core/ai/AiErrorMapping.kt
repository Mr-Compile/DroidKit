package io.github.droidkit.core.ai

import io.github.droidkit.core.common.RecoveryAction
import io.github.droidkit.core.common.UiProblem

/** Single canonical user-facing text for every [AiError] — replaces per-screen when() maps. */
fun AiError.describe(): String = when (this) {
    AiError.DISABLED -> "AI is disabled — enable it in Settings."
    AiError.NO_KEY -> "No API key stored — add one in Settings."
    AiError.OFFLINE -> "Device is offline."
    AiError.AUTH_ERROR -> "Authentication failed — check the key."
    AiError.RATE_LIMITED -> "Rate limited — try again later."
    AiError.MALFORMED_RESPONSE -> "Unexpected response shape."
    AiError.PROVIDER_ERROR -> "Provider error."
}

fun AiError.toUiProblem(technicalDetails: String? = null): UiProblem = UiProblem(
    title = when (this) {
        AiError.DISABLED, AiError.NO_KEY -> "DroidKit Intelligence not set up"
        else -> "AI request failed"
    },
    message = describe(),
    severity = when (this) {
        AiError.DISABLED, AiError.NO_KEY -> UiProblem.Severity.INFO
        else -> UiProblem.Severity.ERROR
    },
    technicalDetails = technicalDetails,
    recoveries = when (this) {
        AiError.DISABLED, AiError.NO_KEY ->
            listOf(RecoveryAction.Navigate("settings"), RecoveryAction.Dismiss)
        AiError.OFFLINE, AiError.RATE_LIMITED ->
            listOf(RecoveryAction.Retry, RecoveryAction.Dismiss)
        else -> listOf(RecoveryAction.Dismiss)
    },
)

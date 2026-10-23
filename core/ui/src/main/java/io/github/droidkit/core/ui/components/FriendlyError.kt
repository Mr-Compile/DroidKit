package io.github.droidkit.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.droidkit.core.common.UiProblem
import io.github.droidkit.core.ui.R

/**
 * Friendly-error resolver (V5 Task 6). Maps a [UiProblem.catalogKey] to the
 * title/message pair in `strings_errors.xml`. Returns null for unknown keys so
 * callers fall back to the problem's baked strings — never invent copy.
 */
@Composable
internal fun UiProblem.friendlyCatalog(): Pair<String, String>? {
    val arg0 = catalogArgs.getOrElse(0) { "" }
    val arg1 = catalogArgs.getOrElse(1) { "" }
    return when (catalogKey) {
        "permission" ->
            stringResource(R.string.err_permission_title) to
                stringResource(R.string.err_permission_message, arg0)
        "hardware" ->
            stringResource(R.string.err_hardware_title) to
                stringResource(R.string.err_hardware_message, arg0)
        "api" ->
            stringResource(R.string.err_api_title) to
                stringResource(R.string.err_api_message, arg0)
        "provider_ready" ->
            stringResource(R.string.err_provider_title) to
                stringResource(R.string.err_provider_ready_message, arg0)
        "provider_setup" ->
            stringResource(R.string.err_provider_title) to
                stringResource(R.string.err_provider_setup_message, arg0, arg1)
        "provider_unavailable" ->
            stringResource(R.string.err_provider_title) to
                stringResource(R.string.err_provider_unavailable_message, arg0, arg1)
        "unavailable" ->
            stringResource(R.string.err_unavailable_title) to
                stringResource(R.string.err_unavailable_message)
        "unexpected" ->
            stringResource(R.string.err_unexpected_title) to
                stringResource(R.string.err_unexpected_message)
        "ai_disabled" ->
            stringResource(R.string.err_ai_setup_title) to
                stringResource(R.string.err_ai_disabled_message)
        "ai_no_key" ->
            stringResource(R.string.err_ai_setup_title) to
                stringResource(R.string.err_ai_no_key_message)
        "ai_offline" ->
            stringResource(R.string.err_ai_request_title) to
                stringResource(R.string.err_ai_offline_message)
        "ai_auth_error" ->
            stringResource(R.string.err_ai_request_title) to
                stringResource(R.string.err_ai_auth_error_message)
        "ai_rate_limited" ->
            stringResource(R.string.err_ai_request_title) to
                stringResource(R.string.err_ai_rate_limited_message)
        "ai_provider_error" ->
            stringResource(R.string.err_ai_request_title) to
                stringResource(R.string.err_ai_provider_error_message)
        "ai_malformed_response" ->
            stringResource(R.string.err_ai_request_title) to
                stringResource(R.string.err_ai_malformed_response_message)
        else -> null
    }
}

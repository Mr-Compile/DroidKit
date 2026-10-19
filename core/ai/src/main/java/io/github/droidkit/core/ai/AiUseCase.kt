package io.github.droidkit.core.ai

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/** Result of an AI operation — [text] on success, [error] on typed failure. */
data class AiResult(
    val text: String? = null,
    val error: AiError? = null,
)

/**
 * Feature-facing AI entry point. Enforces the gates in order:
 * DISABLED → NO_KEY → provider errors. AI can explain/summarize/recommend —
 * it never executes actions.
 */
@Singleton
class AiUseCase @Inject constructor(
    private val store: AiSettingsStore,
    private val provider: AiProvider,
    private val contextBuilder: AiContextBuilder,
) {
    suspend fun isReady(): AiError? {
        if (!store.enabled.first()) return AiError.DISABLED
        if (!store.hasKey.first()) return AiError.NO_KEY
        return null
    }

    /** Explain a structured tool/diagnostic result in technician terms. */
    suspend fun explain(toolTitle: String, fields: Map<String, String?>): AiResult =
        run(
            system = "You are DroidKit, an Android technician assistant. " +
                "Explain the diagnostic data briefly and accurately. " +
                "Do not invent values. Recommend at most one next step.",
            context = contextBuilder.build(
                mapOf("Tool" to toolTitle) + fields,
            ),
        )

    /** Summarize a diagnostic report — optional, user-invoked only. */
    suspend fun summarizeReport(reportText: String): AiResult =
        run(
            system = "Summarize this device diagnostic report in 4-6 bullets. " +
                "Lead with failures, then notable observations.",
            context = contextBuilder.minimize(reportText, 6_000),
        )

    /** Round-trip reachability check for the settings screen. */
    suspend fun testConnection(): AiError? {
        if (!store.enabled.first()) return AiError.DISABLED
        return provider.testConnection()
    }

    private suspend fun run(system: String, context: String): AiResult {
        isReady()?.let { return AiResult(error = it) }
        return provider.complete(AiRequest(system, context))
            .fold(
                onSuccess = { AiResult(text = it.text) },
                onFailure = {
                    AiResult(error = (it as? AiException)?.error ?: AiError.PROVIDER_ERROR)
                },
            )
    }
}

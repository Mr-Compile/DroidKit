package io.github.droidkit.core.ai

import io.github.droidkit.core.common.ToolRegistry
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
    private val toolRegistry: ToolRegistry,
) {
    suspend fun isReady(): AiError? {
        if (!store.enabled.first()) return AiError.DISABLED
        if (!store.hasKey.first()) return AiError.NO_KEY
        return null
    }

    /** Explain a structured tool/diagnostic result in technician terms. */
    suspend fun explain(toolTitle: String, fields: Map<String, String?>): AiResult =
        run(
            system = PromptRegistry.EXPLAIN,
            context = contextBuilder.build(
                mapOf("Tool" to toolTitle) + fields,
            ),
        )

    /** Run one of the registered assistants over structured fields. */
    suspend fun assist(assistant: AiAssistant, fields: Map<String, String?>): AiResult =
        run(
            system = PromptRegistry.forAssistant(assistant),
            context = contextBuilder.build(fields),
        )

    /**
     * Multi-turn chat. [history] pairs role→content; the tool catalog is
     * appended as context so the model may propose `open_tool` actions.
     */
    suspend fun chat(history: List<AiMessage>): AiResult {
        isReady()?.let { return AiResult(error = it) }
        val catalog = contextBuilder.build(
            toolRegistry.tools.associate { it.id to it.title },
            maxChars = 3_000,
        )
        val messages = buildList {
            add(AiMessage("system", PromptRegistry.forAssistant(AiAssistant.CONTEXTUAL_CHAT)))
            add(AiMessage("system", "Available tool catalog (id: title):\n$catalog"))
            addAll(history)
        }
        return provider.complete(AiRequest(messages, maxTokens = 768))
            .fold(
                onSuccess = { AiResult(text = it.text) },
                onFailure = {
                    AiResult(error = (it as? AiException)?.error ?: AiError.PROVIDER_ERROR)
                },
            )
    }

    /**
     * Plain-language verdict over the values a screen currently displays.
     * User-invoked, or auto-invoked only when the user enabled auto-summarize.
     */
    suspend fun summarizeValues(
        screenTitle: String,
        fields: Map<String, String?>,
    ): AiResult =
        run(
            system = PromptRegistry.SUMMARIZE_VALUES,
            context = contextBuilder.build(
                mapOf("Screen" to screenTitle) + fields,
            ),
        )

    /** Summarize a diagnostic report — optional, user-invoked only. */
    suspend fun summarizeReport(reportText: String): AiResult =
        run(
            system = PromptRegistry.SUMMARIZE,
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

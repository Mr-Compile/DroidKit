package io.github.droidkit.core.ai

/** Typed AI failure surface — every path degrades to one of these. */
enum class AiError {
    DISABLED,
    NO_KEY,
    OFFLINE,
    AUTH_ERROR,
    RATE_LIMITED,
    PROVIDER_ERROR,
    MALFORMED_RESPONSE,
}

/** A single message in a chat-style request. */
data class AiMessage(val role: String, val content: String)

/** Provider-agnostic completion request. */
data class AiRequest(
    val messages: List<AiMessage>,
    val maxTokens: Int = 512,
    val temperature: Double = 0.3,
) {
    /** Single system+user convenience — covers every non-chat call site. */
    constructor(
        system: String,
        user: String,
        maxTokens: Int = 512,
        temperature: Double = 0.3,
    ) : this(
        messages = listOf(
            AiMessage("system", system),
            AiMessage("user", user),
        ),
        maxTokens = maxTokens,
        temperature = temperature,
    )
}

/** Provider-agnostic completion response. */
data class AiResponse(val text: String, val model: String)

/**
 * AI provider boundary. Features and use-cases depend only on this interface —
 * never on a concrete vendor client.
 */
interface AiProvider {
    /** Live key check + tiny round-trip; returns null error when reachable. */
    suspend fun testConnection(): AiError?

    /** Run a completion. Never throws — failures map to [AiError]. */
    suspend fun complete(request: AiRequest): Result<AiResponse>
}

/** Thrown by providers to signal a typed failure. */
class AiException(val error: AiError, message: String) : Exception(message)

package io.github.droidkit.core.ai

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds minimal, redacted context for an AI request. Only the fields a caller
 * explicitly supplies are included — callers should pass structured results,
 * never raw logs or whole objects.
 */
@Singleton
class AiContextBuilder @Inject constructor(
    private val privacyFilter: PrivacyFilter,
) {
    /**
     * Renders a labeled context block. Every value passes through the privacy
     * filter; empty/null entries are dropped.
     */
    fun build(entries: Map<String, String?>, maxChars: Int = 4_000): String {
        val body = entries.entries
            .filter { !it.value.isNullOrBlank() }
            .joinToString("\n") { (k, v) -> "$k: ${privacyFilter.redact(v!!)}" }
        return body.take(maxChars)
    }

    /** Redact + truncate a single text block (e.g. a report body). */
    fun minimize(text: String, maxChars: Int = 4_000): String =
        privacyFilter.minimize(text, maxChars)
}

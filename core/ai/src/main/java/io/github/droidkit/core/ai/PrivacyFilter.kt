package io.github.droidkit.core.ai

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Redacts secrets and high-entropy tokens before any context leaves the device.
 * Conservative: anything that *looks* like a credential is stripped, even at
 * the cost of removing legitimate identifiers.
 */
@Singleton
class PrivacyFilter @Inject constructor() {

    private val patterns = listOf(
        // PEM blocks (private keys — never send)
        Regex("-----BEGIN [A-Z ]*PRIVATE KEY-----.*?-----END [A-Z ]*PRIVATE KEY-----", RegexOption.DOT_MATCHES_ALL) to "[REDACTED_PRIVATE_KEY]",
        // Bearer/authorization headers and key=value credentials
        Regex("(?i)(bearer\\s+|(authorization|api[-_ ]?key|token|secret|password|passwd)\\s*[:=]\\s*(bearer\\s+)?)[\"']?\\S{6,}") to "[REDACTED_CREDENTIAL]",
        // Groq/OpenAI-style keys
        Regex("gsk_[A-Za-z0-9]{20,}") to "[REDACTED_API_KEY]",
        Regex("sk-[A-Za-z0-9]{20,}") to "[REDACTED_API_KEY]",
        // Generic long base64/hex blobs (40+ chars — likely a token or key)
        Regex("\\b[A-Za-z0-9+/=_-]{40,}\\b") to "[REDACTED_TOKEN]",
        // MAC addresses and serial-like values
        Regex("\\b([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}\\b") to "[REDACTED_MAC]",
        Regex("(?i)serial[ number]*\\s*[:=]\\s*\\S+") to "[REDACTED_SERIAL]",
    )

    /** Returns [input] with every secret-shaped value replaced by a marker. */
    fun redact(input: String): String =
        patterns.fold(input) { acc, (re, marker) -> re.replace(acc, marker) }

    /** Truncates to [maxChars] after redaction — context minimization. */
    fun minimize(input: String, maxChars: Int = 4_000): String =
        redact(input).take(maxChars)
}

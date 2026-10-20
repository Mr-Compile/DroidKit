package io.github.droidkit.core.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the summarizeValues path: screen fields pass through the context
 * builder (blank dropped, secrets redacted) and the prompt contract requires
 * a plain-language per-value verdict.
 */
class AiSummarizeValuesTest {

    private val builder = AiContextBuilder(PrivacyFilter())

    @Test
    fun `summary context drops null and blank screen fields`() {
        val ctx = builder.build(
            mapOf("Screen" to "Battery lab") + mapOf(
                "Level" to "72%",
                "Voltage" to null,
                "Source" to " ",
            ),
        )
        assertTrue(ctx.contains("Screen: Battery lab"))
        assertTrue(ctx.contains("Level: 72%"))
        assertFalse(ctx.contains("Voltage"))
        assertFalse(ctx.contains("Source"))
    }

    @Test
    fun `summary context redacts key-shaped values`() {
        val ctx = builder.build(
            mapOf("Field" to "note gsk_0123456789abcdefghij0123456789 end"),
        )
        assertFalse(ctx.contains("gsk_"))
        assertTrue(ctx.contains("[REDACTED_API_KEY]"))
    }

    @Test
    fun `summary prompt requires verdict labels and honest fallback`() {
        val p = PromptRegistry.SUMMARIZE_VALUES
        assertTrue(p.contains("non-technical"))
        assertTrue(p.contains("normal, unusual, or failing"))
        assertTrue(p.contains("instead of guessing"))
        assertTrue(p.contains("ALL GOOD"))
        assertTrue(p.contains("WORTH CHECKING"))
        assertTrue(p.contains("NEEDS ATTENTION"))
    }
}

package io.github.droidkit.core.ai

import org.junit.Assert.assertEquals
import org.junit.Test

/** Every [AiError] maps to a `strings_errors.xml` catalog key. */
class AiErrorCatalogTest {

    @Test
    fun `every AiError sets its catalog key`() {
        AiError.entries.forEach { error ->
            assertEquals(
                "ai_${error.name.lowercase()}",
                error.toUiProblem().catalogKey,
            )
        }
    }
}

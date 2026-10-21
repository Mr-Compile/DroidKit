package io.github.droidkit.feature.packages

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PackagesInfoTest {

    @Test
    fun batchSummaryAllSucceeded() {
        assertEquals(
            "Disable succeeded for 3 packages.",
            PackagesInfo.batchSummary(
                "Disable",
                listOf("com.a" to true, "com.b" to true, "com.c" to true),
            ),
        )
    }

    @Test
    fun batchSummarySingular() {
        assertEquals(
            "Suspend succeeded for 1 package.",
            PackagesInfo.batchSummary("Suspend", listOf("com.a" to true)),
        )
    }

    @Test
    fun batchSummaryPartialNamesFailures() {
        assertEquals(
            "Disable: 1 succeeded, 2 failed (com.b, com.c).",
            PackagesInfo.batchSummary(
                "Disable",
                listOf("com.a" to true, "com.b" to false, "com.c" to false),
            ),
        )
    }

    @Test
    fun batchSummaryAllFailed() {
        assertEquals(
            "Enable failed for com.a.",
            PackagesInfo.batchSummary("Enable", listOf("com.a" to false)),
        )
    }

    @Test
    fun batchSummaryEmpty() {
        assertEquals("Nothing to do.", PackagesInfo.batchSummary("Disable", emptyList()))
    }

    @Test
    fun batchConfirmCopy() {
        val (title, body) = PackagesInfo.batchConfirm("Suspend", 2)
        assertEquals("Suspend 2 packages?", title)
        assertTrue(body.contains("each selected package"))
    }
}

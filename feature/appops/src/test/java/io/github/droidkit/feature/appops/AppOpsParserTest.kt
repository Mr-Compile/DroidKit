package io.github.droidkit.feature.appops

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppOpsParserTest {

    @Test
    fun `parses op mode rows from appops get output`() {
        val out = """
            Uid 1000: com.example.app
            RUN_IN_BACKGROUND: ignore
            WAKE_LOCK: allow; time=+5s
            OP_READ_SMS: deny
        """.trimIndent()
        val ops = AppOpsParser.parse(out)
        assertEquals(3, ops.size)
        assertEquals(AppOpEntry("RUN_IN_BACKGROUND", "ignore"), ops[0])
        assertEquals(AppOpEntry("WAKE_LOCK", "allow"), ops[1])
        assertEquals(AppOpEntry("OP_READ_SMS", "deny"), ops[2])
    }

    @Test
    fun `ignores headers and malformed lines`() {
        val out = """
            Uid 1000: com.example.app
            PACKAGE_NAME com.example.app
            garbage line without colon
            : empty-op
            VIBRATE: default
        """.trimIndent()
        val ops = AppOpsParser.parse(out)
        assertEquals(1, ops.size)
        assertEquals(AppOpEntry("VIBRATE", "default"), ops.single())
    }

    @Test
    fun `empty output gives empty list`() {
        assertTrue(AppOpsParser.parse("").isEmpty())
    }

    @Test
    fun `package list parser extracts sorted names`() {
        val out = """
            package:com.zeta
            package:com.alpha
            not-a-package-line
            package:
            package:com.mid
        """.trimIndent()
        assertEquals(
            listOf("com.alpha", "com.mid", "com.zeta"),
            PackageListParser.parse(out),
        )
    }
}

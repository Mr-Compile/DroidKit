package io.github.droidkit.feature.packages

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimePermissionParserTest {

    @Test
    fun `parses runtime permissions block`() {
        val dumpsys = """
            Packages:
              Package [com.example] (abc):
            requested permissions:
              android.permission.CAMERA
            install permissions:
              android.permission.INTERNET: granted=true
            runtime permissions:
              android.permission.CAMERA: granted=false, flags=[USER_SENSITIVE_WHEN_DENIED]
              android.permission.RECORD_AUDIO: granted=true
            gids=[3003]
        """.trimIndent()
        val out = RuntimePermissionParser.parse(dumpsys)
        assertEquals(
            listOf(
                "android.permission.CAMERA" to false,
                "android.permission.RECORD_AUDIO" to true,
            ),
            out,
        )
    }

    @Test
    fun `stops at block boundary`() {
        val dumpsys = """
            runtime permissions:
              android.permission.X: granted=true
            Packages:
              android.permission.Y: granted=true
        """.trimIndent()
        assertEquals(1, RuntimePermissionParser.parse(dumpsys).size)
    }

    @Test
    fun `no block gives empty`() {
        assertTrue(RuntimePermissionParser.parse("nothing here").isEmpty())
    }
}

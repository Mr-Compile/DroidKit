package io.github.droidkit.feature.apps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppInfoTest {

    @Test
    fun sha256ColonHex() {
        // sha256 of empty input, colon-separated uppercase hex
        val digest = AppInfo.sha256Hex(ByteArray(0))
        assertEquals(95, digest.length) // 32 bytes * 2 + 31 colons
        assertTrue(digest.startsWith("E3:B0:C4:42"))
    }

    @Test
    fun formatBytes() {
        assertEquals("Not exposed", AppInfo.formatBytes(null))
        assertEquals("512 B", AppInfo.formatBytes(512))
        assertEquals("1.5 KB", AppInfo.formatBytes(1536))
        assertEquals("24.0 MB", AppInfo.formatBytes(24L * 1024 * 1024))
    }

    @Test
    fun installerLabels() {
        assertEquals("Google Play Store", AppInfo.installerLabel("com.android.vending"))
        assertEquals("Unknown / sideloaded", AppInfo.installerLabel(null))
        assertEquals("com.custom.store", AppInfo.installerLabel("com.custom.store"))
    }

    @Test
    fun libAbiAndSummary() {
        assertEquals("arm64-v8a", AppInfo.libAbi("lib/arm64-v8a/libx.so"))
        assertNull(AppInfo.libAbi("res/drawable/x.png"))
        assertEquals("None bundled", AppInfo.summarizeLibs(emptyList()))
        assertEquals(
            "arm64-v8a: 2 · armeabi-v7a: 1",
            AppInfo.summarizeLibs(
                listOf("lib/arm64-v8a/a.so", "lib/arm64-v8a/b.so", "lib/armeabi-v7a/a.so"),
            ),
        )
    }
}

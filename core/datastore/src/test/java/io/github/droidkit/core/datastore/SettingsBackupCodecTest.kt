package io.github.droidkit.core.datastore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsBackupCodecTest {

    private val snapshot = SettingsSnapshot(
        themeMode = "DARK",
        dynamicColor = true,
        technicianName = "A. Technician",
        favoriteTools = setOf("usb.devices", "nfc.scan"),
        recentTools = listOf("nfc.scan", "usb.devices"),
        toolUseCounts = mapOf("nfc.scan" to 3, "usb.devices" to 7),
        wirelessAdbAutoConnect = false,
    )

    @Test
    fun `round trip preserves every field`() {
        val json = SettingsBackupCodec.encode(snapshot, 1_700_000_000_000)
        val decoded = SettingsBackupCodec.decode(json)
        assertTrue(decoded.isSuccess)
        assertEquals(snapshot, decoded.getOrThrow())
    }

    @Test
    fun `unknown keys are ignored`() {
        val json = """{"v":1,"exportedAt":0,"futureField":{"x":1},
            |"settings":{"themeMode":"LIGHT","brandNewPref":true}}""".trimMargin()
        val decoded = SettingsBackupCodec.decode(json)
        assertTrue(decoded.isSuccess)
        assertEquals("LIGHT", decoded.getOrThrow().themeMode)
        assertEquals(SettingsSnapshot().copy(themeMode = "LIGHT"), decoded.getOrThrow())
    }

    @Test
    fun `missing fields fall back to defaults`() {
        val decoded = SettingsBackupCodec.decode("""{"v":1,"settings":{}}""")
        assertTrue(decoded.isSuccess)
        assertEquals(SettingsSnapshot(), decoded.getOrThrow())
    }

    @Test
    fun `bad json fails`() {
        assertTrue(SettingsBackupCodec.decode("not json").isFailure)
        assertTrue(SettingsBackupCodec.decode("""{"v":2,"settings":{}}""").isFailure)
        assertTrue(SettingsBackupCodec.decode("""{"settings":{}}""").isFailure)
        assertTrue(
            SettingsBackupCodec.decode(
                """{"v":1,"settings":{"dynamicColor":"yes"}}""",
            ).isFailure,
        )
    }

    @Test
    fun `protected keys are not exported by construction`() {
        val json = SettingsBackupCodec.encode(snapshot, 0)
        assertFalse(json.contains("wirelessAdbPaired"))
        assertFalse(json.contains("onboarding"))
        assertFalse(json.lowercase().contains("apikey"))
    }
}

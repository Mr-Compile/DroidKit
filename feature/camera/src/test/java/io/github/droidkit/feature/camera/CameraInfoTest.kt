package io.github.droidkit.feature.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CameraInfoTest {

    @Test
    fun `facingName covers all lens facings`() {
        assertEquals("Front", CameraInfo.facingName(0))
        assertEquals("Back", CameraInfo.facingName(1))
        assertEquals("External", CameraInfo.facingName(2))
        assertEquals("Unknown", CameraInfo.facingName(99))
        assertEquals("Unknown", CameraInfo.facingName(null))
    }

    @Test
    fun `hardwareLevelName covers levels`() {
        assertEquals("Legacy", CameraInfo.hardwareLevelName(2))
        assertEquals("Limited", CameraInfo.hardwareLevelName(0))
        assertEquals("Full", CameraInfo.hardwareLevelName(1))
        assertEquals("Level 3", CameraInfo.hardwareLevelName(3))
        assertEquals("External", CameraInfo.hardwareLevelName(4))
        assertEquals("Unknown", CameraInfo.hardwareLevelName(null))
    }

    @Test
    fun `imageFormatName names common formats and hexes unknown`() {
        assertEquals("JPEG", CameraInfo.imageFormatName(0x100))
        assertEquals("YUV_420_888", CameraInfo.imageFormatName(35))
        assertEquals("PRIVATE (Surface)", CameraInfo.imageFormatName(0x22))
        assertEquals("RAW_SENSOR", CameraInfo.imageFormatName(0x20))
        assertEquals("HEIC", CameraInfo.imageFormatName(0x48454946))
        assertEquals("0x7F", CameraInfo.imageFormatName(0x7F))
    }

    @Test
    fun `capabilityName maps camera2 capability ints`() {
        assertEquals("RAW", CameraInfo.capabilityName(3))
        assertEquals("Backward compatible", CameraInfo.capabilityName(0))
        assertEquals("Manual sensor", CameraInfo.capabilityName(1))
        assertEquals("Depth", CameraInfo.capabilityName(8))
        assertEquals("High-speed video", CameraInfo.capabilityName(9))
        assertEquals("Logical multi-camera", CameraInfo.capabilityName(11))
        assertNull(CameraInfo.capabilityName(255))
    }

    @Test
    fun `mode maps return null for unknown modes`() {
        assertEquals("Continuous video", CameraInfo.afModeName(3))
        assertEquals("EDoF", CameraInfo.afModeName(5))
        assertNull(CameraInfo.afModeName(42))

        assertEquals("Night", CameraInfo.sceneModeName(5))
        assertEquals("HDR", CameraInfo.sceneModeName(18))
        assertNull(CameraInfo.sceneModeName(0)) // DISABLED hides

        assertNull(CameraInfo.effectName(0)) // OFF hides
        assertEquals("Sepia", CameraInfo.effectName(4))

        assertEquals("On", CameraInfo.videoStabilizationName(1))
        assertEquals("Preview stabilization", CameraInfo.videoStabilizationName(2))
        assertNull(CameraInfo.videoStabilizationName(9))

        assertEquals("Auto", CameraInfo.awbModeName(1))
        assertEquals("Shade", CameraInfo.awbModeName(8))
        assertNull(CameraInfo.awbModeName(50))

        assertEquals("Auto flash redeye", CameraInfo.aeModeName(4))
        assertNull(CameraInfo.aeModeName(77))
    }

    @Test
    fun `timestampSourceName is honest about unknown`() {
        assertEquals("Realtime", CameraInfo.timestampSourceName(1))
        assertEquals("Unknown", CameraInfo.timestampSourceName(0))
        assertNull(CameraInfo.timestampSourceName(null))
    }

    @Test
    fun `maxFpsLabel converts ns frame duration`() {
        assertEquals("max 30 fps", CameraInfo.maxFpsLabel(33_366_667L))
        assertEquals("no frame-duration floor", CameraInfo.maxFpsLabel(0L))
    }

    @Test
    fun `fpsRangeLabel renders lower-upper`() {
        assertEquals("15–30 fps", CameraInfo.fpsRangeLabel(15, 30))
    }

    @Test
    fun `megapixels handles null`() {
        assertNull(CameraInfo.megapixels(null))
    }

    @Test
    fun `sizesSummary sorts biggest-first and caps`() {
        val sizes = listOf(
            640 to 480, 4000 to 3000, 1920 to 1080,
            1280 to 720, 3840 to 2160, 320 to 240,
            2560 to 1440, 800 to 600, 176 to 144,
        )
        val out = CameraInfo.sizesSummaryPairs(sizes, cap = 3)
        assertEquals("4000×3000, 3840×2160, 2560×1440  (+6 more)", out)
    }

    @Test
    fun `sizesSummary handles empty and exact-cap`() {
        assertNull(CameraInfo.sizesSummaryPairs(emptyList()))
        assertNull(CameraInfo.sizesSummaryPairs(null))
        assertEquals(
            "1920×1080, 1280×720",
            CameraInfo.sizesSummaryPairs(listOf(1280 to 720, 1920 to 1080), cap = 8),
        )
    }
}

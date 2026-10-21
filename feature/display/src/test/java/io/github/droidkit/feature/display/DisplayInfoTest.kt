package io.github.droidkit.feature.display

import android.view.Display
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DisplayInfoTest {

    @Test
    fun hdrTypeNames() {
        assertEquals("HDR10", DisplayInfo.hdrTypeName(Display.HdrCapabilities.HDR_TYPE_HDR10))
        assertEquals("Dolby Vision", DisplayInfo.hdrTypeName(Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION))
        assertEquals("HLG", DisplayInfo.hdrTypeName(Display.HdrCapabilities.HDR_TYPE_HLG))
        assertEquals("HDR10+", DisplayInfo.hdrTypeName(Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS))
        assertNull(DisplayInfo.hdrTypeName(99))
    }

    @Test
    fun hdrTypeLabelJoinsAndHandlesEmpty() {
        assertEquals("None reported", DisplayInfo.hdrTypeLabel(IntArray(0)))
        assertEquals(
            "HDR10 · HLG",
            DisplayInfo.hdrTypeLabel(
                intArrayOf(
                    Display.HdrCapabilities.HDR_TYPE_HDR10,
                    Display.HdrCapabilities.HDR_TYPE_HLG,
                ),
            ),
        )
        assertEquals("Unknown (99)", DisplayInfo.hdrTypeLabel(intArrayOf(99)))
    }

    @Test
    fun rotationNames() {
        assertEquals("0°", DisplayInfo.rotationName(0))
        assertEquals("90°", DisplayInfo.rotationName(1))
        assertEquals("180°", DisplayInfo.rotationName(2))
        assertEquals("270°", DisplayInfo.rotationName(3))
        assertEquals("Unknown (7)", DisplayInfo.rotationName(7))
    }

    @Test
    fun stateNames() {
        assertEquals("On", DisplayInfo.stateName(2))
        assertEquals("Doze", DisplayInfo.stateName(3))
        assertEquals("Doze (suspended)", DisplayInfo.stateName(4))
        assertEquals("Unknown", DisplayInfo.stateName(0))
        assertEquals("Unknown (77)", DisplayInfo.stateName(77))
    }

    @Test
    fun densityBuckets() {
        assertEquals("mdpi", DisplayInfo.densityBucketName(160))
        assertEquals("hdpi", DisplayInfo.densityBucketName(240))
        assertEquals("xxhdpi", DisplayInfo.densityBucketName(480))
        assertEquals("custom", DisplayInfo.densityBucketName(390))
        assertEquals("420 dpi (custom)", DisplayInfo.densityLabel(420))
        assertEquals("160 dpi (mdpi)", DisplayInfo.densityLabel(160))
    }

    @Test
    fun physicalSizeEstimate() {
        // 2400px at 400 dpi -> 152.4mm; 1080px at 400dpi -> 68.58mm
        val size = DisplayInfo.physicalSizeMm(2400, 1080, 400f, 400f)
        assertEquals(152.4f, size!!.first, 0.05f)
        assertEquals(68.58f, size.second, 0.05f)
        assertNull(DisplayInfo.physicalSizeMm(2400, 1080, 0f, 400f))
        assertNull(DisplayInfo.physicalSizeMm(0, 1080, 400f, 400f))
        assertEquals("152.4 mm x 68.6 mm", DisplayInfo.physicalSizeLabel(size))
        assertEquals("Unavailable", DisplayInfo.physicalSizeLabel(null))
    }

    @Test
    fun diagonalInches() {
        // 6.0in x 2.7in ≈ 6.58" diagonal
        val d = DisplayInfo.diagonalInches(Pair(152.4f, 68.58f))
        assertEquals("6.6″", d)
        assertNull(DisplayInfo.diagonalInches(null))
    }
}

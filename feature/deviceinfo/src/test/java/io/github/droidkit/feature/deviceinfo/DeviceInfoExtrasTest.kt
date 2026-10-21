package io.github.droidkit.feature.deviceinfo

import io.github.droidkit.core.system.DeviceSystemProbe.CodecEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceInfoExtrasTest {

    private fun vulkanBits(major: Int, minor: Int, patch: Int) =
        (major shl 22) or (minor shl 12) or patch

    @Test
    fun vulkanVersionNameDecodesBits() {
        assertEquals("1.1.128", DeviceInfoExtras.vulkanVersionName(vulkanBits(1, 1, 128)))
        assertEquals("1.3.0", DeviceInfoExtras.vulkanVersionName(vulkanBits(1, 3, 0)))
    }

    @Test
    fun deqpDateNameDecodesDate() {
        assertEquals("2020-03-01", DeviceInfoExtras.deqpDateName((2020 shl 16) or (3 shl 8) or 1))
        // Real reported value 0x07E30301 = 2019-03-01
        assertEquals("2019-03-01", DeviceInfoExtras.deqpDateName(0x07E30301))
    }

    @Test
    fun codecSourceNameClassifiesFamilies() {
        assertEquals("Codec 2.0", DeviceInfoExtras.codecSourceName("c2.qti.avc.decoder"))
        assertEquals("Android platform (SW)", DeviceInfoExtras.codecSourceName("OMX.google.aac.decoder"))
        assertEquals("OMX vendor", DeviceInfoExtras.codecSourceName("OMX.qcom.video.decoder.avc"))
        assertEquals("Other", DeviceInfoExtras.codecSourceName("media.custom.thing"))
    }

    @Test
    fun codecFlagsOmitUnknown() {
        val api28Style = codec(isHardwareAccelerated = null, isSoftwareOnly = null)
        assertTrue(DeviceInfoExtras.codecFlags(api28Style).isEmpty())

        val hw = codec(isHardwareAccelerated = true, isVendor = true)
        assertEquals(listOf("Hardware", "Vendor"), DeviceInfoExtras.codecFlags(hw))

        val sw = codec(isHardwareAccelerated = false, isSoftwareOnly = true)
        assertEquals(listOf("Not hardware", "Software"), DeviceInfoExtras.codecFlags(sw))
    }

    @Test
    fun filterCodecsByRoleAndQuery() {
        val list = listOf(
            codec(name = "c2.qti.avc.decoder", types = listOf("video/avc")),
            codec(name = "OMX.google.aac.decoder", types = listOf("audio/mp4a-latm")),
            codec(name = "c2.qti.avc.encoder", isEncoder = true, types = listOf("video/avc")),
        )
        assertEquals(3, DeviceInfoExtras.filterCodecs(list, "all", "").size)
        assertEquals(2, DeviceInfoExtras.filterCodecs(list, "decoders", "").size)
        assertEquals(1, DeviceInfoExtras.filterCodecs(list, "encoders", "").size)
        assertEquals(1, DeviceInfoExtras.filterCodecs(list, "all", "aac").size)
        assertEquals(1, DeviceInfoExtras.filterCodecs(list, "decoders", "video/avc").size)
        assertTrue(DeviceInfoExtras.filterCodecs(list, "all", "zzz").isEmpty())
    }

    @Test
    fun filterCodecsSortsDecodersFirst() {
        val list = listOf(
            codec(name = "b.encoder", isEncoder = true),
            codec(name = "a.decoder"),
        )
        val shown = DeviceInfoExtras.filterCodecs(list, "all", "")
        assertEquals("a.decoder", shown[0].name)
    }

    private fun codec(
        name: String = "test.codec",
        isEncoder: Boolean = false,
        isHardwareAccelerated: Boolean? = null,
        isSoftwareOnly: Boolean? = null,
        isVendor: Boolean? = null,
        isAlias: Boolean? = null,
        types: List<String> = emptyList(),
    ) = CodecEntry(name, isEncoder, isHardwareAccelerated, isSoftwareOnly, isVendor, isAlias, types)
}

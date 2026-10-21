package io.github.droidkit.feature.deviceinfo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceDeepProbeParserTest {

    @Test
    fun `getprop table parses bracketed entries`() {
        val raw = """
            [ro.product.model]: [Pixel 8]
            [ro.build.version.sdk]: [34]
            [dalvik.vm.heapsize]: [512m]
        """.trimIndent()
        val props = parseProps(raw)
        assertEquals(3, props.size)
        assertEquals(PropEntry("ro.product.model", "Pixel 8"), props[0])
        assertEquals(PropEntry("dalvik.vm.heapsize", "512m"), props[2])
    }

    @Test
    fun `thermal zones pair type with millidegree temp`() {
        val raw = """
            == /sys/class/thermal/thermal_zone0
            soc-thermal
            42100
            == /sys/class/thermal/thermal_zone1
            battery
            31500
        """.trimIndent()
        val zones = parseThermalZones(raw)
        assertEquals(2, zones.size)
        assertEquals(ThermalZone("thermal_zone0", "soc-thermal", 42100), zones[0])
        assertEquals("battery", zones[1].type)
        assertEquals(31500L, zones[1].milliCelsius)
    }

    @Test
    fun `cpuinfo counts processors and reads hardware`() {
        val raw = """
            processor	: 0
            model name	: AArch64 Processor rev 12
            Features	: fp asimd evtstrm aes pmull sha1 sha2 crc32
            processor	: 1
            Hardware	: Qualcomm Technologies, Inc SM8475
        """.trimIndent()
        val cpu = parseCpuInfo(raw)!!
        assertEquals(2, cpu.coreCount)
        assertEquals("Qualcomm Technologies, Inc SM8475", cpu.hardware)
        assertEquals("AArch64 Processor rev 12", cpu.modelName)
        assertNull(parseCpuInfo(""))
    }

    @Test
    fun `meminfo extracts totals`() {
        val raw = """
            MemTotal:       11730904 kB
            MemFree:          456789 kB
            Buffers:           12345 kB
            Cached:          2345678 kB
        """.trimIndent()
        val mem = parseMemInfo(raw)!!
        assertEquals(11730904L, mem.totalKb)
        assertEquals(456789L, mem.freeKb)
        assertEquals(12345L, mem.buffersKb)
        assertEquals(2345678L, mem.cachedKb)
        assertNull(parseMemInfo(""))
    }

    @Test
    fun `partitions skips header and blank lines`() {
        val raw = """
            major minor  #blocks  name

               8        0  125034840 sda
               8        1      1024 sda1
             259        0   52428800 mmcblk0p42
        """.trimIndent()
        val parts = parsePartitions(raw)
        assertEquals(3, parts.size)
        assertEquals(Partition(8, 0, 125034840, "sda"), parts[0])
        assertEquals("mmcblk0p42", parts[2].name)
    }

    @Test
    fun `battery dump yields key value pairs`() {
        val raw = """
            AC powered: false
            USB powered: true
            status: 2
            health: 2
            voltage: 4150
        """.trimIndent()
        val kv = parseBatteryDump(raw)
        assertTrue(("status" to "2") in kv)
        assertTrue(("voltage" to "4150") in kv)
    }

    @Test
    fun `learned capacity keeps its unit label`() {
        val learned = parseLearnedCapacity("Learned battery capacity: 3018 mAh")
        assertEquals("3018 mAh (learned)", learned)
        val counter = parseLearnedCapacity("  Charge counter: 2500000")
        assertEquals("2500000 µAh (charge counter)", counter)
        assertNull(parseLearnedCapacity("nothing useful"))
    }

    @Test
    fun `wifi link dump parses fields and generation`() {
        val raw = """
            mWifiInfo SSID: "mywifi", BSSID: aa:bb:cc:dd:ee:ff, RSSI: -55, Link speed: 866Mbps, Frequency: 5180MHz
            Wi-Fi generation: Wi-Fi 6
        """.trimIndent()
        val link = parseWifiLinkDump(raw)!!
        assertEquals("mywifi", link.ssid)
        assertEquals("aa:bb:cc:dd:ee:ff", link.bssid)
        assertEquals(-55, link.rssiDbm)
        assertEquals(866, link.linkSpeedMbps)
        assertEquals(5180, link.frequencyMhz)
        assertEquals("Wi-Fi 6", link.wifiGeneration)
        assertNull(parseWifiLinkDump(""))
    }

    @Test
    fun `getenforce output normalizes to title case`() {
        assertEquals("Enforcing", parseSeLinuxStatus("Enforcing\n"))
        assertEquals("Permissive", parseSeLinuxStatus(" permissive "))
        assertNull(parseSeLinuxStatus("getenforce: not found"))
        assertNull(parseSeLinuxStatus(""))
    }

    @Test
    fun `link addresses pair iface name with mac`() {
        val raw = """
            == /sys/class/net/lo/address
            00:00:00:00:00:00
            == /sys/class/net/wlan0/address
            3C:9C:0F:12:34:56
            == /sys/class/net/rmnet_data0/address
        """.trimIndent()
        val links = parseLinkAddresses(raw)
        assertEquals(listOf("lo" to "00:00:00:00:00:00", "wlan0" to "3c:9c:0f:12:34:56"), links)
        assertTrue(parseLinkAddresses("").isEmpty())
    }

    @Test
    fun `settings entries keep namespace dot keys and skip null`() {
        val raw = """
            == global.device_name
            My Pixel 8 Pro
            == secure.bluetooth_address
            null
            == global.adb_enabled
            1
        """.trimIndent()
        val entries = parseSettingsEntries(raw)
        assertEquals("My Pixel 8 Pro", entries["global.device_name"])
        assertTrue("secure.bluetooth_address" !in entries)
        assertEquals("1", entries["global.adb_enabled"])
    }

    @Test
    fun `phone identity extracts 15-digit imei or returns null`() {
        assertEquals(
            "351234567890123",
            parsePhoneIdentity("  Device ID: 351234567890123"),
        )
        assertEquals("A000002F123456", parsePhoneIdentity("MEID=A000002F123456"))
        assertNull(parsePhoneIdentity("Phone service running\n no identifiers"))
        assertNull(parsePhoneIdentity(""))
    }
}

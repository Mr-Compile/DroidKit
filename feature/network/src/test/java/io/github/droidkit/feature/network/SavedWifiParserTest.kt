package io.github.droidkit.feature.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedWifiParserTest {

    private val apexStore = """
        == /data/misc/apexdata/com.android.wifi/WifiConfigStore.xml
        <?xml version='1.0' encoding='utf-8'?>
        <NetworkList>
          <Network>
            <WifiConfiguration>
              <string name="ConfigKey">&quot;HomeWifi&quot;WPA_PSK</string>
              <string name="SSID">&quot;HomeWifi&quot;</string>
              <string name="PreSharedKey">&quot;s3cret pass&quot;</string>
            </WifiConfiguration>
          </Network>
          <Network>
            <WifiConfiguration>
              <string name="ConfigKey">&quot;Cafe&quot;SAE</string>
              <string name="SSID">&quot;Cafe&quot;</string>
              <string name="PreSharedKey">0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef</string>
            </WifiConfiguration>
          </Network>
          <Network>
            <WifiConfiguration>
              <string name="ConfigKey">&quot;OpenNet&quot;NONE</string>
              <string name="SSID">&quot;OpenNet&quot;</string>
            </WifiConfiguration>
          </Network>
        </NetworkList>
    """.trimIndent()

    private val supplicantConf = """
        == /data/misc/wifi/wpa_supplicant.conf
        ctrl_interface=DIR=/data/system/wpa_supplicant GROUP=wifi
        network={
        	ssid="OldNet"
        	psk="hunter2"
        	key_mgmt=WPA-PSK
        	priority=1
        }
        network={
        	ssid="HexNet"
        	psk=abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789
        	key_mgmt=WPA-PSK
        }
        network={
        	ssid="Escaped\x20Net"
        	key_mgmt=NONE
        }
    """.trimIndent()

    @Test
    fun `xml store yields ssid security and passphrase`() {
        val r = SavedWifiParser.parse(apexStore)
        assertEquals("/data/misc/apexdata/com.android.wifi/WifiConfigStore.xml", r.sourcePath)
        assertEquals(3, r.networks.size)

        val home = r.networks[0]
        assertEquals("HomeWifi", home.ssid)
        assertEquals("WPA_PSK", home.security)
        assertEquals("s3cret pass", home.psk)
        assertFalse(home.pskIsHex)

        val cafe = r.networks[1]
        assertEquals("SAE", cafe.security)
        assertTrue(cafe.pskIsHex)
        assertEquals(64, cafe.psk!!.length)

        val open = r.networks[2]
        assertEquals("NONE", open.security)
        assertNull(open.psk)
    }

    @Test
    fun `supplicant conf yields passphrases hex psks and decoded ssids`() {
        val r = SavedWifiParser.parse(supplicantConf)
        assertEquals("/data/misc/wifi/wpa_supplicant.conf", r.sourcePath)
        assertEquals(3, r.networks.size)

        assertEquals("OldNet", r.networks[0].ssid)
        assertEquals("hunter2", r.networks[0].psk)
        assertEquals("WPA-PSK", r.networks[0].security)

        assertTrue(r.networks[1].pskIsHex)
        assertEquals("Escaped Net", r.networks[2].ssid)
        assertNull(r.networks[2].psk)
    }

    @Test
    fun `first section with readable networks wins as source`() {
        val both = apexStore + "\n" + supplicantConf
        val r = SavedWifiParser.parse(both)
        assertEquals("/data/misc/apexdata/com.android.wifi/WifiConfigStore.xml", r.sourcePath)
        assertEquals(6, r.networks.size)
    }

    @Test
    fun `ssid derived from config key when SSID entry missing`() {
        val body = """
            <Network><WifiConfiguration>
              <string name="ConfigKey">&quot;KeyOnly&quot;WPA_PSK</string>
              <string name="PreSharedKey">&quot;pw&quot;</string>
            </WifiConfiguration></Network>
        """.trimIndent()
        val r = SavedWifiParser.parse(body)
        assertEquals("KeyOnly", r.networks.single().ssid)
    }

    @Test
    fun `unreadable or empty input yields no networks never fabricated`() {
        assertTrue(SavedWifiParser.parse("").networks.isEmpty())
        assertTrue(SavedWifiParser.parse("== /data/misc/wifi/WifiConfigStore.xml\n").networks.isEmpty())
        assertTrue(
            SavedWifiParser.parse("== /data/misc/wifi/WifiConfigStore.xml\n<NetworkList></NetworkList>")
                .networks.isEmpty(),
        )
        assertNull(SavedWifiParser.parse("random\nnoise").sourcePath)
    }

    @Test
    fun `unmarked file body still parses`() {
        val body = """
            <Network><WifiConfiguration>
              <string name="ConfigKey">&quot;Solo&quot;WPA_PSK</string>
              <string name="SSID">&quot;Solo&quot;</string>
              <string name="PreSharedKey">&quot;p@ss!&quot;</string>
            </WifiConfiguration></Network>
        """.trimIndent()
        val r = SavedWifiParser.parse(body)
        assertNull(r.sourcePath)
        assertEquals("p@ss!", r.networks.single().psk)
    }
}

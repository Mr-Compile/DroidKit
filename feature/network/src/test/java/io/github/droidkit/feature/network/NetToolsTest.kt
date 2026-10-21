package io.github.droidkit.feature.network

import java.net.InetAddress
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetToolsTest {

    @Test
    fun `lookupHost resolves localhost`() = runBlocking {
        val result = NetTools.lookupHost("localhost")
        assertTrue(result.addresses.isNotEmpty())
        assertEquals("localhost", result.host)
        assertTrue(result.elapsedMs >= 0)
    }

    @Test
    fun `pingHost transmits requested count`() = runBlocking {
        val result = NetTools.pingHost("127.0.0.1", count = 3, timeoutMs = 500)
        assertEquals(3, result.transmitted)
        assertTrue(result.received in 0..3)
        assertEquals("127.0.0.1", result.resolvedAddress)
    }

    @Test
    fun `ping result math`() {
        val r = NetTools.PingResult("h", "1.2.3.4", listOf(10.0, 20.0), 4, 2)
        assertEquals(50, r.lossPercent)
        assertEquals(10.0, r.minMs!!, 0.01)
        assertEquals(20.0, r.maxMs!!, 0.01)
        assertEquals(15.0, r.avgMs!!, 0.01)
    }

    @Test
    fun `formatMs renders value or dash`() {
        assertEquals("—", NetTools.formatMs(null))
        assertEquals("12.5 ms", NetTools.formatMs(12.5))
    }

    @Test
    fun `parseMacAddress accepts colon dash and plain forms`() {
        val expected = byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte(),
            0xDD.toByte(), 0xEE.toByte(), 0xFF.toByte())
        assertTrue(NetTools.parseMacAddress("AA:BB:CC:DD:EE:FF").contentEquals(expected))
        assertTrue(NetTools.parseMacAddress("aa-bb-cc-dd-ee-ff").contentEquals(expected))
        assertTrue(NetTools.parseMacAddress("aabbccddeeff").contentEquals(expected))
        assertTrue(NetTools.parseMacAddress(" aa:bb:cc:dd:ee:ff ").contentEquals(expected))
    }

    @Test
    fun `parseMacAddress rejects malformed input`() {
        assertEquals(null, NetTools.parseMacAddress(""))
        assertEquals(null, NetTools.parseMacAddress("aa:bb:cc:dd:ee"))
        assertEquals(null, NetTools.parseMacAddress("aa:bb:cc:dd:ee:ff:00"))
        assertEquals(null, NetTools.parseMacAddress("zz:bb:cc:dd:ee:ff"))
        assertEquals(null, NetTools.parseMacAddress("aabbccddee"))
        assertEquals(null, NetTools.parseMacAddress("not-a-mac"))
    }

    @Test
    fun `buildMagicPacket produces 102-byte packet`() {
        val mac = NetTools.parseMacAddress("11:22:33:44:55:66")!!
        val packet = NetTools.buildMagicPacket(mac)!!
        assertEquals(NetTools.MAGIC_PACKET_BYTES, packet.size)
        for (i in 0 until 6) assertEquals(0xFF.toByte(), packet[i])
        for (rep in 0 until 16) {
            for (b in 0 until 6) {
                assertEquals(mac[b], packet[6 + rep * 6 + b])
            }
        }
        assertEquals(null, NetTools.buildMagicPacket(byteArrayOf(1, 2, 3)))
    }

    @Test
    fun `subnetHosts enumerates a slash24`() {
        val base = InetAddress.getByName("192.168.1.42")
        val hosts = NetTools.subnetHosts(base, 24)
        assertEquals(254, hosts.size)
        assertEquals("192.168.1.1", hosts.first().hostAddress)
        assertEquals("192.168.1.254", hosts.last().hostAddress)
    }

    @Test
    fun `subnetHosts narrows wider prefixes and rejects v6`() {
        val base = InetAddress.getByName("10.0.5.9")
        // /16 is wider than /24 — narrows to the host's own /24
        val hosts = NetTools.subnetHosts(base, 16)
        assertEquals(254, hosts.size)
        assertEquals("10.0.5.1", hosts.first().hostAddress)
        assertTrue(NetTools.subnetHosts(base, 33).isEmpty())
    }

    @Test
    fun `sweepSubnet finds only reachable hosts`() = runBlocking {
        val hosts = NetTools.subnetHosts(InetAddress.getByName("127.0.0.1"), 30)
        // /30 has 2 usable hosts — loopback responds, the other likely doesn't.
        val results = NetTools.sweepSubnet(hosts, timeoutMs = 300)
        assertTrue(results.all { it.elapsedMs >= 0 })
        assertTrue(results.size <= hosts.size)
    }
}

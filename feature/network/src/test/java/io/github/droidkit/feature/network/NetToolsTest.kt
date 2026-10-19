package io.github.droidkit.feature.network

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
}

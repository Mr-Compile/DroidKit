package io.github.droidkit.feature.network

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

/** Reachability and DNS helpers — pure JVM, unit-testable. */
object NetTools {

    const val PING_DEFAULT_COUNT = 4
    const val PING_DEFAULT_TIMEOUT_MS = 1_500

    data class PingResult(
        val host: String,
        val resolvedAddress: String?,
        val samplesMs: List<Double>,
        val transmitted: Int,
        val received: Int,
    ) {
        val lossPercent: Int get() = ((transmitted - received) * 100) / transmitted.coerceAtLeast(1)
        val minMs: Double? get() = samplesMs.minOrNull()
        val maxMs: Double? get() = samplesMs.maxOrNull()
        val avgMs: Double? get() = samplesMs.takeIf { it.isNotEmpty() }?.average()
    }

    data class DnsResult(
        val host: String,
        val addresses: List<String>,
        val elapsedMs: Long,
    )

    /**
     * ICMP reachability probe. Caveat: many networks and Android builds block ICMP echo;
     * failure means "not reachable via probe", not necessarily "host down".
     */
    suspend fun pingHost(
        host: String,
        count: Int = PING_DEFAULT_COUNT,
        timeoutMs: Int = PING_DEFAULT_TIMEOUT_MS,
    ): PingResult = withContext(Dispatchers.IO) {
        val address = InetAddress.getByName(host)
        val samples = mutableListOf<Double>()
        var received = 0
        repeat(count) {
            val start = System.nanoTime()
            if (runCatching { address.isReachable(timeoutMs) }.getOrDefault(false)) {
                received += 1
                samples += (System.nanoTime() - start) / 1_000_000.0
            }
        }
        PingResult(host, address.hostAddress, samples, count, received)
    }

    /** Resolves a hostname through the device's configured resolver. */
    suspend fun lookupHost(host: String): DnsResult = withContext(Dispatchers.IO) {
        val start = System.nanoTime()
        val addresses = InetAddress.getAllByName(host).mapNotNull { it.hostAddress }
        DnsResult(host, addresses, (System.nanoTime() - start) / 1_000_000)
    }

    data class PortResult(
        val host: String,
        val port: Int,
        val open: Boolean,
        val elapsedMs: Double,
    )

    /**
     * TCP connect probe for user-specified endpoints. Ports are validated
     * 1–65535; probes run in parallel with a per-port timeout.
     */
    suspend fun probePorts(
        host: String,
        ports: List<Int>,
        timeoutMs: Int = 2_000,
    ): List<PortResult> = withContext(Dispatchers.IO) {
        val address = InetAddress.getByName(host)
        ports.distinct().take(MAX_PORTS).map { port ->
            async {
                val start = System.nanoTime()
                val open = runCatching {
                    Socket().use { it.connect(InetSocketAddress(address, port), timeoutMs) }
                }.isSuccess
                PortResult(host, port, open, (System.nanoTime() - start) / 1_000_000.0)
            }
        }.awaitAll()
    }

    const val MAX_PORTS = 32

    // ---- Wake-on-LAN ----

    const val WOL_DEFAULT_PORT = 9
    const val MAGIC_PACKET_BYTES = 102 // 6 × 0xFF header + 16 × 6-byte MAC

    /**
     * Parses `aa:bb:cc:dd:ee:ff`, `aa-bb-…`, or `aabbccddeeff` into 6 bytes.
     * Returns null for anything else — no silent truncation.
     */
    fun parseMacAddress(text: String): ByteArray? {
        val t = text.trim()
        val hex = when {
            t.contains(":") || t.contains("-") -> t.split(':', '-')
                .takeIf { it.size == 6 && it.all { p -> p.length in 1..2 } }
                ?.joinToString("") { p -> p.padStart(2, '0') }
            else -> t
        } ?: return null
        if (hex.length != 12 || hex.any { it !in '0'..'9' && it !in 'a'..'f' && it !in 'A'..'F' }) {
            return null
        }
        return ByteArray(6) { i -> hex.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
    }

    /** Standard 102-byte magic packet: 0xFF×6 then the target MAC ×16. */
    fun buildMagicPacket(mac: ByteArray): ByteArray? {
        if (mac.size != 6) return null
        val packet = ByteArray(MAGIC_PACKET_BYTES)
        for (i in 0 until 6) packet[i] = 0xFF.toByte()
        for (rep in 0 until 16) {
            mac.copyInto(packet, destinationOffset = 6 + rep * 6)
        }
        return packet
    }

    /**
     * Broadcasts a magic packet. WoL ports 7/9 conventionally; broadcast address
     * 255.255.255.255 reaches the local segment without needing a subnet read.
     */
    suspend fun sendMagicPacket(
        mac: ByteArray,
        port: Int = WOL_DEFAULT_PORT,
    ): Boolean = withContext(Dispatchers.IO) {
        val packet = buildMagicPacket(mac) ?: return@withContext false
        if (port !in 1..65535) return@withContext false
        runCatching {
            DatagramSocket().use { socket ->
                socket.broadcast = true
                val target = InetAddress.getByName("255.255.255.255")
                socket.send(DatagramPacket(packet, packet.size, target, port))
            }
        }.isSuccess
    }

    // ---- LAN sweep ----

    const val SWEEP_MAX_HOSTS = 254
    const val SWEEP_TIMEOUT_MS = 400

    data class SweepResult(
        val address: String,
        val elapsedMs: Double,
    )

    /**
     * Enumerates usable host addresses for an IPv4 address + prefix length.
     * Capped at [SWEEP_MAX_HOSTS] — anything wider than /24 is narrowed to the
     * interface's own /24 rather than sweeping thousands of addresses.
     */
    fun subnetHosts(address: InetAddress, prefixLength: Int): List<InetAddress> {
        if (address !is Inet4Address || prefixLength !in 0..32) return emptyList()
        val effectivePrefix = prefixLength.coerceAtLeast(24)
        val addr = address.address.fold(0L) { acc, b -> (acc shl 8) or (b.toLong() and 0xFF) }
        val hostBits = 32 - effectivePrefix
        val hostCount = (1L shl hostBits) - 2 // exclude network + broadcast
        if (hostCount < 1 || hostCount > SWEEP_MAX_HOSTS) return emptyList()
        val mask = (-1L shl hostBits) and 0xFFFFFFFFL
        val base = addr and mask
        return (1L..hostCount).map { i ->
            val v = base + i
            InetAddress.getByAddress(
                byteArrayOf(
                    ((v shr 24) and 0xFF).toByte(),
                    ((v shr 16) and 0xFF).toByte(),
                    ((v shr 8) and 0xFF).toByte(),
                    (v and 0xFF).toByte(),
                ),
            )
        }
    }

    /**
     * Parallel reachability sweep of a subnet's host list. Uses ICMP echo via
     * [InetAddress.isReachable] — devices that ignore ping do not appear, so
     * absence is not proof a host is down. Caller time-boxes the whole sweep.
     */
    suspend fun sweepSubnet(
        hosts: List<InetAddress>,
        timeoutMs: Int = SWEEP_TIMEOUT_MS,
    ): List<SweepResult> = withContext(Dispatchers.IO) {
        hosts.take(SWEEP_MAX_HOSTS).map { host ->
            async {
                val start = System.nanoTime()
                val up = runCatching { host.isReachable(timeoutMs) }.getOrDefault(false)
                if (up) {
                    SweepResult(
                        address = host.hostAddress ?: host.toString(),
                        elapsedMs = (System.nanoTime() - start) / 1_000_000.0,
                    )
                } else {
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    fun formatMs(ms: Double?): String = ms?.let { "%.1f ms".format(it) } ?: "—"
}

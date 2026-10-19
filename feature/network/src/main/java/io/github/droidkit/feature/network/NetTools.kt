package io.github.droidkit.feature.network

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

    fun formatMs(ms: Double?): String = ms?.let { "%.1f ms".format(it) } ?: "—"
}

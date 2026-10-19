package io.github.droidkit.feature.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.droidkit.core.common.DLog
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NetworkViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val cm =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    data class ConnectionSnapshot(
        val available: Boolean,
        val transports: List<String>,
        val internetCapable: Boolean?,
        val validated: Boolean?,
        val metered: Boolean?,
        val downKbps: Int?,
        val upKbps: Int?,
        val interfaceName: String?,
        val addresses: List<String>,
        val dnsServers: List<String>,
        val routes: Int,
        val mtu: Int?,
        val privateDns: String?,
        val captivePortal: Boolean?,
    )

    enum class LayerState { PASS, FAIL, UNKNOWN, NOT_TESTED }

    data class LayerResult(val name: String, val state: LayerState, val detail: String)

    sealed interface ToolState<out T> {
        data object Idle : ToolState<Nothing>
        data object Running : ToolState<Nothing>
        data class Done<T>(val result: T) : ToolState<T>
        data class Failed(val message: String) : ToolState<Nothing>
    }

    private val _snapshot = MutableStateFlow<ConnectionSnapshot?>(null)
    val snapshot: StateFlow<ConnectionSnapshot?> = _snapshot.asStateFlow()

    private val _layers = MutableStateFlow<List<LayerResult>>(emptyList())
    val layers: StateFlow<List<LayerResult>> = _layers.asStateFlow()

    private val _ping = MutableStateFlow<ToolState<NetTools.PingResult>>(ToolState.Idle)
    val ping: StateFlow<ToolState<NetTools.PingResult>> = _ping.asStateFlow()

    private val _dns = MutableStateFlow<ToolState<NetTools.DnsResult>>(ToolState.Idle)
    val dns: StateFlow<ToolState<NetTools.DnsResult>> = _dns.asStateFlow()

    private val _ports = MutableStateFlow<ToolState<List<NetTools.PortResult>>>(ToolState.Idle)
    val ports: StateFlow<ToolState<List<NetTools.PortResult>>> = _ports.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = refreshSnapshot()
        override fun onLost(network: Network) = refreshSnapshot()
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) =
            refreshSnapshot()
        override fun onLinkPropertiesChanged(network: Network, props: LinkProperties) =
            refreshSnapshot()
    }

    init {
        runCatching { cm?.registerDefaultNetworkCallback(callback) }
        refresh()
    }

    fun refresh() {
        refreshSnapshot()
        runDiagnostics()
    }

    private fun refreshSnapshot() {
        _snapshot.value = readSnapshot()
    }

    private fun readSnapshot(): ConnectionSnapshot {
        val network = cm?.activeNetwork
        val caps = network?.let { cm?.getNetworkCapabilities(it) }
        val link = network?.let { cm?.getLinkProperties(it) }
        return ConnectionSnapshot(
            available = network != null,
            transports = caps?.let { c ->
                buildList {
                    if (c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) add("Wi-Fi")
                    if (c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) add("Cellular")
                    if (c.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) add("Ethernet")
                    if (c.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) add("Bluetooth")
                    if (c.hasTransport(NetworkCapabilities.TRANSPORT_USB)) add("USB")
                    if (c.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) add("VPN")
                }
            }.orEmpty(),
            internetCapable = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
            validated = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
            metered = caps?.let { !it.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) },
            downKbps = caps?.linkDownstreamBandwidthKbps?.takeIf { it > 0 },
            upKbps = caps?.linkUpstreamBandwidthKbps?.takeIf { it > 0 },
            interfaceName = link?.interfaceName,
            addresses = link?.linkAddresses?.map { it.address.hostAddress ?: "?" }.orEmpty(),
            dnsServers = link?.dnsServers?.mapNotNull { it.hostAddress }.orEmpty(),
            routes = link?.routes?.size ?: 0,
            mtu = link?.mtu?.takeIf { it > 0 },
            privateDns = link?.privateDnsServerName,
            captivePortal = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL),
        )
    }

    /**
     * Layer-by-layer diagnostic — each layer reported independently; no aggregate score.
     * Layer 3 resolves a well-known host; Layer 4 probes the device's own DNS server.
     */
    fun runDiagnostics() {
        val snap = readSnapshot()
        val base = mutableListOf(
            LayerResult(
                "Network available",
                if (snap.available) LayerState.PASS else LayerState.FAIL,
                if (snap.available) "Active network: ${snap.transports.joinToString()}" else "No active network",
            ),
            LayerResult(
                "Internet capability",
                when (snap.validated) {
                    true -> LayerState.PASS
                    false -> LayerState.FAIL
                    null -> LayerState.UNKNOWN
                },
                when (snap.validated) {
                    true -> "NET_CAPABILITY_VALIDATED"
                    false -> "Not validated (captive portal or no route)"
                    null -> "No capabilities read"
                },
            ),
            LayerResult("DNS resolution", LayerState.NOT_TESTED, "Running…"),
            LayerResult("Reachability", LayerState.NOT_TESTED, "Running…"),
        )
        _layers.value = base
        viewModelScope.launch {
            val dns = runCatching { NetTools.lookupHost(DNS_PROBE_HOST) }.getOrNull()
            base[2] = LayerResult(
                "DNS resolution",
                if (dns != null && dns.addresses.isNotEmpty()) LayerState.PASS else LayerState.FAIL,
                if (dns != null) "$DNS_PROBE_HOST → ${dns.addresses.size} address(es) in ${dns.elapsedMs} ms"
                else "Lookup failed",
            )
            _layers.value = base.toList()
            val target = snap.dnsServers.firstOrNull()
            if (target == null) {
                base[3] = LayerResult("Reachability", LayerState.UNKNOWN, "No DNS server to probe")
            } else {
                val ping = runCatching { NetTools.pingHost(target, count = 2) }.getOrNull()
                base[3] = LayerResult(
                    "Reachability",
                    if (ping != null && ping.received > 0) LayerState.PASS else LayerState.FAIL,
                    if (ping != null) "DNS server $target — ${ping.received}/${ping.transmitted} replies (ICMP may be filtered)"
                    else "Probe failed",
                )
            }
            _layers.value = base.toList()
        }
    }

    fun ping(host: String) {
        val target = host.trim()
        if (target.isEmpty()) return
        _ping.value = ToolState.Running
        viewModelScope.launch {
            _ping.value = runCatching { NetTools.pingHost(target) }
                .fold(
                    onSuccess = { ToolState.Done(it) },
                    onFailure = { ToolState.Failed(it.message ?: it.javaClass.simpleName) },
                )
        }
    }

    fun lookup(host: String) {
        val target = host.trim()
        if (target.isEmpty()) return
        _dns.value = ToolState.Running
        viewModelScope.launch {
            _dns.value = runCatching { NetTools.lookupHost(target) }
                .fold(
                    onSuccess = { ToolState.Done(it) },
                    onFailure = { ToolState.Failed(it.message ?: it.javaClass.simpleName) },
                )
        }
    }

    /** Probes comma/space-separated ports on a user-specified host. */
    fun probePorts(host: String, portsText: String) {
        val target = host.trim()
        if (target.isEmpty()) return
        val parsed = portsText.split(Regex("[,\\s]+"))
            .mapNotNull { it.toIntOrNull() }
            .filter { it in 1..65535 }
            .distinct()
        if (parsed.isEmpty()) {
            _ports.value = ToolState.Failed("No valid ports (1–65535, comma or space separated)")
            return
        }
        _ports.value = ToolState.Running
        viewModelScope.launch {
            _ports.value = runCatching { NetTools.probePorts(target, parsed) }
                .fold(
                    onSuccess = { ToolState.Done(it) },
                    onFailure = { ToolState.Failed(it.message ?: it.javaClass.simpleName) },
                )
        }
    }

    override fun onCleared() {
        runCatching { cm?.unregisterNetworkCallback(callback) }
        super.onCleared()
    }

    companion object {
        private const val DNS_PROBE_HOST = "dns.google"
    }
}

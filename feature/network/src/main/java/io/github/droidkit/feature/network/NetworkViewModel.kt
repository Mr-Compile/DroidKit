package io.github.droidkit.feature.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.droidkit.core.common.DLog
import io.github.droidkit.core.privileges.ElevatedOps
import io.github.droidkit.core.privileges.ProviderKind
import java.net.Inet4Address
import java.net.NetworkInterface
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@HiltViewModel
class NetworkViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val elevatedOps: ElevatedOps,
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

    private val _wol = MutableStateFlow<ToolState<String>>(ToolState.Idle)
    val wol: StateFlow<ToolState<String>> = _wol.asStateFlow()

    private val _sweep = MutableStateFlow<ToolState<SweepSummary>>(ToolState.Idle)
    val sweep: StateFlow<ToolState<SweepSummary>> = _sweep.asStateFlow()

    data class SweepSummary(
        val subnet: String,
        val hostsScanned: Int,
        val results: List<NetTools.SweepResult>,
        val elapsedMs: Long,
    )

    /** True once a ROOT executor is live — gates the "saved networks" hint text. */
    private val _rootReady = MutableStateFlow<Boolean?>(null)
    val rootReady: StateFlow<Boolean?> = _rootReady.asStateFlow()

    // ---- Wi-Fi link details ----

    /**
     * Live Wi-Fi link fields. SSID/BSSID are only populated when the caller
     * holds ACCESS_FINE_LOCATION — [needsLocation] marks the gated case so the
     * UI can offer a grant instead of silently showing placeholders.
     */
    data class WifiLink(
        val ssid: String?,
        val bssid: String?,
        val rssiDbm: Int?,
        val linkSpeedMbps: Int?,
        val frequencyMhz: Int?,
        val band: String?,
        val ipAddress: String?,
        val gateway: String?,
        val needsLocation: Boolean,
    )

    private val _wifiLink = MutableStateFlow<WifiLink?>(null)
    val wifiLink: StateFlow<WifiLink?> = _wifiLink.asStateFlow()

    private fun readWifiLink(): WifiLink? {
        val snap = _snapshot.value ?: return null
        if (!snap.transports.contains("Wi-Fi")) return null
        val wm = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return null
        val info = runCatching { wm.connectionInfo }.getOrNull() ?: return null
        val locationGranted = context.checkSelfPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        // UNKNOWN_SSID ("<unknown ssid>") is API-30+; the literal is identical
        // and the hidden-SSID value was the same on earlier releases.
        val ssid = info.ssid
            ?.takeIf { locationGranted && it != "<unknown ssid>" }
            ?.removeSurrounding("\"")
        val bssid = info.bssid
            ?.takeIf { locationGranted && it != "02:00:00:00:00:00" }
        val freq = runCatching { info.frequency }.getOrNull()?.takeIf { it > 0 }
        val dhcp = runCatching { wm.dhcpInfo }.getOrNull()
        return WifiLink(
            ssid = ssid,
            bssid = bssid,
            // INVALID_RSSI sentinel is -127; literal avoids an API-33 NewApi hit.
            rssiDbm = info.rssi.takeIf { it != -127 },
            linkSpeedMbps = info.linkSpeed.takeIf { it > 0 },
            frequencyMhz = freq,
            band = freq?.let {
                when {
                    it in 2400..2500 -> "2.4 GHz"
                    it in 5000..5900 -> "5 GHz"
                    it in 5925..7125 -> "6 GHz"
                    else -> null
                }
            },
            ipAddress = dhcp?.ipAddress?.takeIf { it != 0 }?.let(::intToIpv4)
                ?: snap.addresses.firstOrNull(),
            gateway = dhcp?.gateway?.takeIf { it != 0 }?.let(::intToIpv4),
            needsLocation = !locationGranted,
        )
    }

    private fun intToIpv4(v: Int): String =
        "${v and 0xFF}.${(v shr 8) and 0xFF}.${(v shr 16) and 0xFF}.${(v shr 24) and 0xFF}"

    // ---- Interfaces + local service discovery ----

    data class InterfaceInfo(
        val name: String,
        val displayName: String,
        val mtu: Int?,
        val up: Boolean,
        val loopback: Boolean,
        val multicast: Boolean,
        val addresses: List<String>,
    )

    /** Every network interface Android exposes, whatever its state. */
    fun interfaces(): List<InterfaceInfo> = runCatching {
        NetworkInterface.getNetworkInterfaces()?.toList().orEmpty().map { ni ->
            InterfaceInfo(
                name = ni.name,
                displayName = ni.displayName ?: ni.name,
                mtu = runCatching { ni.mtu }.getOrNull()?.takeIf { it > 0 },
                up = runCatching { ni.isUp }.getOrDefault(false),
                loopback = runCatching { ni.isLoopback }.getOrDefault(false),
                multicast = runCatching { ni.supportsMulticast() }.getOrDefault(false),
                addresses = ni.inetAddresses.toList().mapNotNull { it.hostAddress },
            )
        }
    }.getOrDefault(emptyList())

    data class NsdFound(
        val name: String,
        val type: String,
        val host: String? = null,
        val port: Int? = null,
    )

    private val nsdManager =
        context.getSystemService(Context.NSD_SERVICE) as? NsdManager

    private val _nsdState = MutableStateFlow<ToolState<List<NsdFound>>>(ToolState.Idle)
    val nsdState: StateFlow<ToolState<List<NsdFound>>> = _nsdState.asStateFlow()

    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var multicastLock: WifiManager.MulticastLock? = null
    private var discoveryStop: kotlinx.coroutines.Job? = null

    /**
     * Bounded NSD discovery of one service type (e.g. `_ipp._tcp.`). Holds a
     * multicast lock for the window so mDNS traffic is delivered, then stops.
     */
    fun discoverServices(serviceType: String) {
        val type = serviceType.trim().removePrefix(".").let { if (it.endsWith(".")) it else "$it." }
        if (type.isBlank() || nsdManager == null) {
            _nsdState.value = ToolState.Failed("NSD unavailable or no service type")
            return
        }
        stopDiscovery()
        _nsdState.value = ToolState.Running
        val found = mutableListOf<NsdFound>()
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        multicastLock = wm?.createMulticastLock("droidkit-nsd")?.also {
            it.setReferenceCounted(true)
            runCatching { it.acquire() }
        }
        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) {}
            override fun onDiscoveryStopped(serviceType: String) {}
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                _nsdState.value = ToolState.Failed("Discovery failed (error $errorCode)")
                stopDiscovery()
            }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                found += NsdFound(serviceInfo.serviceName, serviceInfo.serviceType.orEmpty())
                _nsdState.value = ToolState.Done(found.toList())
            }
            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                found.removeAll { it.name == serviceInfo.serviceName }
                _nsdState.value = ToolState.Done(found.toList())
            }
        }
        discoveryListener = listener
        runCatching { nsdManager?.discoverServices(type, NsdManager.PROTOCOL_DNS_SD, listener) }
            .onFailure {
                _nsdState.value = ToolState.Failed(it.message ?: "Discovery failed")
                stopDiscovery()
            }
        discoveryStop = viewModelScope.launch {
            delay(10_000)
            // mark end of the bounded window without dropping found services
            if (_nsdState.value is ToolState.Running) {
                _nsdState.value = ToolState.Done(found.toList())
            }
            stopDiscoveryOnly()
        }
    }

    /** Resolves a discovered service to host/port (user-tapped). */
    fun resolveService(name: String, onResult: (NsdFound) -> Unit) {
        val mgr = nsdManager ?: return
        val info = NsdServiceInfo().apply { serviceName = name; serviceType = "" }
        // Resolution needs the full record; re-resolve by name+type snapshot.
        val current = (_nsdState.value as? ToolState.Done)?.result.orEmpty()
        val target = current.firstOrNull { it.name == name } ?: return
        info.serviceType = target.type
        mgr.resolveService(info, object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                onResult(target)
            }
            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                onResult(
                    target.copy(
                        host = serviceInfo.host?.hostAddress,
                        port = serviceInfo.port.takeIf { it > 0 },
                    ),
                )
            }
        })
    }

    private fun stopDiscoveryOnly() {
        discoveryListener?.let { runCatching { nsdManager?.stopServiceDiscovery(it) } }
        discoveryListener = null
        runCatching { multicastLock?.release() }
        multicastLock = null
    }

    fun stopDiscovery() {
        discoveryStop?.cancel()
        discoveryStop = null
        stopDiscoveryOnly()
    }

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
        viewModelScope.launch {
            _rootReady.value = runCatching {
                elevatedOps.isAvailable(ProviderKind.ROOT)
            }.getOrNull()
        }
    }

    private fun refreshSnapshot() {
        _snapshot.value = readSnapshot()
        _wifiLink.value = readWifiLink()
    }

    // ---- Wake-on-LAN ----

    /** Broadcasts a magic packet to wake a configured host on the local segment. */
    fun sendWol(macText: String, portText: String) {
        val mac = NetTools.parseMacAddress(macText)
        if (mac == null) {
            _wol.value = ToolState.Failed(
                "Invalid MAC — expected aa:bb:cc:dd:ee:ff",
            )
            return
        }
        val port = portText.trim().toIntOrNull()
        if (portText.isNotBlank() && (port == null || port !in 1..65535)) {
            _wol.value = ToolState.Failed("Invalid port (1–65535, blank = 9)")
            return
        }
        _wol.value = ToolState.Running
        viewModelScope.launch {
            val ok = NetTools.sendMagicPacket(mac, port ?: NetTools.WOL_DEFAULT_PORT)
            _wol.value = if (ok) {
                ToolState.Done(
                    "Magic packet broadcast for " +
                        mac.joinToString(":") { "%02X".format(it) } +
                        " on UDP port ${port ?: NetTools.WOL_DEFAULT_PORT}",
                )
            } else {
                ToolState.Failed("Broadcast send failed — check Wi-Fi is up")
            }
        }
    }

    // ---- LAN sweep ----

    /**
     * Time-boxed reachability sweep of the interface's own subnet (max /24).
     * Derived from the active link's IPv4/prefix — falls back to DHCP info.
     */
    fun startLanSweep() {
        val network = cm?.activeNetwork
        val link = network?.let { cm?.getLinkProperties(it) }
        var target = link?.linkAddresses?.firstOrNull {
            it.address is Inet4Address && it.prefixLength in 8..30
        }
        var prefix = target?.prefixLength
        var addr = target?.address
        if (addr == null) {
            // DHCP fallback: dhcpInfo exposes ip + netmask as little-endian ints.
            val wm = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val dhcp = runCatching { wm?.dhcpInfo }.getOrNull()
            val ip = dhcp?.ipAddress?.takeIf { it != 0 }
            val mask = dhcp?.netmask?.takeIf { it != 0 }
            if (ip != null && mask != null) {
                addr = Inet4Address.getByAddress(
                    byteArrayOf(
                        (ip and 0xFF).toByte(),
                        ((ip shr 8) and 0xFF).toByte(),
                        ((ip shr 16) and 0xFF).toByte(),
                        ((ip shr 24) and 0xFF).toByte(),
                    ),
                )
                prefix = Integer.bitCount(mask)
            }
        }
        val hosts = addr?.let { NetTools.subnetHosts(it, prefix ?: 24) }.orEmpty()
        if (addr == null || hosts.isEmpty()) {
            _sweep.value = ToolState.Failed(
                "No usable IPv4 subnet on the active network",
            )
            return
        }
        val subnetLabel = hosts.first().hostAddress
            ?.substringBeforeLast('.') + ".0/${(prefix ?: 24).coerceAtLeast(24)}"
        _sweep.value = ToolState.Running
        viewModelScope.launch {
            val start = System.nanoTime()
            val results = withTimeoutOrNull(SWEEP_TIME_BOX_MS) {
                NetTools.sweepSubnet(hosts)
            }.orEmpty()
            _sweep.value = ToolState.Done(
                SweepSummary(
                    subnet = subnetLabel,
                    hostsScanned = hosts.size,
                    results = results.sortedBy {
                        it.address.split('.').mapNotNull(String::toIntOrNull)
                            .fold(0) { acc, o -> acc * 256 + o }
                    },
                    elapsedMs = (System.nanoTime() - start) / 1_000_000,
                ),
            )
        }
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
        stopDiscovery()
        runCatching { cm?.unregisterNetworkCallback(callback) }
        super.onCleared()
    }

    companion object {
        private const val DNS_PROBE_HOST = "dns.google"
        private const val SWEEP_TIME_BOX_MS = 20_000L
    }
}

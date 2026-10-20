package io.github.droidkit.privilege.adb

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import io.github.droidkit.core.privileges.AdbEndpoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * mDNS discovery for Android's wireless-debugging services
 * (`_adb-tls-pairing._tcp` while the pairing dialog is open,
 * `_adb-tls-connect._tcp` while wireless debugging is on).
 *
 * Holds a Wi-Fi multicast lock while active — required on many devices for
 * mDNS packets to actually reach the app.
 */
class AdbDiscovery(context: Context) {

    private val appContext = context.applicationContext
    private val nsdManager = appContext.getSystemService(NsdManager::class.java)
    private val wifiManager = appContext.getSystemService(WifiManager::class.java)

    private val _endpoints = MutableSharedFlow<AdbEndpoint>(extraBufferCapacity = 8)
    val endpoints: SharedFlow<AdbEndpoint> = _endpoints.asSharedFlow()

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var multicastLock: WifiManager.MulticastLock? = null
    private var serviceNameFilter: String? = null

    /** Start discovering [serviceType]; optionally only emit [serviceName]. */
    fun start(serviceType: String, serviceName: String? = null) {
        stop()
        serviceNameFilter = serviceName
        multicastLock = wifiManager
            ?.createMulticastLock(LOCK_TAG)
            ?.apply {
                setReferenceCounted(true)
                acquire()
            }
        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) {}

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                val filter = serviceNameFilter
                if (filter != null && serviceInfo.serviceName != filter) return
                resolve(serviceInfo)
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {}

            override fun onDiscoveryStopped(serviceType: String) {}

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                _errors.tryEmit("mDNS discovery failed to start (error $errorCode)")
                stop()
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                _errors.tryEmit("mDNS discovery failed to stop (error $errorCode)")
            }
        }
        discoveryListener = listener
        nsdManager?.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
    }

    private fun resolve(serviceInfo: NsdServiceInfo) {
        nsdManager?.resolveService(serviceInfo, object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                _errors.tryEmit("mDNS resolve failed for ${serviceInfo.serviceName} (error $errorCode)")
            }

            override fun onServiceResolved(resolved: NsdServiceInfo) {
                val host = resolved.host?.hostAddress ?: return
                _endpoints.tryEmit(
                    AdbEndpoint(
                        host = host,
                        port = resolved.port,
                        serviceName = resolved.serviceName,
                    ),
                )
            }
        })
    }

    fun stop() {
        discoveryListener?.let { runCatching { nsdManager?.stopServiceDiscovery(it) } }
        discoveryListener = null
        multicastLock?.let { runCatching { if (it.isHeld) it.release() } }
        multicastLock = null
    }

    companion object {
        const val SERVICE_TYPE_PAIRING = "_adb-tls-pairing._tcp"
        const val SERVICE_TYPE_CONNECT = "_adb-tls-connect._tcp"
        private const val LOCK_TAG = "droidkit_adb_nsd"
    }
}

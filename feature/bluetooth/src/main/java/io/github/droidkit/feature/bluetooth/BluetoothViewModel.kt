package io.github.droidkit.feature.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    data class BondedDevice(
        val name: String?,
        val address: String?,
        val typeName: String,
        val bondStateName: String,
    )

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val adapter: BluetoothAdapter? get() = bluetoothManager?.adapter

    val classicSupported: Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    val leSupported: Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    val adapterPresent: Boolean get() = adapter != null

    private val _enabled = MutableStateFlow(adapter?.isEnabled == true)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _permissionVersion = MutableStateFlow(0)
    val permissionVersion: StateFlow<Int> = _permissionVersion.asStateFlow()

    private val _bonded = MutableStateFlow<List<BondedDevice>>(emptyList())
    val bonded: StateFlow<List<BondedDevice>> = _bonded.asStateFlow()

    // ---- BLE scan (user-initiated, time-bounded — never background) ----

    data class ScanResultEntry(
        val address: String,
        val name: String?,
        val rssi: Int,
        val lastSeenNanos: Long,
        /** Raw scan-record bytes for the advertisement decoder. */
        val rawBytes: ByteArray?,
        /** Last [RSSI_HISTORY_CAP] readings, oldest → newest. */
        val rssiHistory: List<Int>,
        val decoded: AdvertisementInfo? = null,
    ) {
        // ByteArray in a data class — explicit equality so diffing works.
        override fun equals(other: Any?): Boolean =
            other is ScanResultEntry && address == other.address &&
                name == other.name && rssi == other.rssi &&
                rssiHistory == other.rssiHistory &&
                rawBytes.contentEquals(other.rawBytes) && decoded == other.decoded

        override fun hashCode(): Int =
            31 * address.hashCode() + rssi +
                (rssiHistory.hashCode() * 7) + rawBytes.contentHashCode()
    }

    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning.asStateFlow()

    private val _scanResults = MutableStateFlow<List<ScanResultEntry>>(emptyList())
    val scanResults: StateFlow<List<ScanResultEntry>> = _scanResults.asStateFlow()

    private var scanTimeout: Job? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val raw = runCatching { result.scanRecord?.bytes }.getOrNull()
            val name = runCatching { result.scanRecord?.deviceName ?: result.device.name }.getOrNull()
            val decoded = runCatching { BleAdvertiser.decode(raw) }.getOrNull()
            val current = _scanResults.value.toMutableList()
            val idx = current.indexOfFirst { it.address == result.device.address }
            val history = ((current.getOrNull(idx)?.rssiHistory ?: emptyList()) + result.rssi)
                .takeLast(RSSI_HISTORY_CAP)
            val entry = ScanResultEntry(
                address = result.device.address,
                name = name,
                rssi = result.rssi,
                lastSeenNanos = System.nanoTime(),
                rawBytes = raw,
                rssiHistory = history,
                decoded = decoded,
            )
            if (idx >= 0) current[idx] = entry else current += entry
            _scanResults.value = current.sortedBy { it.address }
        }

        override fun onScanFailed(errorCode: Int) {
            stopScan()
        }
    }

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) refresh()
        }
    }

    init {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(stateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(stateReceiver, filter)
        }
        refresh()
    }

    /** BLUETOOTH_CONNECT is a runtime permission on API 31+; below, BLUETOOTH is install-time. */
    fun hasConnectPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    fun onPermissionResult() {
        _permissionVersion.value += 1
        refresh()
    }

    /** BLUETOOTH_SCAN on API 31+; ACCESS_FINE_LOCATION on API 28–30. */
    fun requiredScanPermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_SCAN
        } else {
            Manifest.permission.ACCESS_FINE_LOCATION
        }

    fun hasScanPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, requiredScanPermission()) ==
            PackageManager.PERMISSION_GRANTED

    /** Starts a bounded BLE scan (SCAN_WINDOW_MS) — requires adapter on + permission. */
    fun startScan() {
        if (_scanning.value) return
        if (!leSupported || adapter?.isEnabled != true || !hasScanPermission()) return
        _scanResults.value = emptyList()
        runCatching {
            adapter?.bluetoothLeScanner?.startScan(
                null,
                ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build(),
                scanCallback,
            )
        }.onSuccess {
            _scanning.value = true
            scanTimeout?.cancel()
            scanTimeout = viewModelScope.launch {
                delay(SCAN_WINDOW_MS)
                stopScan()
            }
        }
    }

    fun stopScan() {
        scanTimeout?.cancel()
        scanTimeout = null
        runCatching { adapter?.bluetoothLeScanner?.stopScan(scanCallback) }
        _scanning.value = false
    }

    fun refresh() {
        _enabled.value = adapter?.isEnabled == true
        _bonded.value = readBonded()
    }

    private fun readBonded(): List<BondedDevice> {
        if (!hasConnectPermission()) return emptyList()
        return runCatching {
            adapter?.bondedDevices.orEmpty().map { d ->
                BondedDevice(
                    name = runCatching { d.name }.getOrNull(),
                    address = runCatching { d.address }.getOrNull(),
                    typeName = BtNames.deviceType(runCatching { d.type }.getOrDefault(0)),
                    bondStateName = BtNames.bondState(runCatching { d.bondState }.getOrDefault(10)),
                )
            }.sortedBy { it.name ?: "" }
        }.getOrDefault(emptyList())
    }

    override fun onCleared() {
        stopScan()
        runCatching { context.unregisterReceiver(stateReceiver) }
        super.onCleared()
    }

    companion object {
        const val SCAN_WINDOW_MS = 15_000L
        const val RSSI_HISTORY_CAP = 24
    }
}

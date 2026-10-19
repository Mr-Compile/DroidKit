package io.github.droidkit.feature.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GattCharacteristicInfo(
    val uuid: String,
    val properties: List<String>,
    val descriptors: List<String>,
    val readValueHex: String? = null,
    val readError: String? = null,
)

data class GattServiceInfo(
    val uuid: String,
    val isPrimary: Boolean,
    val characteristics: List<GattCharacteristicInfo>,
)

/**
 * Read-only GATT explorer. Connects to a discovered device on explicit user
 * action, discovers services, and reads characteristics only where the
 * characteristic advertises PROPERTY_READ. Never writes, never subscribes.
 */
@HiltViewModel
class GattExplorerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedState: SavedStateHandle,
) : ViewModel() {

    sealed interface State {
        data object Connecting : State
        data class Connected(val deviceName: String?, val services: List<GattServiceInfo>) : State
        data class Failed(val reason: String) : State
    }

    val address: String = checkNotNull(savedState["address"])

    private val _state = MutableStateFlow<State>(State.Connecting)
    val state: StateFlow<State> = _state.asStateFlow()

    private var gatt: BluetoothGatt? = null

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            when {
                status != BluetoothGatt.GATT_SUCCESS -> {
                    _state.value = State.Failed("Connection failed (status $status)")
                    runCatching { g.close() }
                }
                newState == BluetoothProfile.STATE_CONNECTED ->
                    runCatching { g.discoverServices() }
                        .onFailure { _state.value = State.Failed("Service discovery failed: ${it.message}") }
                newState == BluetoothProfile.STATE_DISCONNECTED ->
                    _state.value = State.Failed("Device disconnected")
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _state.value = State.Failed("Discovery failed (status $status)")
                return
            }
            _state.value = State.Connected(
                deviceName = runCatching { g.device.name }.getOrNull(),
                services = g.services.map { svc ->
                    GattServiceInfo(
                        uuid = svc.uuid.toString(),
                        isPrimary = svc.type == BluetoothGattService.SERVICE_TYPE_PRIMARY,
                        characteristics = svc.characteristics.map { it.toInfo() },
                    )
                },
            )
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            applyReadResult(characteristic, characteristic.value, status)
        }

        // API 33+ typed overload
        override fun onCharacteristicRead(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int,
        ) {
            applyReadResult(characteristic, value, status)
        }
    }

    private fun applyReadResult(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray?,
        status: Int,
    ) {
        val hex = value?.joinToString(" ") { "%02X".format(it) }
        val current = _state.value as? State.Connected ?: return
        _state.value = current.copy(
            services = current.services.map { svc ->
                svc.copy(
                    characteristics = svc.characteristics.map { c ->
                        if (c.uuid == characteristic.uuid.toString()) {
                            c.copy(
                                readValueHex = if (status == BluetoothGatt.GATT_SUCCESS) hex ?: "(empty)" else null,
                                readError = if (status == BluetoothGatt.GATT_SUCCESS) null else "read failed ($status)",
                            )
                        } else c
                    },
                )
            },
        )
    }

    fun hasConnectPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    /** Connects on explicit user invocation only — never automatic. */
    @SuppressLint("MissingPermission")
    fun connect() {
        if (!hasConnectPermission()) {
            _state.value = State.Failed("BLUETOOTH_CONNECT permission required")
            return
        }
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val device = runCatching { manager?.adapter?.getRemoteDevice(address) }.getOrNull()
        if (device == null) {
            _state.value = State.Failed("Device address unavailable")
            return
        }
        _state.value = State.Connecting
        gatt = runCatching {
            device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
        }.getOrNull()
        if (gatt == null) _state.value = State.Failed("connectGatt returned null")
    }

    /** Reads one characteristic — only when it advertises PROPERTY_READ. */
    @SuppressLint("MissingPermission")
    fun readCharacteristic(serviceUuid: String, characteristicUuid: String) {
        val g = gatt ?: return
        if (!hasConnectPermission()) return
        val svc = runCatching {
            g.getService(java.util.UUID.fromString(serviceUuid))
        }.getOrNull() ?: return
        val ch = runCatching {
            svc.getCharacteristic(java.util.UUID.fromString(characteristicUuid))
        }.getOrNull() ?: return
        if (ch.properties and BluetoothGattCharacteristic.PROPERTY_READ == 0) return
        viewModelScope.launch {
            delay(50) // let the connection settle after discovery
            runCatching { g.readCharacteristic(ch) }
        }
    }

    fun disconnect() {
        runCatching { gatt?.disconnect() }
        runCatching { gatt?.close() }
        gatt = null
    }

    override fun onCleared() {
        disconnect()
        super.onCleared()
    }
}

private fun BluetoothGattCharacteristic.toInfo(): GattCharacteristicInfo {
    val props = buildList {
        if (properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) add("read")
        if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) add("write")
        if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) add("write-no-rsp")
        if (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) add("notify")
        if (properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) add("indicate")
        if (properties and BluetoothGattCharacteristic.PROPERTY_BROADCAST != 0) add("broadcast")
        if (properties and BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE != 0) add("signed-write")
        if (properties and BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS != 0) add("extended")
    }
    return GattCharacteristicInfo(
        uuid = uuid.toString(),
        properties = props,
        descriptors = descriptors.map { it.uuid.toString() },
    )
}

package io.github.droidkit.feature.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.droidkit.core.common.DLog
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class UsbViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager

    val hostSupported: Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)

    private val _devices = MutableStateFlow<List<UsbDevice>>(emptyList())
    val devices: StateFlow<List<UsbDevice>> = _devices.asStateFlow()

    /** One attach/detach observation, kept for the session only. */
    data class UsbEvent(
        val attached: Boolean,
        val deviceName: String,
        val productName: String?,
        val vidPid: String,
        val atEpochMs: Long,
    )

    /**
     * Session attach/detach log — populated from USB broadcasts while this
     * screen's ViewModel is alive. Not persisted; capped so a bus fault
     * storm can't grow unboundedly.
     */
    private val _events = MutableStateFlow<List<UsbEvent>>(emptyList())
    val events: StateFlow<List<UsbEvent>> = _events.asStateFlow()

    /** Bumped when a permission response arrives so the UI re-reads [hasPermission]. */
    private val _permissionVersion = MutableStateFlow(0)
    val permissionVersion: StateFlow<Int> = _permissionVersion.asStateFlow()

    /** Devices a permission request is currently in-flight for (deviceName). */
    private val _permissionPending = MutableStateFlow<Set<String>>(emptySet())
    val permissionPending: StateFlow<Set<String>> = _permissionPending.asStateFlow()

    private val attachReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            val device = intent.parcelableDevice()
            val attached = intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED
            if (device != null) {
                _events.value = (_events.value + UsbEvent(
                    attached = attached,
                    deviceName = device.deviceName,
                    productName = device.productName,
                    vidPid = UsbDescriptors.hex4(device.vendorId) + ":" +
                        UsbDescriptors.hex4(device.productId),
                    atEpochMs = System.currentTimeMillis(),
                )).takeLast(MAX_EVENTS)
            }
            DLog.d(TAG, "USB ${intent.action}")
            refresh()
        }
    }

    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            if (intent.action != ACTION_USB_PERMISSION) return
            val device = intent.parcelableDevice()
            _permissionPending.value = _permissionPending.value - (device?.deviceName ?: "")
            _permissionVersion.value += 1
            DLog.d(TAG, "USB permission granted=${intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)}")
        }
    }

    init {
        register(
            attachReceiver,
            IntentFilter().apply {
                addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            },
        )
        register(permissionReceiver, IntentFilter(ACTION_USB_PERMISSION))
        refresh()
    }

    fun refresh() {
        _devices.value = runCatching { usbManager?.deviceList?.values?.sortedBy { it.deviceName } }
            .getOrNull().orEmpty()
    }

    fun device(name: String): UsbDevice? = _devices.value.firstOrNull { it.deviceName == name }

    fun hasPermission(device: UsbDevice): Boolean =
        runCatching { usbManager?.hasPermission(device) == true }.getOrDefault(false)

    fun requestPermission(device: UsbDevice) {
        val manager = usbManager ?: return
        if (hasPermission(device)) return
        val mutableFlag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        val pi = PendingIntent.getBroadcast(
            context,
            device.deviceName.hashCode(),
            Intent(ACTION_USB_PERMISSION).setPackage(context.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or mutableFlag,
        ) ?: return
        _permissionPending.value = _permissionPending.value + device.deviceName
        runCatching { manager.requestPermission(device, pi) }
            .onFailure { _permissionPending.value = _permissionPending.value - device.deviceName }
    }

    /** Serial throws SecurityException without permission; only call when granted. */
    fun serialOrNull(device: UsbDevice): String? =
        if (hasPermission(device)) runCatching { device.serialNumber }.getOrNull() else null

    private fun register(receiver: BroadcastReceiver, filter: IntentFilter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }
    }

    private fun Intent.parcelableDevice(): UsbDevice? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }

    override fun onCleared() {
        runCatching { context.unregisterReceiver(attachReceiver) }
        runCatching { context.unregisterReceiver(permissionReceiver) }
        super.onCleared()
    }

    companion object {
        private const val TAG = "UsbViewModel"
        private const val MAX_EVENTS = 100
        const val ACTION_USB_PERMISSION = "io.github.droidkit.feature.usb.USB_PERMISSION"
    }
}

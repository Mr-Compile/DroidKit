package io.github.droidkit.feature.adb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.privileges.AdbPairingState
import io.github.droidkit.core.privileges.AdbQrPayload
import io.github.droidkit.core.privileges.AdbSessionSetup
import io.github.droidkit.core.privileges.ElevatedAction
import io.github.droidkit.core.privileges.ElevatedOps
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AdbViewModel @Inject constructor(
    private val adbSetup: AdbSessionSetup,
    private val elevatedOps: ElevatedOps,
) : ViewModel() {

    sealed interface Event {
        data class Message(val text: String) : Event
    }

    private val _session = MutableStateFlow<String?>(adbSetup.sessionSummary())
    val session: StateFlow<String?> = _session.asStateFlow()

    val pairingState: StateFlow<AdbPairingState> = adbSetup.pairingState

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _props = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val props: StateFlow<List<Pair<String, String>>> = _props.asStateFlow()

    private val _log = MutableStateFlow("")
    val log: StateFlow<String> = _log.asStateFlow()

    /** QR payload currently being advertised for another device to scan. */
    private val _qrPayload = MutableStateFlow<AdbQrPayload?>(null)
    val qrPayload: StateFlow<AdbQrPayload?> = _qrPayload.asStateFlow()

    val pairingCode = MutableStateFlow("")
    val pairingHost = MutableStateFlow("")
    val pairingPort = MutableStateFlow("")
    val connectHost = MutableStateFlow("")
    val connectPort = MutableStateFlow("5555")
    val logLines = MutableStateFlow("200")
    val useTls = MutableStateFlow(true)

    fun refreshSession() {
        _session.value = adbSetup.sessionSummary()
    }

    // ---------- guided flow -------------------------------------------------

    fun startPairing() {
        adbSetup.startPairingSearch()
            .onFailure { emit("Could not start discovery: ${it.message}") }
    }

    fun stopPairing() {
        adbSetup.stopPairingSearch()
        _qrPayload.value = null
    }

    fun submitPairingCode() {
        val code = pairingCode.value.trim()
        if (code.isEmpty()) {
            emit("Enter the 6-digit code from the pairing dialog.")
            return
        }
        viewModelScope.launch {
            _busy.value = true
            adbSetup.pairWithDiscovered(code)
                .onSuccess { emit("Pairing succeeded: $it") }
                .onFailure { emit("Pairing failed: ${it.message}") }
            refreshSession()
            _busy.value = false
        }
    }

    /** Connect to the already-paired device via discovered connect endpoint. */
    fun autoConnect() {
        viewModelScope.launch {
            _busy.value = true
            adbSetup.connectDiscovered()
                .onSuccess { emit("Connected — $it") }
                .onFailure { emit("Connect failed: ${it.message}") }
            refreshSession()
            _busy.value = false
        }
    }

    /** Display a QR for another device's "Pair device with QR code" scanner. */
    fun showQrForOtherDevice() {
        val payload = AdbQrPayload.generate()
        adbSetup.startQrPairing(payload)
            .onSuccess { _qrPayload.value = payload }
            .onFailure { emit("Could not start QR pairing: ${it.message}") }
    }

    /** Pair using a QR payload decoded from an imported screenshot. */
    fun pairWithQrText(text: String) {
        val payload = AdbQrPayload.parse(text)
        if (payload == null) {
            emit("That image doesn't contain an ADB pairing QR code.")
            return
        }
        adbSetup.startQrPairing(payload)
            .onFailure { emit("Could not start QR pairing: ${it.message}") }
    }

    // ---------- manual flow (fallback) ---------------------------------------

    fun pair() {
        val host = pairingHost.value.trim()
        val port = pairingPort.value.trim().toIntOrNull()
        val code = pairingCode.value.trim()
        if (host.isEmpty() || port == null || code.isEmpty()) {
            emit("Enter host, pairing port, and the code shown in Wireless debugging.")
            return
        }
        viewModelScope.launch {
            _busy.value = true
            adbSetup.pair(host, port, code)
                .onSuccess { emit("Pairing succeeded: $it") }
                .onFailure { emit("Pairing failed: ${it.message}") }
            _busy.value = false
        }
    }

    fun connect() {
        val host = connectHost.value.trim().ifEmpty { pairingHost.value.trim() }
        val port = connectPort.value.trim().toIntOrNull()
        if (host.isEmpty() || port == null) {
            emit("Enter host and connect port (shown on the Wireless debugging screen).")
            return
        }
        viewModelScope.launch {
            _busy.value = true
            adbSetup.connect(host, port, useTls.value)
                .onSuccess { emit("Connected — $it") }
                .onFailure { emit("Connect failed: ${it.message}") }
            refreshSession()
            _busy.value = false
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            adbSetup.disconnect()
            refreshSession()
            emit("Disconnected.")
        }
    }

    fun loadDeviceProps() {
        viewModelScope.launch {
            val keys = listOf(
                "ro.product.model" to "Model",
                "ro.product.manufacturer" to "Manufacturer",
                "ro.build.version.release" to "Android",
                "ro.build.version.sdk" to "SDK",
                "ro.serialno" to "Serial",
                "ro.build.fingerprint" to "Fingerprint",
            )
            val out = mutableListOf<Pair<String, String>>()
            for ((key, label) in keys) {
                val r = elevatedOps.execute(ElevatedAction.GetProp(key))
                if (r.ok) out += label to r.stdout.trim().ifEmpty { "—" }
            }
            _props.value = out
            if (out.isEmpty()) emit("No session — connect first.")
        }
    }

    fun readLog() {
        viewModelScope.launch {
            val lines = logLines.value.trim().toIntOrNull()?.coerceIn(1, 5000) ?: 200
            val r = elevatedOps.execute(ElevatedAction.ReadLog(lines))
            _log.value = if (r.ok) {
                r.stdout.ifEmpty { "(log empty)" }
            } else {
                "logcat failed (exit ${r.exitCode}): ${r.stderr.ifEmpty { r.stdout }.take(300)}"
            }
        }
    }

    fun reboot() {
        viewModelScope.launch {
            val r = elevatedOps.execute(ElevatedAction.Reboot)
            emit(if (r.ok) "Reboot command sent." else "Reboot failed: ${r.stderr.ifEmpty { r.stdout }.take(200)}")
        }
    }

    fun consumeEvent(event: Event) {
        _events.value = _events.value - event
    }

    private fun emit(text: String) {
        _events.value = _events.value + Event.Message(text)
    }
}

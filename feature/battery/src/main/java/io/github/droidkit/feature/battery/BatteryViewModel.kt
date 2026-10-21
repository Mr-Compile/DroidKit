package io.github.droidkit.feature.battery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.privileges.ElevatedAction
import io.github.droidkit.core.privileges.ElevatedOps
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.system.BatteryStatus
import io.github.droidkit.core.system.DeviceSystemProbe
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class BatteryViewModel @Inject constructor(
    private val probe: DeviceSystemProbe,
    private val elevatedOps: ElevatedOps,
    private val healthStore: BatteryHealthStore,
) : ViewModel() {

    private val _status = MutableStateFlow(probe.batteryStatus)
    val status: StateFlow<BatteryStatus> = _status

    val estimate: StateFlow<CapacityEstimate> = healthStore.estimate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CapacityEstimate(0, null, null))

    private val _elevatedProvider = MutableStateFlow<ProviderKind?>(null)
    val elevatedProvider: StateFlow<ProviderKind?> = _elevatedProvider

    private val _measuring = MutableStateFlow(false)
    val measuring: StateFlow<Boolean> = _measuring

    /**
     * Rolling mA history sampled while the screen is visible (2 s cadence,
     * last 120 samples). Deliberately in-memory only — the persistent
     * estimator already runs at a much slower cadence.
     */
    private val _currentHistoryMa = MutableStateFlow<List<Double>>(emptyList())
    val currentHistoryMa: StateFlow<List<Double>> = _currentHistoryMa

    private var samplingJob: kotlinx.coroutines.Job? = null

    fun setLiveSampling(active: Boolean) {
        if (active && samplingJob == null) {
            samplingJob = viewModelScope.launch {
                while (isActive) {
                    probe.batteryStatus.currentNowUa?.let { ua ->
                        _currentHistoryMa.value =
                            (_currentHistoryMa.value + ua / 1000.0).takeLast(120)
                    }
                    kotlinx.coroutines.delay(2_000)
                }
            }
        } else if (!active) {
            samplingJob?.cancel()
            samplingJob = null
        }
    }

    /** null = not measured yet; reading fields null = node not exposed. */
    private val _measured = MutableStateFlow<FuelGaugeReading?>(null)
    val measured: StateFlow<FuelGaugeReading?> = _measured

    /** The elevated command itself failed (provider error, timeout, …). */
    private val _measureFailed = MutableStateFlow(false)
    val measureFailed: StateFlow<Boolean> = _measureFailed

    /** Command ran fine but the kernel exposes no capacity nodes. */
    private val _measureUnsupported = MutableStateFlow(false)
    val measureUnsupported: StateFlow<Boolean> = _measureUnsupported

    fun refresh() {
        val s = probe.batteryStatus
        _status.value = s
        viewModelScope.launch {
            healthStore.recordSample(s.levelPct, s.chargeCounterUa)
            _elevatedProvider.value = elevatedOps.activeProvider()
        }
    }

    /**
     * Read the kernel fuel gauge through whichever elevated provider is
     * available. A device without the sysfs nodes returns "not exposed" —
     * that is honest hardware reality, not a failure.
     */
    fun measureHealth() {
        if (_measuring.value) return
        viewModelScope.launch {
            _measuring.value = true
            _measureFailed.value = false
            _measureUnsupported.value = false
            val result = elevatedOps.execute(ElevatedAction.ReadPowerSupply)
            _measuring.value = false
            if (!result.ok) {
                _measureFailed.value = true
                return@launch
            }
            _measured.value = parsePowerSupplyDump(result.stdout)
            _measureUnsupported.value = _measured.value == null
        }
    }

    fun setDesignCapacity(mah: Int?) {
        viewModelScope.launch { healthStore.setDesignCapacity(mah) }
    }

    fun resetEstimate() {
        viewModelScope.launch { healthStore.reset() }
    }
}

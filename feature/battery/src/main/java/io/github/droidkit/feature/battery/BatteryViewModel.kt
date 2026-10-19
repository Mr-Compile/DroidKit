package io.github.droidkit.feature.battery

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.system.BatteryStatus
import io.github.droidkit.core.system.DeviceSystemProbe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class BatteryViewModel @Inject constructor(
    private val probe: DeviceSystemProbe,
) : ViewModel() {

    private val _status = MutableStateFlow(probe.batteryStatus)
    val status: StateFlow<BatteryStatus> = _status

    fun refresh() {
        _status.value = probe.batteryStatus
    }
}

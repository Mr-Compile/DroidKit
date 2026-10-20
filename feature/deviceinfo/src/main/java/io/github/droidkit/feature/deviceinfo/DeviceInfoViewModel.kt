package io.github.droidkit.feature.deviceinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.system.DeviceSystemProbe
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DeviceInfoViewModel @Inject constructor(
    val probe: DeviceSystemProbe,
    capabilityManager: CapabilityManager,
    private val deepProbe: DeviceDeepProbe,
) : ViewModel() {
    /** Reactive provider map — re-emits on provider status changes. */
    val providerStatuses: StateFlow<Map<ProviderKind, ProviderStatus>> = capabilityManager.statuses

    private val _elevatedProvider = MutableStateFlow<ProviderKind?>(null)
    val elevatedProvider: StateFlow<ProviderKind?> = _elevatedProvider

    private val _advanced = MutableStateFlow<AdvancedDetail?>(null)
    val advanced: StateFlow<AdvancedDetail?> = _advanced

    private val _advancedLoading = MutableStateFlow(false)
    val advancedLoading: StateFlow<Boolean> = _advancedLoading

    init {
        viewModelScope.launch { _elevatedProvider.value = deepProbe.activeProvider() }
    }

    /** One-shot deep snapshot — re-tappable to refresh. */
    fun loadAdvancedDetail() {
        if (_advancedLoading.value) return
        viewModelScope.launch {
            _advancedLoading.value = true
            try {
                _advanced.value = deepProbe.load()
                _elevatedProvider.value = deepProbe.activeProvider()
            } finally {
                _advancedLoading.value = false
            }
        }
    }
}

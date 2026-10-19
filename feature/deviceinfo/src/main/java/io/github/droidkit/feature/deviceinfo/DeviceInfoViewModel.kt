package io.github.droidkit.feature.deviceinfo

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.system.DeviceSystemProbe
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class DeviceInfoViewModel @Inject constructor(
    val probe: DeviceSystemProbe,
    capabilityManager: CapabilityManager,
) : ViewModel() {
    /** Reactive provider map — re-emits on provider status changes. */
    val providerStatuses: StateFlow<Map<ProviderKind, ProviderStatus>> = capabilityManager.statuses
}

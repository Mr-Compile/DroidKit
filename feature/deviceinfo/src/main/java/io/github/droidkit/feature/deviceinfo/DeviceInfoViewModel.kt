package io.github.droidkit.feature.deviceinfo

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.system.DeviceSystemProbe
import javax.inject.Inject

@HiltViewModel
class DeviceInfoViewModel @Inject constructor(
    val probe: DeviceSystemProbe,
    private val capabilityManager: CapabilityManager,
) : ViewModel() {
    fun providerStatuses(): Map<ProviderKind, ProviderStatus> =
        capabilityManager.providerStatuses()
}

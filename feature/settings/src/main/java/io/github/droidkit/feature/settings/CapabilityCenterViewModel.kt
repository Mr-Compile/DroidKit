package io.github.droidkit.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.privileges.AdbSessionSetup
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.ElevatedOps
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Capability Center — live view of every registered capability provider:
 * status, served capabilities, limitations and the setup/recovery action.
 * Provider selection stays inside [CapabilityManager]; this only reflects it.
 */
@HiltViewModel
class CapabilityCenterViewModel @Inject constructor(
    private val capabilityManager: CapabilityManager,
    private val elevatedOps: ElevatedOps,
    private val adbSetup: AdbSessionSetup,
) : ViewModel() {

    val providerStatuses: StateFlow<Map<ProviderKind, ProviderStatus>> =
        capabilityManager.statuses

    /** Static map — provider capabilities don't change at runtime. */
    val providerCapabilities: Map<ProviderKind, Set<Capability>> =
        capabilityManager.providerCapabilities()

    val adbPairingState = adbSetup.pairingState
    val adbAutoConnect = adbSetup.autoConnectEnabled

    private val _grantBusy = MutableStateFlow(false)
    val grantBusy: StateFlow<Boolean> = _grantBusy.asStateFlow()

    fun adbSessionSummary(): String? = adbSetup.sessionSummary()

    fun refresh() {
        capabilityManager.refreshStatuses()
    }

    fun requestGrant(kind: ProviderKind) {
        viewModelScope.launch {
            _grantBusy.value = true
            try {
                elevatedOps.requestGrant(kind)
            } finally {
                _grantBusy.value = false
                capabilityManager.refreshStatuses()
            }
        }
    }
}

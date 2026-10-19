package io.github.droidkit.core.privileges

import io.github.droidkit.core.common.Capability
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

enum class ProviderKind { STANDARD, SHIZUKU, ADB, ROOT }

sealed interface ProviderStatus {
    data object Ready : ProviderStatus
    data class NeedsSetup(val instructions: String) : ProviderStatus
    data class Unavailable(val reason: String) : ProviderStatus
}

interface CapabilityProvider {
    val kind: ProviderKind
    val status: ProviderStatus
    val capabilities: Set<Capability>
}

/**
 * Lightweight "provider status may have changed" signal. Providers call
 * [notifyChanged] after grant/setup/connect/disconnect; [CapabilityManager]
 * folds it into its reactive `statuses` flow so UI updates without restarts.
 */
object ProviderStatusBus {
    // replay=1: a notify fired before a subscriber attaches is still delivered once.
    private val _changes = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 16)
    val changes: SharedFlow<Unit> = _changes

    fun notifyChanged() {
        _changes.tryEmit(Unit)
    }
}

package io.github.droidkit.core.privileges

import io.github.droidkit.core.common.Capability

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

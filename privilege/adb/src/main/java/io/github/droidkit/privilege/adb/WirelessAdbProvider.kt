package io.github.droidkit.privilege.adb

import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.privileges.CapabilityProvider
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus

class WirelessAdbProvider : CapabilityProvider {
    override val kind = ProviderKind.ADB
    override val status = ProviderStatus.Unavailable("Coming in Phase 9 — wireless ADB provider not implemented yet")
    override val capabilities = setOf(Capability.ADB_SHELL)
}

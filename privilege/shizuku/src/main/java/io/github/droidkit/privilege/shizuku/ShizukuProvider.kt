package io.github.droidkit.privilege.shizuku

import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.privileges.CapabilityProvider
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus

class ShizukuProvider : CapabilityProvider {
    override val kind = ProviderKind.SHIZUKU
    override val status = ProviderStatus.Unavailable("Coming in Phase 7 — Shizuku provider not implemented yet")
    override val capabilities = setOf(Capability.SHIZUKU_SERVICE)
}

package io.github.droidkit.privilege.root

import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.privileges.CapabilityProvider
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus

class RootProvider : CapabilityProvider {
    override val kind = ProviderKind.ROOT
    override val status = ProviderStatus.Unavailable("Postponed — optional future provider")
    override val capabilities = setOf(Capability.ROOT)
}

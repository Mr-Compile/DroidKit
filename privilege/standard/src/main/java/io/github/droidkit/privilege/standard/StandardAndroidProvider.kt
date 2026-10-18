package io.github.droidkit.privilege.standard

import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.privileges.CapabilityProvider
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus

class StandardAndroidProvider : CapabilityProvider {
    override val kind = ProviderKind.STANDARD
    override val status = ProviderStatus.Ready
    override val capabilities = setOf(Capability.STANDARD_API, Capability.RUNTIME_PERMISSION)
}

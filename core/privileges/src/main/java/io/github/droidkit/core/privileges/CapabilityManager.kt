package io.github.droidkit.core.privileges

import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.permissions.PermissionChecker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapabilityManager @Inject constructor(
    providers: Set<@JvmSuppressWildcards CapabilityProvider>,
    private val sdkInt: Int,
    private val hasFeature: (String) -> Boolean,
    private val permissionChecker: PermissionChecker,
) {
    private val providersByKind: Map<ProviderKind, CapabilityProvider> =
        providers.associateBy { it.kind }
    private val providersByCapability: Map<Capability, List<CapabilityProvider>> =
        providers.flatMap { p -> p.capabilities.map { c -> c to p } }
            .groupBy({ it.first }, { it.second })

    fun providerStatuses(): Map<ProviderKind, ProviderStatus> =
        providersByKind.mapValues { it.value.status }

    fun resolve(tool: Tool): ToolResolution {
        val reasons = mutableListOf<BlockedReason>()

        tool.minSdk?.let { required ->
            if (sdkInt < required) reasons += BlockedReason.RequiresApi(required)
        }
        tool.requiredFeatures.forEach { feature ->
            if (!hasFeature(feature)) reasons += BlockedReason.MissingHardware(feature)
        }
        tool.requiredPermissions.forEach { permission ->
            if (!permissionChecker.isGranted(permission)) {
                reasons += BlockedReason.MissingPermission(permission)
            }
        }
        tool.requiredCapabilities.forEach { capability ->
            val candidates = providersByCapability[capability].orEmpty()
            if (candidates.none { it.status == ProviderStatus.Ready }) {
                val best = candidates.minByOrNull { statusRank(it.status) }
                reasons += BlockedReason.NeedsProvider(
                    kind = best?.kind ?: providerKindFor(capability),
                    status = best?.status ?: ProviderStatus.Unavailable("No provider registered"),
                )
            }
        }

        return if (reasons.isEmpty()) ToolResolution.Ready else ToolResolution.Blocked(reasons)
    }

    private fun statusRank(status: ProviderStatus): Int = when (status) {
        ProviderStatus.Ready -> 0
        is ProviderStatus.NeedsSetup -> 1
        is ProviderStatus.Unavailable -> 2
    }

    private fun providerKindFor(capability: Capability): ProviderKind = when (capability) {
        Capability.STANDARD_API, Capability.RUNTIME_PERMISSION -> ProviderKind.STANDARD
        Capability.SHIZUKU_SERVICE -> ProviderKind.SHIZUKU
        Capability.ADB_SHELL -> ProviderKind.ADB
        Capability.ROOT -> ProviderKind.ROOT
    }
}

package io.github.droidkit.core.privileges

sealed interface BlockedReason {
    data class MissingPermission(val permission: String) : BlockedReason
    data class MissingHardware(val feature: String) : BlockedReason
    data class RequiresApi(val minSdk: Int) : BlockedReason
    data class NeedsProvider(val kind: ProviderKind, val status: ProviderStatus) : BlockedReason
}

sealed interface ToolResolution {
    data object Ready : ToolResolution
    data class Blocked(val reasons: List<BlockedReason>) : ToolResolution
}

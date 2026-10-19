package io.github.droidkit.core.privileges

import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.permissions.PermissionChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityManagerTest {

    private val standardProvider = object : CapabilityProvider {
        override val kind = ProviderKind.STANDARD
        override val status = ProviderStatus.Ready
        override val capabilities = setOf(Capability.STANDARD_API, Capability.RUNTIME_PERMISSION)
    }

    private val shizukuStub = object : CapabilityProvider {
        override val kind = ProviderKind.SHIZUKU
        override val status = ProviderStatus.Unavailable("Coming in Phase 7")
        override val capabilities = setOf(Capability.SHIZUKU_SERVICE)
    }

    private fun manager(
        permissions: Set<String> = emptySet(),
        features: Set<String> = emptySet(),
        sdk: Int = 36,
    ) = CapabilityManager(
        providers = setOf(standardProvider, shizukuStub),
        sdkInt = sdk,
        hasFeature = { it in features },
        permissionChecker = PermissionChecker { it in permissions },
    )

    @Test
    fun `standard tool with no requirements is ready`() {
        val tool = Tool("t", "T", "", ToolCategory.DEVICE, "r")
        assertEquals(ToolResolution.Ready, manager().resolve(tool))
    }

    @Test
    fun `missing permission produces blocked reason`() {
        val tool = Tool("t", "T", "", ToolCategory.DEVICE, "r",
            requiredCapabilities = setOf(Capability.RUNTIME_PERMISSION),
            requiredPermissions = listOf("android.permission.CAMERA"))
        val res = manager().resolve(tool)
        assertTrue(res is ToolResolution.Blocked)
        assertTrue((res as ToolResolution.Blocked).reasons.any { it is BlockedReason.MissingPermission })
    }

    @Test
    fun `granted permission is ready`() {
        val tool = Tool("t", "T", "", ToolCategory.DEVICE, "r",
            requiredPermissions = listOf("android.permission.CAMERA"))
        assertEquals(ToolResolution.Ready, manager(permissions = setOf("android.permission.CAMERA")).resolve(tool))
    }

    @Test
    fun `missing hardware feature blocks`() {
        val tool = Tool("t", "T", "", ToolCategory.HARDWARE, "r",
            requiredFeatures = setOf("android.hardware.nfc"))
        val res = manager().resolve(tool)
        assertTrue((res as ToolResolution.Blocked).reasons.any { it is BlockedReason.MissingHardware })
    }

    @Test
    fun `minSdk gate blocks on old devices`() {
        val tool = Tool("t", "T", "", ToolCategory.DEVICE, "r", minSdk = 35)
        val res = manager(sdk = 28).resolve(tool)
        assertTrue((res as ToolResolution.Blocked).reasons.any { it is BlockedReason.RequiresApi })
    }

    @Test
    fun `shizuku capability resolves to needs-provider with stub status`() {
        val tool = Tool("t", "T", "", ToolCategory.APPS, "r",
            requiredCapabilities = setOf(Capability.SHIZUKU_SERVICE))
        val res = manager().resolve(tool)
        val reason = (res as ToolResolution.Blocked).reasons.filterIsInstance<BlockedReason.NeedsProvider>().single()
        assertEquals(ProviderKind.SHIZUKU, reason.kind)
        assertTrue(reason.status is ProviderStatus.Unavailable)
    }

    @Test
    fun `capability with no provider reports unavailability`() {
        val tool = Tool("t", "T", "", ToolCategory.DEVELOPER, "r",
            requiredCapabilities = setOf(Capability.ROOT))
        val res = CapabilityManager(
            providers = setOf(standardProvider),
            sdkInt = 36,
            hasFeature = { true },
            permissionChecker = PermissionChecker { true },
        ).resolve(tool)
        val reason = (res as ToolResolution.Blocked).reasons.filterIsInstance<BlockedReason.NeedsProvider>().single()
        assertTrue(reason.status is ProviderStatus.Unavailable)
    }

    @Test
    fun `providerStatuses maps all providers`() {
        val statuses = manager().providerStatuses()
        assertEquals(ProviderStatus.Ready, statuses[ProviderKind.STANDARD])
        assertTrue(statuses[ProviderKind.SHIZUKU] is ProviderStatus.Unavailable)
    }

    @Test
    fun `statuses flow reflects provider change after notifyChanged`() {
        val flipper = object : CapabilityProvider {
            var current: ProviderStatus = ProviderStatus.NeedsSetup("set up")
            override val kind = ProviderKind.SHIZUKU
            override val status get() = current
            override val capabilities = setOf(Capability.SHIZUKU_SERVICE)
        }
        val m = CapabilityManager(
            providers = setOf(standardProvider, flipper),
            sdkInt = 36,
            hasFeature = { true },
            permissionChecker = PermissionChecker { true },
        )
        assertTrue(m.statuses.value[ProviderKind.SHIZUKU] is ProviderStatus.NeedsSetup)

        flipper.current = ProviderStatus.Ready
        ProviderStatusBus.notifyChanged()

        // The bus collects on the manager's Default-dispatcher scope — poll briefly.
        val deadline = System.currentTimeMillis() + 5_000
        while (m.statuses.value[ProviderKind.SHIZUKU] !is ProviderStatus.Ready &&
            System.currentTimeMillis() < deadline
        ) {
            Thread.sleep(25)
        }
        assertEquals(ProviderStatus.Ready, m.statuses.value[ProviderKind.SHIZUKU])
    }

    @Test
    fun `refreshStatuses republishes live provider state synchronously`() {
        val m = manager()
        assertTrue(m.statuses.value[ProviderKind.SHIZUKU] is ProviderStatus.Unavailable)
        m.refreshStatuses()
        assertEquals(m.providerStatuses(), m.statuses.value)
    }
}

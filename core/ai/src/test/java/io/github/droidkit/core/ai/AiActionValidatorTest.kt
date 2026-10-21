package io.github.droidkit.core.ai

import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolRegistry
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.CapabilityProvider
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiActionValidatorTest {

    private object FakeFeatures : FeatureContribution {
        override val tools = listOf(
            Tool(
                id = "device.info",
                title = "Device Inspector",
                description = "x",
                category = ToolCategory.DEVICE,
                route = "device/info",
            ),
            Tool(
                id = "needs.elevated",
                title = "Elevated",
                description = "x",
                category = ToolCategory.DEVELOPER,
                route = "dev/elevated",
                requiredCapabilities = setOf(Capability.ELEVATED_SHELL),
            ),
        )
    }

    private val registry = ToolRegistry(setOf(FakeFeatures))

    private fun validator(providers: Set<CapabilityProvider> = defaultProviders()) =
        AiActionValidator(
            registry,
            CapabilityManager(
                providers = providers,
                sdkInt = 35,
                hasFeature = { true },
                permissionChecker = { true },
            ),
        )

    private fun defaultProviders() = setOf<CapabilityProvider>(
        stubProvider(
            ProviderKind.STANDARD,
            ProviderStatus.Ready,
            setOf(Capability.STANDARD_API),
        ),
    )

    private fun stubProvider(
        kind: ProviderKind,
        status: ProviderStatus,
        caps: Set<Capability> = setOf(Capability.ELEVATED_SHELL),
    ) = object : CapabilityProvider {
        override val kind = kind
        override val capabilities = caps
        override val status = status
    }

    @Test
    fun `open_tool allowed when tool resolves ready`() {
        val vp = validator().validate(AiProposal.OpenTool("device.info"))
        assertTrue(vp.allowed)
        assertNull(vp.blockedReason)
    }

    @Test
    fun `open_tool blocked for unknown tool`() {
        val vp = validator().validate(AiProposal.OpenTool("does.not.exist"))
        assertFalse(vp.allowed)
        assertEquals("unknown tool", vp.blockedReason)
    }

    @Test
    fun `open_tool blocked when capability unsatisfied`() {
        val vp = validator().validate(AiProposal.OpenTool("needs.elevated"))
        assertFalse(vp.allowed)
        assertTrue(vp.blockedReason!!.contains("ADB") || vp.blockedReason!!.isNotBlank())
    }

    @Test
    fun `toolRoute returns route only for known tools`() {
        val v = validator()
        assertEquals("device/info", v.toolRoute("device.info"))
        assertNull(v.toolRoute("nope"))
    }

    @Test
    fun `read-only elevated proposal allowed`() {
        val vp = validator().validate(
            AiProposal.ReadOnlyElevated(io.github.droidkit.core.privileges.ElevatedAction.WhoAmI),
        )
        assertTrue(vp.allowed)
    }
}

package io.github.droidkit.core.privileges

import io.github.droidkit.core.common.UiProblem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Catalog coverage for the friendly-error layer: every [BlockedReason] variant
 * and the generic fallbacks must set a `catalogKey` the `strings_errors.xml`
 * catalog resolves. The key set here is the contract — a new variant without a
 * catalog entry fails this test, not a runtime lookup.
 */
class FriendlyErrorCatalogTest {

    private val reasons = listOf(
        BlockedReason.MissingPermission("android.permission.CAMERA"),
        BlockedReason.MissingHardware("android.hardware.nfc"),
        BlockedReason.RequiresApi(34),
        BlockedReason.NeedsProvider(ProviderKind.ADB, ProviderStatus.Ready),
        BlockedReason.NeedsProvider(ProviderKind.ADB, ProviderStatus.NeedsSetup("pair")),
        BlockedReason.NeedsProvider(ProviderKind.ROOT, ProviderStatus.Unavailable("none")),
    )

    @Test
    fun `every BlockedReason variant sets a catalog key`() {
        reasons.forEach { reason ->
            val problem = reason.toUiProblem()
            assertNotNull("no catalogKey for $reason", problem.catalogKey)
        }
    }

    @Test
    fun `catalog keys match the strings_errors catalog contract`() {
        val keys = reasons.map { it.toUiProblem().catalogKey }.toSet()
        assertEquals(
            setOf(
                "permission", "hardware", "api",
                "provider_ready", "provider_setup", "provider_unavailable",
            ),
            keys,
        )
    }

    @Test
    fun `catalog args carry the substitution values`() {
        assertEquals(
            listOf("android.permission.CAMERA"),
            BlockedReason.MissingPermission("android.permission.CAMERA")
                .toUiProblem().catalogArgs,
        )
        assertEquals(
            listOf("34"),
            BlockedReason.RequiresApi(34).toUiProblem().catalogArgs,
        )
        val provider = BlockedReason.NeedsProvider(
            ProviderKind.ADB,
            ProviderStatus.NeedsSetup("pair wireless ADB"),
        ).toUiProblem()
        assertEquals(listOf("ADB", "pair wireless ADB"), provider.catalogArgs)
    }

    @Test
    fun `generic fallbacks set catalog keys`() {
        assertEquals(
            "unavailable",
            ToolResolution.Blocked(emptyList()).toUiProblem().catalogKey,
        )
        assertEquals(
            "unexpected",
            UiProblem.unexpected(IllegalStateException("boom")).catalogKey,
        )
    }
}

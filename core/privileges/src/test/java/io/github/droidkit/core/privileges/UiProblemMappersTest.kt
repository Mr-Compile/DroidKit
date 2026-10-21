package io.github.droidkit.core.privileges

import io.github.droidkit.core.common.RecoveryAction
import io.github.droidkit.core.common.UiProblem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UiProblemMappersTest {

    @Test
    fun `missing permission maps to permission recovery`() {
        val problem = BlockedReason.MissingPermission("android.permission.CAMERA").toUiProblem()
        assertTrue(problem.recoveries.any { it is RecoveryAction.RequestPermission })
    }

    @Test
    fun `missing hardware is informational and not an error`() {
        val problem = BlockedReason.MissingHardware("android.hardware.nfc").toUiProblem()
        assertEquals(UiProblem.Severity.INFO, problem.severity)
        assertTrue(problem.recoveries.none { it is RecoveryAction.RequestPermission })
    }

    @Test
    fun `needs provider maps to elevated-access recovery`() {
        val problem = BlockedReason.NeedsProvider(
            ProviderKind.ADB,
            ProviderStatus.NeedsSetup("pair wireless ADB"),
        ).toUiProblem()
        assertTrue(problem.recoveries.any { it == RecoveryAction.RequestElevatedAccess })
    }

    @Test
    fun `blocked resolution picks most actionable reason`() {
        val resolution = ToolResolution.Blocked(
            listOf(
                BlockedReason.MissingHardware("android.hardware.nfc"),
                BlockedReason.NeedsProvider(ProviderKind.ADB, ProviderStatus.Unavailable("off")),
            ),
        )
        val problem = resolution.toUiProblem()
        assertTrue(problem.recoveries.any { it == RecoveryAction.RequestElevatedAccess })
    }
}

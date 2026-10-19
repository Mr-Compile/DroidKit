package io.github.droidkit.feature.nfc

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution

object NfcFeature : FeatureContribution {
    const val ROUTE = "nfc"
    const val WRITE_ROUTE = "nfc/write"

    override val tools = listOf(
        Tool(
            id = "nfc.read",
            title = "NFC Tag Read",
            description = "Tag technologies, NDEF records and Tag Doctor",
            category = ToolCategory.HARDWARE,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredPermissions = listOf("android.permission.NFC"),
            requiredFeatures = setOf("android.hardware.nfc"),
            phase = ToolPhase.V1,
        ),
        Tool(
            id = "nfc.write",
            title = "NFC Write / Erase",
            description = "Write text/URI NDEF records or erase writable tags",
            category = ToolCategory.HARDWARE,
            route = WRITE_ROUTE,
            safetyLevel = SafetyLevel.MINOR,
            requiredPermissions = listOf("android.permission.NFC"),
            requiredFeatures = setOf("android.hardware.nfc"),
            phase = ToolPhase.V1,
        ),
    )
}

fun NavGraphBuilder.nfcGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(NfcFeature.ROUTE) {
        NfcScreen(onOpenWrite = { nav.navigate(NfcFeature.WRITE_ROUTE) })
    }
    composable(NfcFeature.WRITE_ROUTE) {
        NfcWriteScreen()
    }
}

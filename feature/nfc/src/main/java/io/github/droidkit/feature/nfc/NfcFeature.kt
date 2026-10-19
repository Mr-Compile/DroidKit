package io.github.droidkit.feature.nfc

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object NfcFeature : FeatureContribution {
    const val ROUTE = "nfc"

    override val tools = listOf(
        Tool(
            id = "nfc.read",
            title = "NFC Tag Read",
            description = "Tag technologies and NDEF record reading",
            category = ToolCategory.HARDWARE,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.NFC"),
            requiredFeatures = setOf("android.hardware.nfc"),
        ),
        Tool(
            id = "nfc.write",
            title = "NFC Write / Format",
            description = "Write, erase and format supported tags",
            category = ToolCategory.HARDWARE,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.NFC"),
            requiredFeatures = setOf("android.hardware.nfc"),
        ),
    )
}

fun NavGraphBuilder.nfcGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(NfcFeature.ROUTE) {
        FeatureStubScreen(
            title = "NFC Lab",
            subtitle = "Tag tech detection, NDEF read and write/format ship in Phase 6.",
            tools = NfcFeature.tools,
            resolve = resolve,
        )
    }
}

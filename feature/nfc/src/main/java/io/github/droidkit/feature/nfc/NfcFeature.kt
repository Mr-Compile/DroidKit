package io.github.droidkit.feature.nfc

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolIconKeys
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution

object NfcFeature : FeatureContribution {
    const val ROUTE = "nfc"
    const val WRITE_ROUTE = "nfc/write"

    override val tools = listOf(
        Tool(
            id = "nfc.read",
            title = "NFC Lab — Scan & Inspect",
            description = "Tag technologies, NDEF records, tech details, Tag Doctor",
            category = ToolCategory.HARDWARE,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredPermissions = listOf("android.permission.NFC"),
            requiredFeatures = setOf("android.hardware.nfc"),
            phase = ToolPhase.V1,
            keywords = setOf(
                "nfc", "tag", "ndef", "scan", "reader", "contactless",
                "inspect", "mifare", "ultralight", "isodep",
            ),
            iconKey = ToolIconKeys.NFC,
        ),
        Tool(
            id = "nfc.write",
            title = "NFC Writer — Records & Ops",
            description = "Compose NDEF records, write, duplicate, format or erase tags",
            category = ToolCategory.HARDWARE,
            route = WRITE_ROUTE,
            safetyLevel = SafetyLevel.MINOR,
            requiredPermissions = listOf("android.permission.NFC"),
            requiredFeatures = setOf("android.hardware.nfc"),
            phase = ToolPhase.V1,
            keywords = setOf(
                "write", "erase", "ndef", "tag", "format", "duplicate",
                "copy", "clone", "vcard", "wifi", "contact", "link",
            ),
            iconKey = ToolIconKeys.CONTACTLESS,
        ),
    )
}

/**
 * Owner for the ViewModel shared by the read screen and writer. Normal
 * intake has "nfc" underneath; direct launches (tool card, search) have no
 * parent entry, so the nav graph owns the ViewModel instead of crashing.
 */
private fun NavHostController.nfcStoreOwner(): ViewModelStoreOwner =
    runCatching { getBackStackEntry(NfcFeature.ROUTE) }
        .getOrElse { getViewModelStoreOwner(graph.id) }

fun NavGraphBuilder.nfcGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(NfcFeature.ROUTE) {
        NfcScreen(
            onOpenWrite = { nav.navigate(NfcFeature.WRITE_ROUTE) { launchSingleTop = true } },
            onBack = { nav.navigateUp() },
        )
    }
    composable(NfcFeature.WRITE_ROUTE) {
        // Shares the read screen's ViewModel so the composer sees the last
        // scanned tag (capacity preview, duplicate payload).
        NfcWriteScreen(
            onBack = { nav.navigateUp() },
            viewModel = hiltViewModel(nav.nfcStoreOwner()),
        )
    }
}

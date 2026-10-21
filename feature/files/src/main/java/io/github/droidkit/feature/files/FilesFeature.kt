package io.github.droidkit.feature.files

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolIconKeys
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution

object FilesFeature : FeatureContribution {
    const val ROUTE = "files"

    override val tools = listOf(
        Tool(
            id = "files.storage",
            title = "Restricted storage browser",
            description = "Browse Android/data, preview text, batch-copy, SAF fallback",
            category = ToolCategory.APPS,
            route = ROUTE,
            safetyLevel = SafetyLevel.ADVANCED,
            requiredCapabilities = setOf(Capability.ELEVATED_SHELL),
            phase = ToolPhase.V2,
            keywords = setOf("android data", "obb", "files", "storage", "access denied", "search", "preview", "saf"),
            iconKey = ToolIconKeys.STORAGE,
        ),
    )
}

fun NavGraphBuilder.filesGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(FilesFeature.ROUTE) {
        FilesScreen(
            onBack = { nav.navigateUp() },
            onOpenAdbSetup = { nav.navigate("adb") },
        )
    }
}

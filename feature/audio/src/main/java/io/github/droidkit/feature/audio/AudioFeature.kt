package io.github.droidkit.feature.audio

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object AudioFeature : FeatureContribution {
    const val ROUTE = "audio"

    override val tools = listOf(
        Tool(
            id = "audio.speaker",
            title = "Speaker Test",
            description = "Play test tones through each output",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
        ),
        Tool(
            id = "audio.microphone",
            title = "Microphone Test",
            description = "Record and meter microphone input",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.RECORD_AUDIO"),
        ),
        Tool(
            id = "audio.vibration",
            title = "Vibration Test",
            description = "Run haptic patterns and confirm motor response",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.VIBRATE"),
        ),
    )
}

fun NavGraphBuilder.audioGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(AudioFeature.ROUTE) {
        FeatureStubScreen(
            title = "Audio & Haptics",
            subtitle = "Speaker, microphone and vibration tests ship in Phase 3 (Phone Doctor).",
            tools = AudioFeature.tools,
            resolve = resolve,
        )
    }
}

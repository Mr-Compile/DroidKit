package io.github.droidkit.feature.audio

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolIconKeys
import io.github.droidkit.core.ui.LabEntry
import io.github.droidkit.core.ui.LabIndexScreen

object AudioFeature : FeatureContribution {
    const val ROUTE = "audio"

    override val tools = listOf(
        Tool(
            id = "audio.speaker",
            title = "Speaker Test",
            description = "Play test tones through each output",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
            keywords = setOf("tone", "sound", "loudspeaker", "output"),
            iconKey = ToolIconKeys.SPEAKER,
        ),
        Tool(
            id = "audio.microphone",
            title = "Microphone Test",
            description = "Record and meter microphone input",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.RECORD_AUDIO"),
            keywords = setOf("record", "mic", "voice", "input"),
            iconKey = ToolIconKeys.MIC,
        ),
        Tool(
            id = "audio.vibration",
            title = "Vibration Test",
            description = "Run haptic patterns and confirm motor response",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.VIBRATE"),
            keywords = setOf("haptic", "motor", "buzz"),
            iconKey = ToolIconKeys.VIBRATION,
        ),
        Tool(
            id = "audio.routing",
            title = "Audio Routing",
            description = "Audio devices, active mode, routing and capabilities",
            category = ToolCategory.DIAGNOSTICS,
            route = "$ROUTE/routing",
            keywords = setOf("route", "devices", "bluetooth audio", "headset"),
            iconKey = ToolIconKeys.AUDIO_ROUTE,
        ),
        Tool(
            id = "audio.lab",
            title = "Audio Lab",
            description = "Tone generator, stereo channel check and mic level meter",
            category = ToolCategory.DIAGNOSTICS,
            route = "$ROUTE/lab",
            keywords = setOf("tone", "sine", "channel", "left", "right", "meter"),
            iconKey = ToolIconKeys.SPEAKER,
        ),
    )
}

fun NavGraphBuilder.audioGraph(nav: NavHostController) {
    composable(AudioFeature.ROUTE) {
        LabIndexScreen(
            title = "Audio & Haptics",
            subtitle = "Standalone audio and haptics checks from Phone Doctor. " +
                "Results here are not saved — run Full Diagnostic to persist them.",
            entries = listOf(
                LabEntry(
                    testId = "audio.speaker",
                    title = "Speaker",
                    blurb = "Play a test tone and confirm clear, consistent output.",
                ),
                LabEntry(
                    testId = "audio.microphone",
                    title = "Microphone",
                    blurb = "Record a few seconds of speech and play it back. " +
                        "Needs microphone permission.",
                ),
                LabEntry(
                    testId = "haptics.vibration",
                    title = "Vibration",
                    blurb = "Buzz the haptic motor and confirm the response.",
                ),
            ),
            onOpenTest = { testId -> nav.navigate("diagnose/test/$testId") { launchSingleTop = true } },
            onBack = { nav.navigateUp() },
        )
    }
    composable("${AudioFeature.ROUTE}/routing") {
        AudioRoutingScreen(onBack = { nav.navigateUp() })
    }
    composable("${AudioFeature.ROUTE}/lab") {
        AudioLabScreen(onBack = { nav.navigateUp() })
    }
}

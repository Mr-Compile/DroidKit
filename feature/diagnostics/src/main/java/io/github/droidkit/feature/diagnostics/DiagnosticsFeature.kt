package io.github.droidkit.feature.diagnostics

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.SearchEntry
import io.github.droidkit.core.common.SearchResultType
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolIconKeys
import io.github.droidkit.core.ui.navigation.openRoute

object DiagnosticsFeature : FeatureContribution {
    const val ROUTE = "diagnose"
    const val GUIDED_ROUTE = "diagnose/guided"
    const val REVIEW_ROUTE = "diagnose/review"
    const val TEST_ROUTE = "diagnose/test/{testId}"

    fun testRoute(testId: String) = "diagnose/test/$testId"

    override val tools = listOf(
        Tool(
            id = "diagnostics.full",
            title = "Full Diagnostic",
            description = "Guided end-to-end hardware check with a saved report",
            category = ToolCategory.DIAGNOSTICS,
            route = GUIDED_ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredCapabilities = setOf(Capability.STANDARD_API),
            keywords = setOf("phone doctor", "checkup", "health check", "all tests"),
            iconKey = ToolIconKeys.MONITOR_HEART,
        ),
        Tool(
            id = "diagnostics.quick",
            title = "Quick Tests",
            description = "Jump straight to a single hardware test",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
            keywords = setOf("single test", "check", "test"),
            iconKey = ToolIconKeys.FACT_CHECK,
        ),
    )

    /** Per-test icon identity — one key drives every surface a test appears on. */
    private val testIcons: Map<String, String> = mapOf(
        "display.color" to ToolIconKeys.PALETTE,
        "display.dead_pixel" to ToolIconKeys.DEAD_PIXEL,
        "touch.grid" to ToolIconKeys.TOUCH,
        "touch.multitouch" to ToolIconKeys.TOUCH,
        "haptics.vibration" to ToolIconKeys.VIBRATION,
        "sensor.accelerometer" to ToolIconKeys.SENSORS,
        "sensor.gyroscope" to ToolIconKeys.SENSORS,
        "sensor.proximity" to ToolIconKeys.SENSORS,
        "sensor.light" to ToolIconKeys.SENSORS,
        "sensor.magnetometer" to ToolIconKeys.COMPASS,
        "audio.speaker" to ToolIconKeys.SPEAKER,
        "audio.microphone" to ToolIconKeys.MIC,
        "audio.headset_jack" to ToolIconKeys.AUDIO_ROUTE,
        "buttons.volume" to ToolIconKeys.SPEAKER,
        "camera.front" to ToolIconKeys.CAMERA,
        "camera.rear" to ToolIconKeys.CAMERA,
        "camera.flash" to ToolIconKeys.FLASH,
        "connectivity.wifi" to ToolIconKeys.WIFI,
        "connectivity.bluetooth" to ToolIconKeys.BLUETOOTH,
        "connectivity.location" to ToolIconKeys.GPS,
        "connectivity.nfc" to ToolIconKeys.NFC,
        "connectivity.usb" to ToolIconKeys.USB,
        "power.charging" to ToolIconKeys.CHARGING,
    )

    /** Icon key for a catalog test — the same key its search entry uses. */
    fun testIconKey(testId: String): String = testIcons.getValue(testId)

    private val testKeywords: Map<String, Set<String>> = mapOf(
        "display.color" to setOf("screen", "color", "banding", "tint"),
        "display.dead_pixel" to setOf("pixel", "stuck", "screen", "dot"),
        "touch.grid" to setOf("touch", "dead zone", "screen"),
        "touch.multitouch" to setOf("fingers", "multi touch", "points"),
        "haptics.vibration" to setOf("vibrate", "haptic", "motor", "buzz"),
        "sensor.accelerometer" to setOf("tilt", "motion"),
        "sensor.gyroscope" to setOf("rotation", "spin"),
        "sensor.proximity" to setOf("earpiece", "near", "far"),
        "sensor.light" to setOf("lux", "brightness", "ambient"),
        "sensor.magnetometer" to setOf("magnet", "compass", "field"),
        "audio.speaker" to setOf("sound", "tone", "output", "loudspeaker"),
        "audio.microphone" to setOf("record", "mic", "voice"),
        "audio.headset_jack" to setOf("headset", "headphones", "3.5mm", "usb-c audio", "earphones"),
        "buttons.volume" to setOf("volume", "buttons", "keys", "volume up", "volume down"),
        "camera.front" to setOf("selfie", "preview", "photo"),
        "camera.rear" to setOf("back", "preview", "photo"),
        "camera.flash" to setOf("torch", "led", "light"),
        "connectivity.wifi" to setOf("wifi", "wireless", "radio"),
        "connectivity.bluetooth" to setOf("bt", "radio"),
        "connectivity.location" to setOf("gps", "gnss", "location"),
        "connectivity.nfc" to setOf("nfc", "contactless", "tag"),
        "connectivity.usb" to setOf("usb", "otg", "host"),
        "power.charging" to setOf("charge", "charger", "plug", "battery"),
    )

    /**
     * Every Phone Doctor test is searchable as a DIAGNOSTIC result
     * (plan file 10). Entries are generated from the catalog so a new test
     * can never drift out of search; [testIcons] is asserted complete by
     * PhoneDoctorCatalogTest.
     */
    override val searchEntries: List<SearchEntry> = PhoneDoctorTests.ALL.map { test ->
        SearchEntry(
            id = "diag.${test.id}",
            title = test.title,
            subtitle = "${test.category} test",
            route = testRoute(test.id),
            type = SearchResultType.DIAGNOSTIC,
            iconKey = testIcons.getValue(test.id),
            keywords = testKeywords[test.id].orEmpty(),
        )
    }
}

/**
 * Owner for the session ViewModel shared by the picker, guided runner and
 * review. When the Diagnose tab entry sits underneath (the normal intake
 * path) the session is scoped to it; external entries — the Tools card,
 * Dashboard recents, search — have no "diagnose" on the back stack, so the
 * nav graph owns the session instead of crashing in getBackStackEntry.
 */
private fun NavHostController.sessionStoreOwner(): ViewModelStoreOwner =
    runCatching { getBackStackEntry(DiagnosticsFeature.ROUTE) }
        .getOrElse { getViewModelStoreOwner(graph.id) }

fun NavGraphBuilder.diagnosticsGraph(nav: NavHostController) {
    composable(DiagnosticsFeature.ROUTE) {
        DiagnosticsScreen(
            onStartGuided = { nav.navigate(DiagnosticsFeature.GUIDED_ROUTE) { launchSingleTop = true } },
            onOpenTool = { route -> nav.openRoute(route) },
            onOpenReports = { nav.openRoute("reports") },
        )
    }
    composable(DiagnosticsFeature.GUIDED_ROUTE) {
        GuidedSessionScreen(
            onFinished = {
                nav.navigate(DiagnosticsFeature.REVIEW_ROUTE) {
                    popUpTo(DiagnosticsFeature.GUIDED_ROUTE) { inclusive = true }
                }
            },
            onBack = { nav.navigateUp() },
            viewModel = hiltViewModel(nav.sessionStoreOwner()),
        )
    }
    composable(DiagnosticsFeature.REVIEW_ROUTE) {
        SessionReviewScreen(
            onRetry = { nav.navigate(DiagnosticsFeature.GUIDED_ROUTE) { launchSingleTop = true } },
            onSaved = {
                nav.navigate(DiagnosticsFeature.ROUTE) {
                    popUpTo(DiagnosticsFeature.ROUTE) { inclusive = true }
                }
            },
            onBack = { nav.navigateUp() },
            viewModel = hiltViewModel(nav.sessionStoreOwner()),
        )
    }
    composable(DiagnosticsFeature.TEST_ROUTE) { backStackEntry ->
        val testId = backStackEntry.arguments?.getString("testId").orEmpty()
        val test = PhoneDoctorTests.findById(testId)
        if (test == null) {
            UnknownTestScreen(onBack = { nav.navigateUp() })
        } else {
            PhoneDoctorTestScreen(
                test = test,
                onResult = { _, _ -> nav.popBackStack() },
                onBack = { nav.navigateUp() },
                autoResult = false,
            )
        }
    }
}

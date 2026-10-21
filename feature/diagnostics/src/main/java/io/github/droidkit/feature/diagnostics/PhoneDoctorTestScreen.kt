package io.github.droidkit.feature.diagnostics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.feature.diagnostics.tests.CameraTestScreen
import io.github.droidkit.feature.diagnostics.tests.ChargingCheckScreen
import io.github.droidkit.feature.diagnostics.tests.ConnectivityCheckScreen
import io.github.droidkit.feature.diagnostics.tests.DisplayColorScreen
import io.github.droidkit.feature.diagnostics.tests.FlashTestScreen
import io.github.droidkit.feature.diagnostics.tests.LocationCheckScreen
import io.github.droidkit.feature.diagnostics.tests.MicrophoneTestScreen
import io.github.droidkit.feature.diagnostics.tests.MultitouchScreen
import io.github.droidkit.feature.diagnostics.tests.SensorTestScreen
import io.github.droidkit.feature.diagnostics.tests.SpeakerTestScreen
import io.github.droidkit.feature.diagnostics.tests.TouchGridScreen
import io.github.droidkit.feature.diagnostics.tests.VibrationScreen

/**
 * Single dispatch point for every Phone Doctor test screen.
 * Used by both the standalone quick-test route and the guided runner.
 *
 * [autoResult] lets objective checks auto-emit their result — needed by the
 * guided runner, which advances on emit. Standalone mode must not auto-emit:
 * the route pops on result, so an automatic emit would close the screen
 * before the reading is ever visible.
 */
@Composable
fun PhoneDoctorTestScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
    onBack: (() -> Unit)? = null,
    autoResult: Boolean = true,
) {
    // Several routes serve multiple tests (CameraPreview: front/rear, Sensor: 5
    // types, ConnectivityCheck: 5 radios, DisplayColor: color/dead-pixel). In the
    // guided runner the same call site recomposes with a new [test], so key on
    // test.id — without it, remembered state and Unit-keyed effects leak across
    // tests (e.g. the rear camera test kept showing the front preview).
    key(test.id) {
        when (routeFor(test)) {
            PhoneDoctorRoute.DisplayColor -> DisplayColorScreen(test, onResult)
            PhoneDoctorRoute.TouchGrid -> TouchGridScreen(test, onResult)
            PhoneDoctorRoute.Multitouch -> MultitouchScreen(test, onResult)
            PhoneDoctorRoute.Vibration -> VibrationScreen(test, onResult, autoResult)
            PhoneDoctorRoute.Speaker -> SpeakerTestScreen(test, onResult)
            PhoneDoctorRoute.Microphone -> MicrophoneTestScreen(test, onResult, autoResult)
            PhoneDoctorRoute.CameraPreview -> CameraTestScreen(test, onResult, autoResult)
            PhoneDoctorRoute.Flash -> FlashTestScreen(test, onResult, autoResult)
            PhoneDoctorRoute.ConnectivityCheck -> ConnectivityCheckScreen(test, onResult, autoResult)
            PhoneDoctorRoute.LocationCheck -> LocationCheckScreen(test, onResult, autoResult)
            PhoneDoctorRoute.ChargingCheck -> ChargingCheckScreen(test, onResult, autoResult)
            PhoneDoctorRoute.Sensor -> SensorTestScreen(test, onResult, autoResult)
            PhoneDoctorRoute.Unsupported -> UnsupportedTestPlaceholder(test, onBack)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnsupportedTestPlaceholder(test: PhoneDoctorTest, onBack: (() -> Unit)?) {
    Scaffold(topBar = { DroidKitTopAppBar(test.title, onBack = onBack) }) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text("Test screen not yet wired: ${test.id}")
        }
    }
}

/**
 * Friendly fallback for invalid `diagnose/test/{testId}` route arguments
 * (plan file 13 — invalid arguments must fail safely, not render raw text).
 */
@Composable
fun UnknownTestScreen(onBack: () -> Unit) {
    Scaffold(topBar = { DroidKitTopAppBar("Diagnostic test", onBack = onBack) }) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            EmptyState("This test is not available — it may have been renamed or removed.")
        }
    }
}

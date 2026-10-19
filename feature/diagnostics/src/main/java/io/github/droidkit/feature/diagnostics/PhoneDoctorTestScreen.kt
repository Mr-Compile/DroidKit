package io.github.droidkit.feature.diagnostics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.feature.diagnostics.tests.CameraTestScreen
import io.github.droidkit.feature.diagnostics.tests.ChargingCheckScreen
import io.github.droidkit.feature.diagnostics.tests.ConnectivityCheckScreen
import io.github.droidkit.feature.diagnostics.tests.DisplayColorScreen
import io.github.droidkit.feature.diagnostics.tests.FlashTestScreen
import io.github.droidkit.feature.diagnostics.tests.MicrophoneTestScreen
import io.github.droidkit.feature.diagnostics.tests.MultitouchScreen
import io.github.droidkit.feature.diagnostics.tests.SensorTestScreen
import io.github.droidkit.feature.diagnostics.tests.SpeakerTestScreen
import io.github.droidkit.feature.diagnostics.tests.TouchGridScreen
import io.github.droidkit.feature.diagnostics.tests.VibrationScreen

/**
 * Single dispatch point for every Phone Doctor test screen.
 * Used by both the standalone quick-test route and the guided runner.
 */
@Composable
fun PhoneDoctorTestScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
) {
    when (routeFor(test)) {
        PhoneDoctorRoute.DisplayColor -> DisplayColorScreen(test, onResult)
        PhoneDoctorRoute.TouchGrid -> TouchGridScreen(test, onResult)
        PhoneDoctorRoute.Multitouch -> MultitouchScreen(test, onResult)
        PhoneDoctorRoute.Vibration -> VibrationScreen(test, onResult)
        PhoneDoctorRoute.Speaker -> SpeakerTestScreen(test, onResult)
        PhoneDoctorRoute.Microphone -> MicrophoneTestScreen(test, onResult)
        PhoneDoctorRoute.CameraPreview -> CameraTestScreen(test, onResult)
        PhoneDoctorRoute.Flash -> FlashTestScreen(test, onResult)
        PhoneDoctorRoute.ConnectivityCheck -> ConnectivityCheckScreen(test, onResult)
        PhoneDoctorRoute.ChargingCheck -> ChargingCheckScreen(test, onResult)
        PhoneDoctorRoute.Sensor -> SensorTestScreen(test, onResult)
        PhoneDoctorRoute.Unsupported -> UnsupportedTestPlaceholder(test)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnsupportedTestPlaceholder(test: PhoneDoctorTest) {
    Scaffold(topBar = { TopAppBar(title = { Text(test.title) }) }) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text("Test screen not yet wired: ${test.id}")
        }
    }
}

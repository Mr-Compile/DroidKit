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
    when {
        test.id.startsWith("display.") -> DisplayColorScreen(test, onResult)
        test.id == "touch.grid" -> TouchGridScreen(test, onResult)
        test.id == "touch.multitouch" -> MultitouchScreen(test, onResult)
        test.id == "haptics.vibration" -> VibrationScreen(test, onResult)
        test.id == "audio.speaker" -> SpeakerTestScreen(test, onResult)
        test.id == "audio.microphone" -> MicrophoneTestScreen(test, onResult)
        test.id.startsWith("camera.") && test.id != "camera.flash" ->
            CameraTestScreen(test, onResult)
        test.id == "camera.flash" -> FlashTestScreen(test, onResult)
        test.capabilityKey != null -> ConnectivityCheckScreen(test, onResult)
        test.id == "power.charging" -> ChargingCheckScreen(test, onResult)
        test.sensorType != null -> SensorTestScreen(test, onResult)
        else -> UnsupportedTestPlaceholder(test)
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

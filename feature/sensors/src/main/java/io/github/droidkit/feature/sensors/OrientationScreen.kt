package io.github.droidkit.feature.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.EmptyState
import kotlin.math.roundToInt

/**
 * Orientation helper (post-V1 sensor lab): azimuth/pitch/roll derived from the
 * rotation-vector sensor. Honest about absence — a device without a fused
 * rotation vector reports NOT_SUPPORTED rather than a fake compass.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrientationScreen(
    viewModel: SensorsViewModel = hiltViewModel(),
) {
    val sensor = viewModel.defaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    var orientation by remember { mutableStateOf<Triple<Float, Float, Float>?>(null) }
    var accuracy by remember { mutableStateOf(-1) }

    DisposableEffect(sensor) {
        if (sensor == null) return@DisposableEffect onDispose {}
        val rotationMatrix = FloatArray(9)
        val out = FloatArray(3)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, out)
                orientation = Triple(
                    Math.toDegrees(out[0].toDouble()).toFloat(), // azimuth, -180..180
                    Math.toDegrees(out[1].toDouble()).toFloat(), // pitch
                    Math.toDegrees(out[2].toDouble()).toFloat(), // roll
                )
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {
                accuracy = a
            }
        }
        viewModel.sensorManager?.registerListener(
            listener, sensor, SensorManager.SENSOR_DELAY_UI,
        )
        onDispose { viewModel.sensorManager?.unregisterListener(listener) }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Orientation") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (sensor == null) {
                item {
                    EmptyState(
                        "No rotation-vector sensor on this device (NOT_SUPPORTED) — " +
                            "a reliable compass needs accelerometer + magnetometer fusion.",
                    )
                }
            } else {
                item {
                    SectionCard(title = "Orientation") {
                        val o = orientation
                        if (o == null) {
                            Text("Waiting for first reading…", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            InfoRow("Azimuth", "${o.first.roundToInt()}° (0=N, 90=E)")
                            InfoRow("Pitch", "${o.second.roundToInt()}° (+tilt back)")
                            InfoRow("Roll", "${o.third.roundToInt()}° (+right edge up)")
                        }
                        InfoRow(
                            "Reported accuracy",
                            when (accuracy) {
                                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "high"
                                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "medium"
                                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "low — move in a figure-8"
                                SensorManager.SENSOR_STATUS_UNRELIABLE -> "unreliable"
                                else -> "not reported"
                            },
                            copyable = false,
                        )
                    }
                }
                item {
                    SectionCard(title = "Sensor") {
                        InfoRow("Name", sensor.name)
                        InfoRow("Vendor", sensor.vendor)
                    }
                }
            }
        }
    }
}

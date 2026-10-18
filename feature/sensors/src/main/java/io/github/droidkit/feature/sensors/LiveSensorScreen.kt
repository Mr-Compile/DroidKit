package io.github.droidkit.feature.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
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
import kotlin.math.abs
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveSensorScreen(
    sensorType: Int,
    viewModel: SensorsViewModel = hiltViewModel(),
) {
    val supported = SensorsViewModel.LIVE_TYPES.contains(sensorType)
    val sensor = if (supported) viewModel.defaultSensor(sensorType) else null
    val unit = SensorsViewModel.unitFor(sensorType)

    var values by remember { mutableStateOf<FloatArray?>(null) }
    var updates by remember { mutableStateOf(0L) }

    DisposableEffect(sensor) {
        if (sensor == null) return@DisposableEffect onDispose {}
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                values = event.values.copyOf()
                updates++
            }
            override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}
        }
        viewModel.sensorManager?.registerListener(
            listener, sensor, SensorManager.SENSOR_DELAY_UI,
        )
        onDispose { viewModel.sensorManager?.unregisterListener(listener) }
    }

    val axes = when (sensorType) {
        Sensor.TYPE_LIGHT, Sensor.TYPE_PROXIMITY, Sensor.TYPE_PRESSURE,
        Sensor.TYPE_AMBIENT_TEMPERATURE, Sensor.TYPE_RELATIVE_HUMIDITY -> listOf("value")
        Sensor.TYPE_ROTATION_VECTOR -> listOf("x", "y", "z", "w")
        else -> listOf("x", "y", "z")
    }

    Scaffold(topBar = { TopAppBar(title = { Text(sensor?.name ?: "Live Sensor") }) }) { padding ->
        when {
            !supported -> Column(Modifier.padding(padding)) {
                EmptyState("This sensor type is not in the live-monitor safe list.")
            }
            sensor == null -> Column(Modifier.padding(padding)) {
                EmptyState("Sensor not present on this device.")
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    SectionCard(title = "Live values") {
                        if (values == null) {
                            Text("Waiting for first reading…", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            axes.forEachIndexed { i, axis ->
                                val v = values!!.getOrNull(i) ?: 0f
                                InfoRow(axis, "%.3f %s".format(v, unit).trim())
                                if (sensor.maximumRange > 0f) {
                                    LinearProgressIndicator(
                                        progress = { min(1f, abs(v) / sensor.maximumRange) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 4.dp),
                                    )
                                }
                            }
                            InfoRow("Updates", updates.toString(), copyable = false)
                        }
                    }
                }
                item {
                    SectionCard(title = "Sensor") {
                        InfoRow("Vendor", sensor.vendor)
                        InfoRow("Type", sensor.stringType ?: sensorType.toString())
                        InfoRow("Max range", "${sensor.maximumRange} $unit".trim())
                        InfoRow("Resolution", sensor.resolution.toString())
                    }
                }
            }
        }
    }
}

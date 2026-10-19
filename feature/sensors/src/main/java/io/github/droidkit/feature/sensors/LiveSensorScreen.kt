package io.github.droidkit.feature.sensors

import android.content.Intent
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.EmptyState
import kotlin.math.abs
import kotlin.math.min

private const val MAX_SAMPLES = 2000

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
    var recording by remember { mutableStateOf(false) }
    val samples = remember { mutableListOf<Pair<Long, FloatArray>>() }
    val context = LocalContext.current

    DisposableEffect(sensor, recording) {
        if (sensor == null) return@DisposableEffect onDispose {}
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                values = event.values.copyOf()
                updates++
                if (recording && samples.size < MAX_SAMPLES) {
                    samples += event.timestamp to event.values.copyOf()
                }
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
                    SectionCard(title = "Session recording") {
                        Text(
                            if (recording) {
                                "Recording… ${samples.size} samples (cap $MAX_SAMPLES)"
                            } else {
                                "Record a short live session, then share it as CSV."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(
                            onClick = {
                                if (recording) {
                                    recording = false
                                } else {
                                    samples.clear()
                                    recording = true
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp),
                        ) { Text(if (recording) "Stop" else "Record") }
                        if (!recording && samples.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    val sensorName = sensor.name.replace(" ", "_")
                                    val csv = buildString {
                                        appendLine("sensor,${sensor.name}")
                                        appendLine("unit,${unit}")
                                        appendLine("timestamp_ns,${axes.joinToString(",")}")
                                        samples.forEach { (ts, v) ->
                                            appendLine("$ts," + axes.indices.joinToString(",") {
                                                "%.6f".format(v.getOrElse(it) { 0f })
                                            })
                                        }
                                    }
                                    val send = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_SUBJECT, "DroidKit sensor session — $sensorName")
                                        putExtra(Intent.EXTRA_TEXT, csv)
                                    }
                                    context.startActivity(Intent.createChooser(send, "Share sensor session"))
                                },
                                modifier = Modifier.padding(top = 8.dp),
                            ) { Text("Share ${samples.size} samples as CSV") }
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

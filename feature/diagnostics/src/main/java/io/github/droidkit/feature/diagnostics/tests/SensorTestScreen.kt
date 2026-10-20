package io.github.droidkit.feature.diagnostics.tests

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.feature.diagnostics.PhoneDoctorTest

private val axisLabels = listOf("X", "Y", "Z")

@Composable
fun SensorTestScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
    autoResult: Boolean = true,
) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }
    val sensor = remember(test.sensorType) {
        test.sensorType?.let { sensorManager?.getDefaultSensor(it) }
    }

    // Missing hardware is NOT_SUPPORTED, never FAIL — auto-emitted in the
    // guided runner; standalone keeps the notice on screen.
    if (sensor == null) {
        if (autoResult) {
            LaunchedEffect(test.id) {
                onResult(TestResultState.NOT_SUPPORTED, "No ${test.title} sensor reported by SensorManager")
            }
        }
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(test.title, style = MaterialTheme.typography.titleMedium)
            Text(
                "This device reports no ${test.title.lowercase()} hardware." +
                    if (autoResult) " Recording NOT SUPPORTED." else " NOT SUPPORTED.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        return
    }

    var values by remember { mutableStateOf<FloatArray?>(null) }
    var accuracy by remember { mutableStateOf<String?>(null) }

    DisposableEffect(sensor) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                values = event.values.copyOf()
            }
            override fun onAccuracyChanged(s: Sensor?, acc: Int) {
                accuracy = when (acc) {
                    SensorManager.SENSOR_STATUS_UNRELIABLE -> "Unreliable"
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low"
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium"
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High"
                    else -> null
                }
            }
        }
        sensorManager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager?.unregisterListener(listener) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(test.title, style = MaterialTheme.typography.titleMedium)
            Text(
                test.instructions,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Live readings", style = MaterialTheme.typography.titleSmall)
                val v = values
                if (v == null) {
                    Text("Waiting for first sensor event…", style = MaterialTheme.typography.bodyMedium)
                } else {
                    v.forEachIndexed { index, value ->
                        Row {
                            Text(
                                text = axisLabels.getOrElse(index) { "v$index" },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "%.3f".format(value),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
                accuracy?.let {
                    Text(
                        "Reported accuracy: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        TestResultBar(
            onResult = { state, note ->
                val reading = values?.joinToString(", ") { "%.3f".format(it) }
                val detail = listOfNotNull(note, reading?.let { "Last reading: $it" })
                    .joinToString("; ")
                onResult(state, detail.ifBlank { null })
            },
        )
    }
}

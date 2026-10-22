package io.github.droidkit.feature.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import io.github.droidkit.core.ui.components.DroidKitOutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.CopyAllAction
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.formatSectionsForCopy
import io.github.droidkit.core.ui.components.DroidKitTopAppBar

private const val CHECK_WINDOW_MS = 4_000L

/**
 * Registers every live-capable sensor at once for a short window and reports
 * which ones actually produced events. Verdicts are honest: a sensor that
 * exists but stayed silent is marked SILENT, not FAIL — many sensors only
 * emit on change.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorCheckScreen(
    onBack: () -> Unit,
    viewModel: SensorsViewModel = hiltViewModel(),
) {
    var running by remember { mutableStateOf(false) }
    var runId by remember { mutableIntStateOf(0) }
    var finishedAt by remember { mutableLongStateOf(0L) }
    val live = remember { viewModel.liveSensors() }
    val absentTypes = remember {
        SensorsViewModel.LIVE_TYPES.filter { t -> live.none { it.first == t } }
    }
    val eventCounts = remember { mutableStateMapOf<Int, Int>() }
    val lastValues = remember { mutableStateMapOf<Int, FloatArray>() }
    var tick by remember { mutableIntStateOf(0) }

    DisposableEffect(runId) {
        if (!running) return@DisposableEffect onDispose {}
        val sm = viewModel.sensorManager ?: return@DisposableEffect onDispose {}
        val listeners = mutableListOf<SensorEventListener>()
        live.forEach { (type, sensor) ->
            val l = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    eventCounts[type] = (eventCounts[type] ?: 0) + 1
                    lastValues[type] = event.values.copyOf()
                    tick++
                }
                override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}
            }
            sm.registerListener(l, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            listeners += l
        }
        onDispose { listeners.forEach { sm.unregisterListener(it) } }
    }

    // Stop the window after CHECK_WINDOW_MS.
    androidx.compose.runtime.LaunchedEffect(running, runId) {
        if (!running) return@LaunchedEffect
        kotlinx.coroutines.delay(CHECK_WINDOW_MS)
        running = false
        finishedAt = System.currentTimeMillis()
    }

    Scaffold(
        topBar = {
            DroidKitTopAppBar(
                "Sensor check",
                onBack = onBack,
                actions = {
                    CopyAllAction {
                        if (finishedAt == 0L && !running) {
                            null
                        } else {
                            formatSectionsForCopy(
                                listOf(
                                    "Batch check" to live.map { (type, sensor) ->
                                        val events = eventCounts[type] ?: 0
                                        (sensor?.name ?: "Type $type") to buildString {
                                            append(
                                                sensorCheckVerdict(true, events).name +
                                                    " — $events events",
                                            )
                                            lastValues[type]?.let { v ->
                                                append(
                                                    " — last: " + v.take(3)
                                                        .joinToString { "%.2f".format(it) },
                                                )
                                            }
                                        }
                                    } + absentTypes.map { type ->
                                        "Type $type" to "ABSENT — not present on this device"
                                    },
                                ),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SectionCard(title = "Batch check") {
                    Text(
                        "Streams every live-capable sensor for ${CHECK_WINDOW_MS / 1000}s and " +
                            "reports which produced events. A sensor that only fires on change " +
                            "(e.g. proximity) can read SILENT while working fine.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    DroidKitOutlinedButton(
                        onClick = {
                            eventCounts.clear()
                            lastValues.clear()
                            running = true
                            runId++
                        },
                        enabled = !running,
                        modifier = Modifier.padding(top = 8.dp),
                    ) { Text(if (running) "Checking…" else "Run check") }
                }
            }
            if (finishedAt != 0L || running) {
                items(
                    live.map { (type, sensor) -> Triple(type, sensor, true) } +
                        absentTypes.map { Triple(it, null as Sensor?, false) },
                    key = { it.first },
                ) { (type, sensor, present) ->
                    @Suppress("UNUSED_EXPRESSION") tick // recompose on events
                    val events = eventCounts[type] ?: 0
                    val verdict = sensorCheckVerdict(present, events)
                    val windowSec = CHECK_WINDOW_MS / 1000f
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                sensor?.name ?: "Type $type",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                when (verdict) {
                                    SensorCheckVerdict.RESPONDING ->
                                        "$events events · ~%.1f Hz".format(events / windowSec)
                                    SensorCheckVerdict.SILENT ->
                                        "No events in ${CHECK_WINDOW_MS / 1000}s window"
                                    SensorCheckVerdict.ABSENT -> "Not present on this device"
                                } + (lastValues[type]?.let {
                                    " — last: " + it.take(3).joinToString { v -> "%.2f".format(v) }
                                } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            verdict.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = when (verdict) {
                                SensorCheckVerdict.RESPONDING -> MaterialTheme.colorScheme.primary
                                SensorCheckVerdict.ABSENT -> MaterialTheme.colorScheme.onSurfaceVariant
                                SensorCheckVerdict.SILENT -> MaterialTheme.colorScheme.error
                            },
                        )
                    }
                }
            }
        }
    }
}

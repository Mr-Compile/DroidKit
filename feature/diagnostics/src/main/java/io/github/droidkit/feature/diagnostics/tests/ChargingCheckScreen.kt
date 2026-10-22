package io.github.droidkit.feature.diagnostics.tests

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import io.github.droidkit.core.ui.components.DroidKitCard
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
import androidx.core.content.ContextCompat
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.feature.diagnostics.PhoneDoctorTest

private data class BatteryReading(
    val levelPercent: Int,
    val statusLabel: String,
    val plugLabel: String,
    val charging: Boolean,
)

@Composable
fun ChargingCheckScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
    autoResult: Boolean = true,
) {
    val context = LocalContext.current
    var reading by remember { mutableStateOf<BatteryReading?>(null) }
    var plugEvents by remember { mutableStateOf<List<String>>(emptyList()) }

    fun parse(intent: Intent?): BatteryReading? {
        if (intent == null) return null
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        if (level < 0 || scale <= 0) return null
        return BatteryReading(
            levelPercent = (level * 100) / scale,
            statusLabel = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
                else -> "Unknown"
            },
            plugLabel = when {
                plugged and BatteryManager.BATTERY_PLUGGED_AC != 0 -> "AC"
                plugged and BatteryManager.BATTERY_PLUGGED_USB != 0 -> "USB"
                plugged and BatteryManager.BATTERY_PLUGGED_WIRELESS != 0 -> "Wireless"
                else -> "Unplugged"
            },
            charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL,
        )
    }

    // Live plug/unplug detection: the sticky ACTION_BATTERY_CHANGED delivers the
    // current state on registration, then every transition fires the receiver.
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                val next = parse(intent) ?: return
                val prev = reading
                if (prev != null && (prev.charging != next.charging || prev.plugLabel != next.plugLabel)) {
                    plugEvents = plugEvents + (
                        if (next.charging) "Plugged in (${next.plugLabel})" else "Unplugged"
                        )
                }
                reading = next
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        onDispose { context.unregisterReceiver(receiver) }
    }

    LaunchedEffect(Unit) {
        // Wait a beat for the sticky broadcast before declaring unsupported.
        kotlinx.coroutines.delay(1_500)
        if (reading == null && autoResult) {
            onResult(TestResultState.NOT_SUPPORTED, "Battery status broadcast unavailable")
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(test.title, style = MaterialTheme.typography.titleMedium)
            Text(
                test.instructions,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        DroidKitCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val r = reading
                if (r == null) {
                    Text(
                        "Battery broadcast unavailable — " +
                            if (autoResult) "recorded NOT SUPPORTED." else "NOT SUPPORTED.",
                    )
                } else {
                    Row { Text("Level", Modifier.weight(1f)); Text("${r.levelPercent}%") }
                    Row { Text("Status", Modifier.weight(1f)); Text(r.statusLabel) }
                    Row { Text("Plug", Modifier.weight(1f)); Text(r.plugLabel) }
                    Text(
                        "Live — updates as soon as a charger connects or disconnects.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    if (plugEvents.isNotEmpty()) {
                        plugEvents.takeLast(6).forEach {
                            Text(it, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        if (!autoResult) {
            reading?.let { r ->
                AiSummaryCard(
                    screenTitle = test.title,
                    fields = mapOf(
                        "Level" to "${r.levelPercent}%",
                        "Status" to r.statusLabel,
                        "Plug" to r.plugLabel,
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        TestResultBar(
            onResult = { state, note ->
                val r = reading
                val detail = listOfNotNull(
                    note,
                    r?.let { "Level ${it.levelPercent}%, ${it.statusLabel}, plug ${it.plugLabel}" },
                ).joinToString("; ")
                onResult(state, detail.ifBlank { null })
            },
        )
    }
}

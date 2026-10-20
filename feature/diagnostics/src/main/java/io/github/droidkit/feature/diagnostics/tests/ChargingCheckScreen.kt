package io.github.droidkit.feature.diagnostics.tests

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    var refreshes by remember { mutableIntStateOf(0) }

    fun read(): BatteryReading? {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return null
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

    LaunchedEffect(Unit) {
        val r = read()
        reading = r
        if (r == null && autoResult) {
            onResult(TestResultState.NOT_SUPPORTED, "Battery status broadcast unavailable")
        }
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
                    Button(
                        onClick = { reading = read(); refreshes++ },
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text("Refresh reading")
                    }
                }
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

package io.github.droidkit.feature.battery

import android.os.PowerManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryScreen(viewModel: BatteryViewModel = hiltViewModel()) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val powerSave = (context.getSystemService(android.content.Context.POWER_SERVICE) as? PowerManager)
        ?.isPowerSaveMode

    Scaffold(topBar = { TopAppBar(title = { Text("Battery Lab") }) }) { padding ->
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
                SectionCard(title = "Charge") {
                    InfoRow("Level", status.levelPct.takeIf { it >= 0f }?.let { "${it.toInt()}%" })
                    InfoRow("Status", status.statusText)
                    InfoRow("Source", status.pluggedText)
                }
            }
            item {
                SectionCard(title = "Conditions") {
                    InfoRow("Temperature", status.temperatureC.takeIf { it >= 0f }?.let { "%.1f °C".format(it) })
                    InfoRow("Voltage", status.voltageMv.takeIf { it > 0 }?.let { "$it mV" })
                    InfoRow("Technology", status.technology)
                    InfoRow("Health (as reported)", status.healthText)
                    if (status.temperatureC >= 45f) {
                        Text(
                            "Battery unusually warm — let device cool",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
            item {
                SectionCard(title = "Power") {
                    InfoRow(
                        "Current now",
                        status.currentNowUa?.let { "${it / 1000} mA (instantaneous)" },
                    )
                    InfoRow("Battery saver", powerSave?.toString())
                }
            }
        }
    }
}

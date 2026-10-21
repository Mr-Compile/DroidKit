package io.github.droidkit.feature.sensors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState

/** Entry point for the `sensors.live` tool — lists only sensors that support live streaming. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveSensorPickerScreen(
    nav: NavHostController,
    viewModel: SensorsViewModel = hiltViewModel(),
) {
    val sensors = viewModel.probe.sensors.filter { SensorsViewModel.LIVE_TYPES.contains(it.type) }

    Scaffold(topBar = { DroidKitTopAppBar("Live Sensor Monitor", onBack = { nav.navigateUp() }) }) { padding ->
        if (sensors.isEmpty()) {
            Column(Modifier.padding(padding)) {
                EmptyState("No sensors on this device support live monitoring.")
            }
        } else {
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
                    OutlinedButton(
                        onClick = { nav.navigate("sensors/check") { launchSingleTop = true } },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Run batch sensor check") }
                }
                items(sensors, key = { it.type }) { s ->
                    Card(
                        onClick = { nav.navigate("sensors/live/${s.type}") { launchSingleTop = true } },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(s.name, style = MaterialTheme.typography.titleMedium)
                            InfoRow("Type", "${s.stringType} (${s.type})")
                            InfoRow("Max range", "${s.maxRange} ${SensorsViewModel.unitFor(s.type)}".trim())
                        }
                    }
                }
            }
        }
    }
}

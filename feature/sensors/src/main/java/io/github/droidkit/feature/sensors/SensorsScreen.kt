package io.github.droidkit.feature.sensors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.ui.CopyAllAction
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.formatSectionsForCopy
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsScreen(
    nav: NavHostController,
    viewModel: SensorsViewModel = hiltViewModel(),
) {
    val sensors = viewModel.probe.sensors

    Scaffold(
        topBar = {
            DroidKitTopAppBar(
                "Sensor Lab",
                onBack = { nav.navigateUp() },
                actions = {
                    CopyAllAction {
                        sensors.takeIf { it.isNotEmpty() }?.let { list ->
                            formatSectionsForCopy(
                                list.map { s ->
                                    s.name to listOf(
                                        "Vendor" to s.vendor,
                                        "Type" to "${s.stringType} (${s.type})",
                                        "Version" to s.version.toString(),
                                        "Max range" to "${s.maxRange} " +
                                            "${SensorsViewModel.unitFor(s.type)}".trim(),
                                        "Resolution" to s.resolution.toString(),
                                        "Power" to "${s.powerMa} mA",
                                        "Wake-up" to s.isWakeUp.toString(),
                                    )
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (sensors.isEmpty()) {
            Column(Modifier.padding(padding)) {
                EmptyState("No sensors reported by the platform.")
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
                    AiSummaryCard(
                        screenTitle = "Sensor inventory",
                        fields = mapOf(
                            "Sensors reported" to sensors.size.toString(),
                            "Sensor names" to sensors.joinToString { it.name },
                        ),
                    )
                }
                items(sensors.size) { i ->
                    val s = sensors[i]
                    val live = SensorsViewModel.LIVE_TYPES.contains(s.type)
                    SectionCard(title = s.name) {
                        InfoRow("Vendor", s.vendor)
                        InfoRow("Type", "${s.stringType} (${s.type})")
                        InfoRow("Version", s.version.toString())
                        InfoRow("Max range", "${s.maxRange} ${SensorsViewModel.unitFor(s.type)}".trim())
                        InfoRow("Resolution", s.resolution.toString())
                        InfoRow("Power", "${s.powerMa} mA")
                        InfoRow("Wake-up", s.isWakeUp.toString())
                        if (live) {
                            Text(
                                "Tap card to open live monitor",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable { nav.navigate("sensors/live/${s.type}") { launchSingleTop = true } },
                            )
                        }
                    }
                }
            }
        }
    }
}

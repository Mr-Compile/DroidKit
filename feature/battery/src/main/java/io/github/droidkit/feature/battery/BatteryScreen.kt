package io.github.droidkit.feature.battery

import android.os.PowerManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.DroidKitTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryScreen(
    onBack: () -> Unit,
    onOpenPermissions: () -> Unit,
    viewModel: BatteryViewModel = hiltViewModel(),
) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val estimate by viewModel.estimate.collectAsStateWithLifecycle()
    val elevatedProvider by viewModel.elevatedProvider.collectAsStateWithLifecycle()
    val measured by viewModel.measured.collectAsStateWithLifecycle()
    val measuring by viewModel.measuring.collectAsStateWithLifecycle()
    val measureFailed by viewModel.measureFailed.collectAsStateWithLifecycle()
    val measureUnsupported by viewModel.measureUnsupported.collectAsStateWithLifecycle()
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

    Scaffold(topBar = { DroidKitTopAppBar("Battery Lab", onBack = onBack) }) { padding ->
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
                SectionCard(title = "Health") {
                    InfoRow("Cycle count (reported)", status.cycleCount?.toString() ?: "Not exposed")
                    InfoRow(
                        "Charge in pack",
                        status.chargeCounterUa?.let { "${it / 1000} mAh" } ?: "Not exposed",
                    )
                    val m = measured
                    if (m != null) {
                        InfoRow("Fuel-gauge node", m.node)
                        InfoRow("Max capacity (measured)", m.chargeFullUa?.let { "${it / 1000} mAh" })
                        InfoRow("Design capacity", m.chargeFullDesignUa?.let { "${it / 1000} mAh" })
                        InfoRow("Health (measured)", m.healthPct?.let { "%.0f%%".format(it) })
                        InfoRow("Cycles (fuel gauge)", m.cycleCount?.toString())
                    } else if (measureUnsupported) {
                        Text(
                            "No fuel-gauge data exposed on this device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else if (measureFailed) {
                        Text(
                            "Elevated read failed — check the provider in Permission Center",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    if (elevatedProvider != null) {
                        TextButton(
                            onClick = { viewModel.measureHealth() },
                            enabled = !measuring,
                        ) { Text(if (measuring) "Reading fuel gauge…" else "Measure via elevated access") }
                    } else {
                        Text(
                            "A measured capacity % needs the kernel fuel gauge via " +
                                "Shizuku, wireless ADB, or root — it is not exposed to apps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = onOpenPermissions) { Text("Open Permission Center") }
                    }
                }
            }
            item {
                SectionCard(title = "Capacity estimate") {
                    Text(
                        "Tracks real charge flowing into the pack across sessions " +
                            "— an estimate, not a measured value.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    InfoRow("Samples", estimate.samples.toString())
                    InfoRow(
                        "Estimated capacity",
                        estimate.estimatedFullUa?.let { "≈${it / 1000} mAh" }
                            ?: "Collecting — needs ≥20% of tracked charge",
                    )
                    var designInput by rememberSaveable { mutableStateOf("") }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = designInput,
                            onValueChange = { designInput = it.filter(Char::isDigit).take(5) },
                            label = { Text("Design capacity (mAh)") },
                            placeholder = { Text(estimate.designMah?.toString() ?: "e.g. 5000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = {
                                viewModel.setDesignCapacity(designInput.toIntOrNull())
                                designInput = ""
                            },
                            enabled = designInput.isNotEmpty(),
                        ) { Text("Save") }
                    }
                    estimate.healthPct?.let {
                        InfoRow("Estimated health", "≈%.0f%% of design capacity".format(it))
                    }
                    if (estimate.samples > 0) {
                        TextButton(onClick = { viewModel.resetEstimate() }) { Text("Reset estimate") }
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
            item {
                AiSummaryCard(
                    screenTitle = "Battery lab",
                    fields = mapOf(
                        "Level" to status.levelPct.takeIf { it >= 0f }?.let { "${it.toInt()}%" },
                        "Status" to status.statusText,
                        "Source" to status.pluggedText,
                        "Temperature" to status.temperatureC.takeIf { it >= 0f }?.let { "%.1f °C".format(it) },
                        "Voltage" to status.voltageMv.takeIf { it > 0 }?.let { "$it mV" },
                        "Technology" to status.technology,
                        "Health (as reported)" to status.healthText,
                        "Cycle count" to status.cycleCount?.toString(),
                        "Health (measured)" to measured?.healthPct?.let { "%.0f%%".format(it) },
                        "Health (estimated)" to estimate.healthPct?.let { "≈%.0f%%".format(it) },
                        "Current now" to status.currentNowUa?.let { "${it / 1000} mA" },
                        "Battery saver" to powerSave?.toString(),
                    ),
                )
            }
        }
    }
}

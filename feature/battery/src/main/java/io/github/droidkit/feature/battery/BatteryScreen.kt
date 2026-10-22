package io.github.droidkit.feature.battery

import android.os.PowerManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import io.github.droidkit.core.ui.components.DroidKitButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import io.github.droidkit.core.ui.components.DroidKitTextField
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlin.math.roundToInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.ui.CopyAllAction
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.formatSectionsForCopy
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
    val currentHistoryMa by viewModel.currentHistoryMa.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.refresh()
                    viewModel.setLiveSampling(true)
                }
                Lifecycle.Event.ON_PAUSE -> viewModel.setLiveSampling(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.setLiveSampling(false)
        }
    }

    val powerSave = (context.getSystemService(android.content.Context.POWER_SERVICE) as? PowerManager)
        ?.isPowerSaveMode
    val batteryManager =
        context.getSystemService(android.content.Context.BATTERY_SERVICE) as? android.os.BatteryManager
    val currentAvgUa = batteryManager
        ?.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
        ?.takeIf { it != Int.MIN_VALUE }

    Scaffold(
        topBar = {
            DroidKitTopAppBar(
                "Battery Lab",
                onBack = onBack,
                actions = {
                    CopyAllAction {
                        formatSectionsForCopy(
                            listOf(
                                "Charge" to listOf(
                                    "Level" to status.levelPct.takeIf { it >= 0f }
                                        ?.let { "${it.toInt()}%" },
                                    "Status" to status.statusText,
                                    "Source" to status.pluggedText,
                                ),
                                "Conditions" to listOf(
                                    "Temperature" to status.temperatureC.takeIf { it >= 0f }
                                        ?.let { "%.1f °C".format(it) },
                                    "Voltage" to status.voltageMv.takeIf { it > 0 }
                                        ?.let { "$it mV" },
                                    "Technology" to status.technology,
                                    "Health (as reported)" to status.healthText,
                                ),
                                "Health" to buildList {
                                    add("Cycle count (reported)" to status.cycleCount?.toString())
                                    add("Charge in pack" to status.chargeCounterUa
                                        ?.let { "${it / 1000} mAh" })
                                    measured?.let { m ->
                                        add("Fuel-gauge node" to m.node)
                                        add("Max capacity (measured)" to m.chargeFullUa
                                            ?.let { "${it / 1000} mAh" })
                                        add("Design capacity" to m.chargeFullDesignUa
                                            ?.let { "${it / 1000} mAh" })
                                        add("Health (measured)" to m.healthPct
                                            ?.let { "%.0f%%".format(it) })
                                        add("Cycles (fuel gauge)" to m.cycleCount?.toString())
                                    }
                                },
                                "Capacity estimate" to listOf(
                                    "Samples" to estimate.samples.toString(),
                                    "Estimated capacity" to (
                                        estimate.estimatedFullUa?.let { "≈${it / 1000} mAh" }
                                            ?: "Collecting — needs ≥20% of tracked charge"
                                        ),
                                    "Estimated health" to estimate.healthPct
                                        ?.let { "≈%.0f%% of design capacity".format(it) },
                                ),
                                "Power" to listOf(
                                    "Current now" to status.currentNowUa
                                        ?.let { "${it / 1000} mA (instantaneous)" },
                                    "Current average" to BatteryInfo.maLabel(currentAvgUa),
                                    "Power (est.)" to BatteryInfo
                                        .powerMw(status.currentNowUa, status.voltageMv)
                                        ?.let { "$it mW" },
                                    "Battery saver" to powerSave?.toString(),
                                ),
                            ),
                        )
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
                    val tempLevel = BatteryInfo.tempLevel(status.temperatureC)
                    BatteryInfo.tempHint(tempLevel)?.let { hint ->
                        Text(
                            hint,
                            color = if (tempLevel == BatteryInfo.TempLevel.HOT) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.tertiary
                            },
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
                                "wireless ADB or root — it is not exposed to apps.",
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
                        DroidKitTextField(
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
                    InfoRow("Current average", BatteryInfo.maLabel(currentAvgUa))
                    InfoRow(
                        "Power (est.)",
                        BatteryInfo.powerMw(status.currentNowUa, status.voltageMv)
                            ?.let { "$it mW" },
                    )
                    InfoRow("Battery saver", powerSave?.toString())
                    val history = currentHistoryMa
                    if (history.size >= 2) {
                        Text(
                            "Live draw — ${history.size} samples @ 2 s",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        val lineColor = MaterialTheme.colorScheme.primary
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .padding(top = 6.dp)
                                .drawBehind {
                                    val pts = BatteryInfo.sparklinePoints(
                                        history, size.width, size.height,
                                    )
                                    for (i in 1 until pts.size) {
                                        drawLine(
                                            lineColor,
                                            Offset(pts[i - 1].first, pts[i - 1].second),
                                            Offset(pts[i].first, pts[i].second),
                                            strokeWidth = 2.dp.toPx(),
                                        )
                                    }
                                },
                        )
                        Text(
                            "min ${history.min().roundToInt()} mA · " +
                                "max ${history.max().roundToInt()} mA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
                        "Current average" to BatteryInfo.maLabel(currentAvgUa),
                        "Battery saver" to powerSave?.toString(),
                    ),
                )
            }
        }
    }
}

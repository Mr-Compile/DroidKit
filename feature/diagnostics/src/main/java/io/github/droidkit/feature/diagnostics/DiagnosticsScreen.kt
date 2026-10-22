package io.github.droidkit.feature.diagnostics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import io.github.droidkit.core.ui.components.DroidKitAlertDialog
import io.github.droidkit.core.ui.components.DroidKitButton
import io.github.droidkit.core.ui.components.DroidKitCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import io.github.droidkit.core.ui.components.DroidKitOutlinedButton
import io.github.droidkit.core.ui.components.DroidKitTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.database.DiagnosticSessionEntity
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.SectionHeader
import io.github.droidkit.core.ui.components.MiniTileIcon
import io.github.droidkit.core.ui.components.StatusDot
import io.github.droidkit.core.ui.components.ToolIcons
import io.github.droidkit.core.ui.theme.DroidKitTheme
import io.github.droidkit.core.ui.theme.testAccent
import java.text.DateFormat
import java.util.Date

/** `.preset` blurb per preset (mockup PDESC). */
private fun presetDescription(preset: DiagnosticPreset): String = when (preset) {
    DiagnosticPreset.FULL ->
        "Every check below, guided end-to-end with a saved report."
    DiagnosticPreset.USED_PHONE ->
        "Focused intake check for a second-hand device — screen, touch, cameras, radios."
}

/** mockup `.preset` — selectable card with check count, label and blurb. */
@Composable
private fun PresetCard(
    preset: DiagnosticPreset,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = DroidKitTheme.colors
    DroidKitCard(
        onClick = onClick,
        modifier = modifier,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (selected) c.panel2 else c.panel,
            contentColor = c.textPrimary,
        ),
        border = BorderStroke(
            1.dp,
            if (selected) c.brandPrimary.copy(alpha = 0.5f) else c.lineNeutral,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "${preset.tests.size} checks",
                style = DroidKitTheme.extraTypography.microLabel,
                color = c.brandSecondary,
            )
            Text(
                preset.label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = c.textPrimary,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
            )
            Text(
                presetDescription(preset),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                ),
                color = c.textSecondary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onStartGuided: () -> Unit,
    onOpenTool: (String) -> Unit,
    onOpenReports: () -> Unit,
    onResumeSession: (String) -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    var preset by remember { mutableStateOf(DiagnosticPreset.FULL) }
    var showIntake by remember { mutableStateOf(false) }
    var resumeCandidate by remember { mutableStateOf<DiagnosticSessionEntity?>(null) }

    Scaffold(topBar = { DroidKitTopAppBar("Diagnose") }) { padding ->
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
                // mockup .segrow of .preset cards — N checks + label + blurb.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiagnosticPreset.entries.forEach { p ->
                        PresetCard(
                            preset = p,
                            selected = preset == p,
                            onClick = { preset = p },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            item {
                DroidKitButton(onClick = { showIntake = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Start ${preset.label}")
                }
            }
            item {
                DroidKitOutlinedButton(onClick = onOpenReports, modifier = Modifier.fillMaxWidth()) {
                    Text("Saved reports")
                }
            }

            item { SectionHeader("Quick tests") }
            PhoneDoctorTests.ALL.groupBy { it.category }.forEach { (category, tests) ->
                item {
                    Text(
                        category.uppercase(),
                        style = DroidKitTheme.extraTypography.microLabel,
                        color = DroidKitTheme.colors.faint,
                        modifier = Modifier.padding(top = 4.dp, start = 2.dp),
                    )
                }
                item {
                    // mockup: one .card holding .trow rows — qic tile + title + ›
                    DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            tests.forEach { test ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onOpenTool(DiagnosticsFeature.testRoute(test.id))
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    MiniTileIcon(
                                        icon = ToolIcons.of(DiagnosticsFeature.testIconKey(test.id)),
                                        accent = testAccent(test.category)
                                            ?: DroidKitTheme.colors.brandPrimary,
                                    )
                                    Text(
                                        test.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                        ),
                                        color = DroidKitTheme.colors.textPrimary,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 10.dp),
                                    )
                                    Text(
                                        "›",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = DroidKitTheme.colors.faint,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { SectionHeader("History") }
            if (sessions.isEmpty()) {
                item { EmptyState("No diagnostic sessions yet.") }
            } else {
                items(sessions, key = { it.id }) { session ->
                    val done = session.overallStatus.equals("COMPLETED", ignoreCase = true)
                    val inProgress = session.overallStatus.equals("IN_PROGRESS", ignoreCase = true)
                    DroidKitCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (inProgress) resumeCandidate = session else onOpenReports()
                        },
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StatusDot(
                                if (done) DroidKitTheme.colors.statusSuccess
                                else DroidKitTheme.colors.statusWarning,
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 10.dp),
                            ) {
                                Text(
                                    DateFormat.getDateTimeInstance()
                                        .format(Date(session.startedAtEpochMs)),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DroidKitTheme.colors.textPrimary,
                                )
                                Text(
                                    listOfNotNull(
                                        if (inProgress) "in progress — tap to resume"
                                        else session.overallStatus.lowercase(),
                                        session.preset,
                                        session.customerLabel,
                                        session.deviceLabel,
                                        "v${session.appVersion}",
                                    ).joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DroidKitTheme.colors.textSecondary,
                                )
                            }
                            Text(
                                "›",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DroidKitTheme.colors.faint,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showIntake) {
        var customer by remember { mutableStateOf("") }
        var device by remember { mutableStateOf("") }
        DroidKitAlertDialog(
            onDismissRequest = { showIntake = false },
            title = { Text("Session intake (optional)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DroidKitTextField(
                        value = customer,
                        onValueChange = { customer = it },
                        label = { Text("Customer / ticket ref") },
                        singleLine = true,
                    )
                    DroidKitTextField(
                        value = device,
                        onValueChange = { device = it },
                        label = { Text("Device label (e.g. shelf #)") },
                        singleLine = true,
                    )
                    Text(
                        "Labels stay on this device and appear on the report.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.armSession(preset, customer, device)
                        showIntake = false
                        onStartGuided()
                    },
                ) { Text("Start") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.armSession(preset, null, null)
                        showIntake = false
                        onStartGuided()
                    },
                ) { Text("Skip") }
            },
        )
    }

    resumeCandidate?.let { session ->
        DroidKitAlertDialog(
            onDismissRequest = { resumeCandidate = null },
            title = { Text("Resume session?") },
            text = {
                Text(
                    "This diagnostic was left in progress — continue where it " +
                        "stopped, or abandon it. Recorded results stay saved either way.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        resumeCandidate = null
                        onResumeSession(session.id)
                    },
                ) { Text("Resume") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.abandonSession(session.id)
                        resumeCandidate = null
                    },
                ) { Text("Abandon") }
            },
        )
    }
}

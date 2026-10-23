package io.github.droidkit.feature.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.droidkit.core.database.TechnicianPresetEntity
import io.github.droidkit.core.ui.components.ConfirmationSheet
import io.github.droidkit.core.ui.components.DroidKitButton
import io.github.droidkit.core.ui.components.DroidKitCard
import io.github.droidkit.core.ui.components.DroidKitOutlinedButton
import io.github.droidkit.core.ui.components.DroidKitTextField
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.RiskLevel
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * Technician preset builder — name a subset of Phone Doctor tests, then pick
 * it on the Diagnose screen. Sessions keep their own copy of the test list,
 * so editing or deleting a preset never rewrites history.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetManagerScreen(
    onBack: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val presets by viewModel.customPresets.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<TechnicianPresetEntity?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<TechnicianPresetEntity?>(null) }

    val editorOpen = creating || editing != null

    Scaffold(
        topBar = {
            DroidKitTopAppBar(
                if (editorOpen) {
                    if (editing == null) "New preset" else "Edit preset"
                } else {
                    "Custom presets"
                },
                onBack = onBack,
            )
        },
    ) { padding ->
        if (editorOpen) {
            PresetEditor(
                initial = editing,
                padding = padding,
                onSave = { name, ids ->
                    viewModel.savePreset(name, ids, editing?.id)
                    editing = null
                    creating = false
                },
                onCancel = {
                    editing = null
                    creating = false
                },
            )
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
                    Text(
                        "Your own diagnostic line-ups — pick the checks a " +
                            "customer device actually needs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DroidKitTheme.colors.textSecondary,
                    )
                }
                if (presets.isEmpty()) {
                    item { EmptyState("No custom presets yet.") }
                } else {
                    items(presets, key = { it.id }) { preset ->
                        DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(
                                    start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp,
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        preset.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = DroidKitTheme.colors.textPrimary,
                                    )
                                    Text(
                                        "${preset.testIds.split(",").filter { it.isNotBlank() }.size} checks",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DroidKitTheme.colors.textSecondary,
                                    )
                                }
                                TextButton(onClick = { editing = preset }) { Text("Edit") }
                                TextButton(onClick = { deleteCandidate = preset }) {
                                    Text(
                                        "Delete",
                                        color = DroidKitTheme.colors.statusError,
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    DroidKitButton(
                        onClick = { creating = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("New preset") }
                }
            }
        }
    }

    deleteCandidate?.let { preset ->
        ConfirmationSheet(
            title = "Delete “${preset.name}”?",
            consequence = "The preset is removed from the picker. Sessions already run with it keep their results.",
            risk = RiskLevel.Minor,
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deletePreset(preset.id)
                deleteCandidate = null
            },
            onDismiss = { deleteCandidate = null },
        )
    }
}

@Composable
private fun PresetEditor(
    initial: TechnicianPresetEntity?,
    padding: PaddingValues,
    onSave: (String, List<String>) -> Unit,
    onCancel: () -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var selected by remember {
        mutableStateOf(
            initial?.testIds?.split(",")?.filter { it.isNotBlank() }?.toSet()
                ?: emptySet(),
        )
    }
    val grouped = remember { PhoneDoctorTests.ALL.groupBy { it.category } }

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
            DroidKitTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Preset name") },
                placeholder = { Text("e.g. Intake quick check") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        grouped.forEach { (category, tests) ->
            item(key = "cat-$category") {
                Text(
                    category.uppercase(),
                    style = DroidKitTheme.extraTypography.microLabel,
                    color = DroidKitTheme.colors.faint,
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp),
                )
            }
            item(key = "group-$category") {
                DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        tests.forEach { test ->
                            val checked = test.id in selected
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(selected = checked) {
                                        selected =
                                            if (checked) selected - test.id
                                            else selected + test.id
                                    }
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = {
                                        selected =
                                            if (checked) selected - test.id
                                            else selected + test.id
                                    },
                                )
                                Text(
                                    test.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DroidKitTheme.colors.textPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DroidKitOutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                ) { Text("Cancel") }
                DroidKitButton(
                    onClick = {
                        onSave(
                            name,
                            // keep catalog order, not tap order
                            PhoneDoctorTests.ALL.map { it.id }.filter { it in selected },
                        )
                    },
                    enabled = name.isNotBlank() && selected.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) { Text("Save (${selected.size})") }
            }
        }
    }
}

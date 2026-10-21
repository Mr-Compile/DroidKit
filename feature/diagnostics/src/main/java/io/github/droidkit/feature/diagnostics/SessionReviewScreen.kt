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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ai.AiAssistant
import io.github.droidkit.core.ai.AiExplainButton
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.ResultBadge
import io.github.droidkit.core.ui.components.SectionHeader
import io.github.droidkit.core.ui.components.ToolIcon
import io.github.droidkit.core.ui.components.ToolIcons
import io.github.droidkit.core.ui.theme.testAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionReviewScreen(
    onRetry: () -> Unit,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: DiagnosticsViewModel,
) {
    val engineState by viewModel.engine.state.collectAsStateWithLifecycle()
    var notes by rememberSaveable { mutableStateOf("") }

    val retryable = engineState.results
        .filterValues { it == TestResultState.FAIL || it == TestResultState.NOT_TESTED }
        .keys
        .toList()

    val counts = engineState.results.values.groupingBy { it }.eachCount()
    val summary = buildList {
        counts[TestResultState.PASS]?.let { add("PASS $it") }
        counts[TestResultState.FAIL]?.let { add("FAIL $it") }
        counts[TestResultState.NOT_TESTED]?.let { add("SKIPPED $it") }
        counts[TestResultState.NOT_SUPPORTED]?.let { add("NOT SUPPORTED $it") }
        counts[TestResultState.PERMISSION_REQUIRED]?.let { add("PERMISSION $it") }
        counts[TestResultState.UNKNOWN]?.let { add("UNKNOWN $it") }
    }.joinToString(" · ")

    val grouped = engineState.results.entries
        .groupBy { PhoneDoctorTests.findById(it.key)?.category ?: "Other" }

    Scaffold(topBar = { DroidKitTopAppBar("Diagnostic review", onBack = onBack) }) { padding ->
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        summary.ifBlank { "No results recorded" },
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            item {
                fun field(id: String, state: TestResultState): Pair<String, String> {
                    val title = PhoneDoctorTests.findById(id)?.title ?: id
                    val note = engineState.notes[id]
                    return title to (state.name + (note?.let { " — $it" } ?: ""))
                }
                val resultFields =
                    engineState.results.entries.associate { (id, state) -> field(id, state) }
                val failFields = engineState.results.entries
                    .filter { it.value == TestResultState.FAIL }
                    .associate { (id, state) -> field(id, state) }
                AiSummaryCard(
                    screenTitle = "Diagnostic review",
                    fields = mapOf("Summary" to summary) + resultFields,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AiExplainButton(
                        assistant = AiAssistant.DIAGNOSTIC_ANALYZER,
                        fields = mapOf("Summary" to summary) + resultFields,
                        modifier = Modifier.weight(1f),
                    )
                    if (counts[TestResultState.FAIL] != null) {
                        AiExplainButton(
                            assistant = AiAssistant.REPAIR_ASSISTANT,
                            fields = mapOf("Summary" to summary) + failFields,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            grouped.forEach { (category, results) ->
                item { SectionHeader(category) }
                items(results, key = { it.key }) { (testId, state) ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val test = PhoneDoctorTests.findById(testId)
                                if (test != null) {
                                    ToolIcon(
                                        ToolIcons.of(DiagnosticsFeature.testIconKey(test.id)),
                                        accent = testAccent(test.category),
                                    )
                                }
                                Text(
                                    test?.title ?: testId,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = if (test != null) 12.dp else 0.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                ResultBadge(state)
                            }
                            engineState.notes[testId]?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Technician notes") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }

            item {
                if (retryable.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            viewModel.retryTests(retryable)
                            onRetry()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Retry ${retryable.size} failed/skipped test(s)")
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.finishSession(notes)
                        onSaved()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save session")
                }
            }
        }
    }
}

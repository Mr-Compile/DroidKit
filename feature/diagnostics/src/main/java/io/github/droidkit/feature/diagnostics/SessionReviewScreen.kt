package io.github.droidkit.feature.diagnostics

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ai.AiAssistant
import io.github.droidkit.core.ai.AiExplainButton
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.core.ui.components.ResultBadge
import io.github.droidkit.core.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionReviewScreen(
    onRetry: () -> Unit,
    onSaved: () -> Unit,
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

    Scaffold(topBar = { TopAppBar(title = { Text("Diagnostic review") }) }) { padding ->
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
                val resultFields = engineState.results.entries.associate { (id, state) ->
                    (PhoneDoctorTests.findById(id)?.title ?: id) to state.name
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AiExplainButton(
                        assistant = AiAssistant.DIAGNOSTIC_ANALYZER,
                        fields = mapOf("Summary" to summary) + resultFields,
                        modifier = Modifier.weight(1f),
                    )
                    if (counts[TestResultState.FAIL] != null) {
                        AiExplainButton(
                            assistant = AiAssistant.REPAIR_ASSISTANT,
                            fields = mapOf("Summary" to summary) + resultFields
                                .filterValues { it == TestResultState.FAIL.name },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            grouped.forEach { (category, results) ->
                item { SectionHeader(category) }
                items(results, key = { it.key }) { (testId, state) ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Text(
                                PhoneDoctorTests.findById(testId)?.title ?: testId,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            ResultBadge(state)
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

package io.github.droidkit.feature.reports

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.database.DiagnosticSessionEntity
import io.github.droidkit.core.database.DiagnosticSessionDao
import io.github.droidkit.core.database.DiagnosticResultDao
import io.github.droidkit.core.database.DeviceSnapshotDao
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.SectionHeader
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CompareResult(
    val before: DiagnosticSessionEntity,
    val after: DiagnosticSessionEntity,
    val transitions: List<TestTransition>,
    val snapshotDiffs: List<SnapshotDiff>,
)

@HiltViewModel
class SessionCompareViewModel @Inject constructor(
    private val sessionDao: DiagnosticSessionDao,
    private val resultDao: DiagnosticResultDao,
    private val snapshotDao: DeviceSnapshotDao,
) : ViewModel() {

    /** Completed sessions, newest first — candidates for before/after pickers. */
    val sessions: StateFlow<List<DiagnosticSessionEntity>> =
        sessionDao.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _result = MutableStateFlow<CompareResult?>(null)
    val result = _result.asStateFlow()

    fun compare(beforeId: String, afterId: String) {
        viewModelScope.launch {
            val before = sessionDao.getById(beforeId) ?: return@launch
            val after = sessionDao.getById(afterId) ?: return@launch
            _result.value = CompareResult(
                before = before,
                after = after,
                transitions = SessionComparator.compare(
                    resultDao.getBySession(beforeId),
                    resultDao.getBySession(afterId),
                ),
                snapshotDiffs = SessionComparator.diffSnapshots(
                    before.deviceSnapshotId?.let { snapshotDao.getById(it) },
                    after.deviceSnapshotId?.let { snapshotDao.getById(it) },
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionCompareScreen(
    onBack: () -> Unit,
    viewModel: SessionCompareViewModel = hiltViewModel(),
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    var beforeId by remember { mutableStateOf<String?>(null) }
    var afterId by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { DroidKitTopAppBar("Compare sessions", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (sessions.size < 2) {
                item {
                    EmptyState("Run at least two diagnostic sessions to compare before/after.")
                }
            } else {
                item { SectionHeader("Before") }
                item {
                    SessionPicker(sessions, beforeId) { beforeId = it }
                }
                item { SectionHeader("After") }
                item {
                    SessionPicker(sessions, afterId) { afterId = it }
                }
                item {
                    androidx.compose.material3.Button(
                        onClick = {
                            val b = beforeId ?: return@Button
                            val a = afterId ?: return@Button
                            if (b != a) viewModel.compare(b, a)
                        },
                        enabled = beforeId != null && afterId != null && beforeId != afterId,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Compare") }
                }

                result?.let { r ->
                    val interesting = r.transitions.filter { it.transition != Transition.UNCHANGED }
                    val unchanged = r.transitions.size - interesting.size

                    item { SectionHeader("Test transitions") }
                    if (interesting.isEmpty()) {
                        item { Text("No state changes between sessions.", style = MaterialTheme.typography.bodyMedium) }
                    }
                    items(interesting, key = { it.testId }) { t ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    t.transition.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = when (t.transition) {
                                        Transition.FIXED, Transition.IMPROVED -> MaterialTheme.colorScheme.primary
                                        Transition.REGRESSED -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                                Column {
                                    Text(t.testId, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "${t.before ?: "—"} → ${t.after ?: "—"}",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                    if (unchanged > 0) {
                        item {
                            Text(
                                "$unchanged tests unchanged.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (r.snapshotDiffs.isNotEmpty()) {
                        item { SectionHeader("Device changes") }
                        items(r.snapshotDiffs, key = { it.label }) { d ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(d.label, style = MaterialTheme.typography.labelLarge)
                                    Text(
                                        "${d.before ?: "—"} → ${d.after ?: "—"}",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionPicker(
    sessions: List<DiagnosticSessionEntity>,
    selected: String?,
    onSelect: (String) -> Unit,
) {
    val df = DateFormat.getDateTimeInstance()
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        sessions.forEach { s ->
            FilterChip(
                selected = selected == s.id,
                onClick = { onSelect(s.id) },
                label = {
                    Text(
                        listOfNotNull(
                            df.format(Date(s.startedAtEpochMs)),
                            s.deviceLabel ?: s.customerLabel,
                        ).joinToString(" · "),
                    )
                },
            )
        }
    }
}

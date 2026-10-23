package io.github.droidkit.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.ai.AiUseCase
import io.github.droidkit.core.ai.describe
import io.github.droidkit.core.database.DeviceSnapshotDao
import io.github.droidkit.core.database.DeviceSnapshotEntity
import io.github.droidkit.core.database.DiagnosticResultDao
import io.github.droidkit.core.database.DiagnosticResultEntity
import io.github.droidkit.core.database.DiagnosticSessionDao
import io.github.droidkit.core.database.DiagnosticSessionEntity
import io.github.droidkit.core.database.SavedReportDao
import io.github.droidkit.core.database.SavedReportEntity
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReportRow(
    val session: DiagnosticSessionEntity,
    val results: List<DiagnosticResultEntity>,
)

enum class StatusFilter { ALL, IN_PROGRESS, COMPLETED, ABANDONED }

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val sessionDao: DiagnosticSessionDao,
    private val resultDao: DiagnosticResultDao,
    private val snapshotDao: DeviceSnapshotDao,
    private val reportDao: SavedReportDao,
    private val aiUseCase: AiUseCase,
) : ViewModel() {

    sealed interface AiSummaryState {
        data object Idle : AiSummaryState
        data object Loading : AiSummaryState
        data class Text(val summary: String) : AiSummaryState
        data class Unavailable(val reason: String) : AiSummaryState
    }

    val aiSummary = MutableStateFlow<AiSummaryState>(AiSummaryState.Idle)

    /** Optional, user-invoked report summary through the AI pipeline. */
    fun summarize(reportText: String) {
        aiSummary.value = AiSummaryState.Loading
        viewModelScope.launch {
            val result = aiUseCase.summarizeReport(reportText)
            aiSummary.value = result.text?.let { AiSummaryState.Text(it) }
                ?: AiSummaryState.Unavailable(
                    result.error?.describe() ?: "AI provider error",
                )
        }
    }

    val statusFilter = MutableStateFlow(StatusFilter.ALL)

    /** Multi-device history — null means "every device/customer". */
    val deviceFilter = MutableStateFlow<String?>(null)
    val customerFilter = MutableStateFlow<String?>(null)

    /** Distinct intake labels present in history, for the filter dropdowns. */
    val deviceLabels: StateFlow<List<String>> =
        sessionDao.observeAll()
            .map { list -> list.mapNotNull { it.deviceLabel }.distinct().sorted() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val customerLabels: StateFlow<List<String>> =
        sessionDao.observeAll()
            .map { list -> list.mapNotNull { it.customerLabel }.distinct().sorted() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val rows: StateFlow<List<ReportRow>> =
        combine(
            sessionDao.observeAll(),
            resultDao.observeAll(),
            statusFilter,
            deviceFilter,
            customerFilter,
        ) { sessions, results, filter, device, customer ->
            sessions
                .filter { filter == StatusFilter.ALL || it.overallStatus == filter.name }
                .filter { device == null || it.deviceLabel == device }
                .filter { customer == null || it.customerLabel == customer }
                .map { s -> ReportRow(s, results.filter { it.sessionId == s.id }) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun session(id: String): DiagnosticSessionEntity? = sessionDao.getById(id)

    suspend fun resultsFor(id: String): List<DiagnosticResultEntity> =
        resultDao.getBySession(id)

    suspend fun snapshotFor(session: DiagnosticSessionEntity): DeviceSnapshotEntity? =
        session.deviceSnapshotId?.let { snapshotDao.getById(it) }

    /** Deletes a session plus its results — a session never leaves orphan rows. */
    fun deleteSession(id: String) {
        viewModelScope.launch {
            resultDao.deleteBySession(id)
            sessionDao.deleteById(id)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            resultDao.deleteAll()
            sessionDao.deleteAll()
            reportDao.deleteAll()
        }
    }

    fun recordSavedReport(sessionId: String, format: String, localUri: String) {
        viewModelScope.launch {
            reportDao.insert(
                SavedReportEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    generatedAtEpochMs = System.currentTimeMillis(),
                    format = format,
                    localUri = localUri,
                ),
            )
        }
    }
}

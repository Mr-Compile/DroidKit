package io.github.droidkit.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.common.TestTitleLookup
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolRegistry
import io.github.droidkit.core.database.DiagnosticResultDao
import io.github.droidkit.core.database.DiagnosticSessionDao
import io.github.droidkit.core.database.ElevatedActionLogDao
import io.github.droidkit.core.database.InsightRange
import io.github.droidkit.core.database.InsightsCalculator
import io.github.droidkit.core.database.NfcTagScanDao
import io.github.droidkit.core.database.SavedReportDao
import io.github.droidkit.core.database.SessionHealth
import io.github.droidkit.core.database.StoragePoint
import io.github.droidkit.core.database.StorageSnapshotDao
import io.github.droidkit.core.database.TestAttention
import io.github.droidkit.core.datastore.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Health section: range-filtered session trend + attention list + storage. */
data class HealthState(
    val range: InsightRange = InsightRange.DAYS_30,
    val sessions: List<SessionHealth> = emptyList(), // ascending by startedAt
    val attention: List<TestAttention> = emptyList(),
    val storage: List<StoragePoint> = emptyList(),
    val snapshotCount: Int = 0,
    val completedCount: Int = 0,
) {
    val latest: SessionHealth? get() = sessions.lastOrNull()
    val previous: SessionHealth? get() = sessions.dropLast(1).lastOrNull()
    val passDeltaVsPrev: Int? get() =
        previous?.let { latest?.pass?.minus(it.pass) }
}

/** Activity section: all-time counts from Room + DataStore counters. */
data class ActivityState(
    val testsRecorded: Int = 0,
    val reportsSaved: Int = 0,
    val nfcScans: Int = 0,
    val elevatedActions: Int = 0,
    val toolsUsed: Int = 0,
    val topTools: List<Pair<Tool, Int>> = emptyList(),
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    sessionDao: DiagnosticSessionDao,
    resultDao: DiagnosticResultDao,
    storageDao: StorageSnapshotDao,
    reportDao: SavedReportDao,
    nfcDao: NfcTagScanDao,
    elevatedDao: ElevatedActionLogDao,
    settings: SettingsRepository,
    private val registry: ToolRegistry,
    testTitles: TestTitleLookup,
) : ViewModel() {

    private val _range = MutableStateFlow(InsightRange.DAYS_30)
    val range: StateFlow<InsightRange> = _range

    fun setRange(r: InsightRange) { _range.value = r }

    val health: StateFlow<HealthState> = combine(
        sessionDao.observeAll(),
        resultDao.observeAll(),
        storageDao.observeAll(),
        _range,
    ) { sessions, results, snapshots, r ->
        val inRange = InsightsCalculator.inRange(sessions, r, System.currentTimeMillis())
        val bySession = results.groupBy { it.sessionId }
        HealthState(
            range = r,
            sessions = inRange.map { s ->
                InsightsCalculator.health(
                    s.id, s.startedAtEpochMs, s.preset, bySession[s.id].orEmpty(),
                )
            },
            attention = InsightsCalculator.attention(
                inRange.flatMap { bySession[it.id].orEmpty() },
                testTitles::titleFor,
            ),
            storage = InsightsCalculator.storageTrend(snapshots),
            snapshotCount = snapshots.size,
            completedCount = sessions.count { it.overallStatus == "COMPLETED" },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HealthState())

    val activity: StateFlow<ActivityState> = combine(
        reportDao.observeCount(),
        nfcDao.observeCount(),
        elevatedDao.observeCount(),
        resultDao.observeAll(),
        settings.toolUseCounts,
    ) { reports, nfc, elevated, results, toolUse ->
        ActivityState(
            testsRecorded = results.size,
            reportsSaved = reports,
            nfcScans = nfc,
            elevatedActions = elevated,
            toolsUsed = toolUse.size,
            topTools = toolUse.entries.sortedByDescending { it.value }.take(3)
                .mapNotNull { (id, n) -> registry.byId(id)?.let { it to n } },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActivityState())
}

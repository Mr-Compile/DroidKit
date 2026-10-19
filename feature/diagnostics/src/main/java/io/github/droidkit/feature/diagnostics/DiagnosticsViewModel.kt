package io.github.droidkit.feature.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.core.datastore.SettingsRepository
import io.github.droidkit.core.database.DeviceSnapshotDao
import io.github.droidkit.core.database.DeviceSnapshotEntity
import io.github.droidkit.core.database.DiagnosticResultDao
import io.github.droidkit.core.database.DiagnosticResultEntity
import io.github.droidkit.core.database.DiagnosticSessionDao
import io.github.droidkit.core.database.DiagnosticSessionEntity
import io.github.droidkit.core.system.DeviceSystemProbe
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val sessionDao: DiagnosticSessionDao,
    private val resultDao: DiagnosticResultDao,
    private val snapshotDao: DeviceSnapshotDao,
    private val probe: DeviceSystemProbe,
    private val settings: SettingsRepository,
) : ViewModel() {

    val sessions: StateFlow<List<DiagnosticSessionEntity>> =
        sessionDao.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val engine = SessionEngine(PhoneDoctorTests.ALL)

    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId = _activeSessionId.asStateFlow()

    /** Intake data armed by the picker screen before navigation to the guided flow. */
    private var pendingPreset = DiagnosticPreset.FULL
    private var pendingCustomer: String? = null
    private var pendingDevice: String? = null

    /** Stages intake values for the next guided run. */
    fun armSession(preset: DiagnosticPreset, customerLabel: String?, deviceLabel: String?) {
        pendingPreset = preset
        pendingCustomer = customerLabel?.ifBlank { null }
        pendingDevice = deviceLabel?.ifBlank { null }
    }

    /** Creates a session + device snapshot and seeds the guided engine. */
    fun startGuided() {
        viewModelScope.launch {
            val preset = pendingPreset
            _activeSessionId.value = createSession(preset)
            engine.restart(preset.tests.map { it.id })
        }
    }

    /** Persists one test result for the active session and advances the engine. */
    fun recordResult(testId: String, state: TestResultState, note: String?) {
        engine.record(state, note)
        val sessionId = _activeSessionId.value ?: return
        viewModelScope.launch {
            resultDao.insert(
                DiagnosticResultEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    testId = testId,
                    category = PhoneDoctorTests.findById(testId)?.category.orEmpty(),
                    state = state.name,
                    measuredDataJson = null,
                    userNote = note,
                    timestampEpochMs = System.currentTimeMillis(),
                ),
            )
        }
    }

    /** Re-queues the given tests for retry; their previous rows stay as history. */
    fun retryTests(testIds: List<String>) = engine.restart(testIds)

    /** Completes the active session with final notes. */
    fun finishSession(notes: String?) {
        val sessionId = _activeSessionId.value ?: return
        viewModelScope.launch {
            sessionDao.complete(
                sessionId = sessionId,
                ts = System.currentTimeMillis(),
                status = "COMPLETED",
                notes = notes?.ifBlank { null },
            )
        }
    }

    private suspend fun createSession(preset: DiagnosticPreset): String {
        val now = System.currentTimeMillis()
        val technician = settings.technicianName.first().ifBlank { null }
        val snapshot = DeviceSnapshotEntity(
            id = UUID.randomUUID().toString(),
            capturedAtEpochMs = now,
            manufacturer = probe.manufacturer,
            model = probe.model,
            device = probe.device,
            product = probe.product,
            androidRelease = probe.androidRelease,
            apiLevel = probe.sdkInt,
            securityPatch = probe.securityPatch,
            primaryAbi = probe.supportedAbis.firstOrNull().orEmpty(),
            totalRamMb = probe.totalRamMb,
            storageTotalBytes = probe.storage.totalBytes,
            storageFreeBytes = probe.storage.freeBytes,
            displaySummary = with(probe.displayInfo) { "${widthPx}x${heightPx} ${densityDpi}dpi ${refreshHz}Hz" },
            capabilitySummary = probe.capabilitySummary(),
        )
        snapshotDao.insert(snapshot)
        val sessionId = UUID.randomUUID().toString()
        sessionDao.insert(
            DiagnosticSessionEntity(
                id = sessionId,
                startedAtEpochMs = now,
                completedAtEpochMs = null,
                deviceSnapshotId = snapshot.id,
                overallStatus = "IN_PROGRESS",
                notes = null,
                appVersion = probe.appVersionName,
                preset = preset.name,
                customerLabel = pendingCustomer,
                deviceLabel = pendingDevice,
                technicianName = technician,
            ),
        )
        return sessionId
    }
}

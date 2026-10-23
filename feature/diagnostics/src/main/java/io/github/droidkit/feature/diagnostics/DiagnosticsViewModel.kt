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
import io.github.droidkit.core.database.TechnicianPresetDao
import io.github.droidkit.core.database.TechnicianPresetEntity
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
    private val presetDao: TechnicianPresetDao,
    private val probe: DeviceSystemProbe,
    private val settings: SettingsRepository,
) : ViewModel() {

    val sessions: StateFlow<List<DiagnosticSessionEntity>> =
        sessionDao.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Technician-authored presets shown alongside the built-ins on the picker. */
    val customPresets: StateFlow<List<TechnicianPresetEntity>> =
        presetDao.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val engine = SessionEngine(PhoneDoctorTests.ALL)

    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId = _activeSessionId.asStateFlow()

    /** Intake data armed by the picker screen before navigation to the guided flow. */
    private var pendingTestIds: List<String> = DiagnosticPreset.FULL.tests.map { it.id }
    private var pendingPresetTag: String = DiagnosticPreset.FULL.name
    private var pendingCustomer: String? = null
    private var pendingDevice: String? = null

    /** Stages intake values for the next guided run. */
    fun armSession(selection: PresetSelection, customerLabel: String?, deviceLabel: String?) {
        pendingTestIds = selection.testIds
        pendingPresetTag = when (selection) {
            is PresetSelection.Builtin -> selection.preset.name
            is PresetSelection.Custom -> selection.preset.name
        }
        pendingCustomer = customerLabel?.ifBlank { null }
        pendingDevice = deviceLabel?.ifBlank { null }
    }

    /** Creates a session + device snapshot and seeds the guided engine. */
    fun startGuided() {
        val testIds = pendingTestIds
        // Restart synchronously so a finished run can't emit done=true while
        // the coroutine below is still persisting the new session.
        engine.restart(testIds)
        viewModelScope.launch {
            _activeSessionId.value = createSession(testIds)
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

    /**
     * Loads a persisted IN_PROGRESS session back into the engine so the guided
     * flow continues at its first unanswered test.
     */
    fun resumeSession(sessionId: String) {
        // Clear synchronously so a stale done=true can't navigate while the
        // session row is still being read.
        engine.reset()
        viewModelScope.launch {
            val session = sessionDao.getById(sessionId)
            if (session == null) {
                _activeSessionId.value = null
                return@launch
            }
            val rows = resultDao.getBySession(sessionId)
            val results = rows.mapNotNull { row ->
                runCatching { TestResultState.valueOf(row.state) }.getOrNull()
                    ?.let { row.testId to it }
            }.toMap()
            val notes = rows.mapNotNull { row ->
                row.userNote?.let { row.testId to it }
            }.toMap()
            // The stored test list is authoritative (survives preset
            // edits/deletes); rows from before v6 fall back to the preset tag.
            val testIds = session.testIds
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?: runCatching { DiagnosticPreset.valueOf(session.preset) }
                    .getOrDefault(DiagnosticPreset.FULL)
                    .tests.map { it.id }
            engine.seed(testIds, results, notes)
            _activeSessionId.value = sessionId
        }
    }

    /** Marks a session ABANDONED; its recorded results stay as history. */
    fun abandonSession(sessionId: String) {
        if (_activeSessionId.value == sessionId) _activeSessionId.value = null
        viewModelScope.launch {
            sessionDao.complete(
                sessionId = sessionId,
                ts = System.currentTimeMillis(),
                status = "ABANDONED",
                notes = null,
            )
        }
    }

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

    // ---------- technician presets -----------------------------------------

    /** Insert a new preset, or update it when [existingId] is given. */
    fun savePreset(name: String, testIds: List<String>, existingId: String? = null) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || testIds.isEmpty()) return
        viewModelScope.launch {
            if (existingId == null) {
                presetDao.insert(
                    TechnicianPresetEntity(
                        id = UUID.randomUUID().toString(),
                        name = trimmed,
                        createdAtEpochMs = System.currentTimeMillis(),
                        testIds = testIds.joinToString(","),
                    ),
                )
            } else {
                presetDao.update(existingId, trimmed, testIds.joinToString(","))
            }
        }
    }

    /** Removing a preset never rewrites sessions recorded with it. */
    fun deletePreset(id: String) {
        viewModelScope.launch { presetDao.deleteById(id) }
    }

    private suspend fun createSession(testIds: List<String>): String {
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
                preset = pendingPresetTag,
                testIds = testIds.joinToString(","),
                customerLabel = pendingCustomer,
                deviceLabel = pendingDevice,
                technicianName = technician,
            ),
        )
        return sessionId
    }
}

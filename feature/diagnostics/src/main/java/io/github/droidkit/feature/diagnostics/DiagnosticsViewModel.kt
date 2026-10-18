package io.github.droidkit.feature.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.database.DeviceSnapshotDao
import io.github.droidkit.core.database.DeviceSnapshotEntity
import io.github.droidkit.core.database.DiagnosticResultDao
import io.github.droidkit.core.database.DiagnosticSessionDao
import io.github.droidkit.core.database.DiagnosticSessionEntity
import io.github.droidkit.core.system.DeviceSystemProbe
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val sessionDao: DiagnosticSessionDao,
    private val resultDao: DiagnosticResultDao,
    private val snapshotDao: DeviceSnapshotDao,
    private val probe: DeviceSystemProbe,
) : ViewModel() {

    val sessions: StateFlow<List<DiagnosticSessionEntity>> =
        sessionDao.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Creates a session + captures a device snapshot (doc flow 4). Guided tests land in Phase 3. */
    fun startSession() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
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
            sessionDao.insert(
                DiagnosticSessionEntity(
                    id = UUID.randomUUID().toString(),
                    startedAtEpochMs = now,
                    completedAtEpochMs = null,
                    deviceSnapshotId = snapshot.id,
                    overallStatus = "IN_PROGRESS",
                    notes = null,
                    appVersion = probe.appVersionName,
                ),
            )
        }
    }
}

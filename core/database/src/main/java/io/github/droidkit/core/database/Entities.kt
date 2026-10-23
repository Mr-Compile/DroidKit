package io.github.droidkit.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "device_snapshots")
data class DeviceSnapshotEntity(
    @PrimaryKey val id: String,
    val capturedAtEpochMs: Long,
    val manufacturer: String,
    val model: String,
    val device: String,
    val product: String,
    val androidRelease: String,
    val apiLevel: Int,
    val securityPatch: String?,
    val primaryAbi: String,
    val totalRamMb: Long,
    val storageTotalBytes: Long,
    val storageFreeBytes: Long,
    val displaySummary: String,
    val capabilitySummary: String,
)

@Entity(tableName = "diagnostic_sessions", indices = [Index("deviceSnapshotId")])
data class DiagnosticSessionEntity(
    @PrimaryKey val id: String,
    val startedAtEpochMs: Long,
    val completedAtEpochMs: Long?,
    val deviceSnapshotId: String?,
    val overallStatus: String, // IN_PROGRESS | COMPLETED | ABANDONED
    val notes: String?,
    val appVersion: String,
    val preset: String = "FULL", // FULL | USED_PHONE | custom preset label
    val customerLabel: String? = null,
    val deviceLabel: String? = null,
    val technicianName: String? = null,
    /**
     * Comma-separated PhoneDoctorTest ids queued at session start — the
     * authoritative test list for resume, independent of how the preset
     * was later edited or deleted. Null on rows written before v6.
     */
    val testIds: String? = null,
)

@Entity(tableName = "diagnostic_results", indices = [Index("sessionId")])
data class DiagnosticResultEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val testId: String,
    val category: String,
    val state: String, // io.github.droidkit.core.common.TestResultState name
    val measuredDataJson: String?,
    val userNote: String?,
    val timestampEpochMs: Long,
)

@Entity(tableName = "saved_reports", indices = [Index("sessionId")])
data class SavedReportEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val generatedAtEpochMs: Long,
    val format: String, // TEXT | PDF | ...
    val localUri: String,
)

/**
 * A user-captured storage snapshot. [volumesData] is a line-delimited encoding
 * of `name|path|state|totalBytes|freeBytes` — decoded by StorageSnapshotCodec.
 */
@Entity(tableName = "storage_snapshots")
data class StorageSnapshotEntity(
    @PrimaryKey val id: String,
    val capturedAtEpochMs: Long,
    val label: String?,
    val volumesData: String,
)

/** Local-only history of scanned NFC tags; tag IDs truncated to last 4 bytes. */
@Entity(tableName = "nfc_tag_scans")
data class NfcTagScanEntity(
    @PrimaryKey val id: String,
    val scannedAtEpochMs: Long,
    val tagIdTail: String?,
    val techList: String,
    val recordSummary: String?,
)

/**
 * A technician-authored diagnostic preset — a named subset of
 * PhoneDoctorTest ids. [testIds] is comma-separated in catalog order;
 * sessions store their own resolved copy, so editing or deleting a preset
 * never rewrites history.
 */
@Entity(tableName = "technician_presets")
data class TechnicianPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAtEpochMs: Long,
    val testIds: String,
)

@Entity(tableName = "elevated_action_log")
data class ElevatedActionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochMs: Long,
    val provider: String, // ProviderKind name
    val command: String,
    val exitCode: Int,
    val stderrSummary: String?,
)

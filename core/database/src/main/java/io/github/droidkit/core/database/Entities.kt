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

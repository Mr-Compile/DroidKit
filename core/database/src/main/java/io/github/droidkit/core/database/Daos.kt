package io.github.droidkit.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosticSessionDao {
    @Insert
    suspend fun insert(session: DiagnosticSessionEntity)

    @Query("UPDATE diagnostic_sessions SET completedAtEpochMs = :ts, overallStatus = :status, notes = :notes WHERE id = :sessionId")
    suspend fun complete(sessionId: String, ts: Long, status: String, notes: String?)

    @Query("SELECT * FROM diagnostic_sessions ORDER BY startedAtEpochMs DESC")
    fun observeAll(): Flow<List<DiagnosticSessionEntity>>

    @Query("SELECT * FROM diagnostic_sessions WHERE id = :sessionId")
    suspend fun getById(sessionId: String): DiagnosticSessionEntity?

    @Query("DELETE FROM diagnostic_sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: String)

    @Query("DELETE FROM diagnostic_sessions")
    suspend fun deleteAll()
}

@Dao
interface DiagnosticResultDao {
    @Insert
    suspend fun insert(result: DiagnosticResultEntity)

    @Query("SELECT * FROM diagnostic_results WHERE sessionId = :sessionId")
    fun observeBySession(sessionId: String): Flow<List<DiagnosticResultEntity>>

    @Query("SELECT * FROM diagnostic_results WHERE sessionId = :sessionId")
    suspend fun getBySession(sessionId: String): List<DiagnosticResultEntity>

    @Query("SELECT * FROM diagnostic_results")
    fun observeAll(): Flow<List<DiagnosticResultEntity>>

    @Query("DELETE FROM diagnostic_results WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: String)

    @Query("DELETE FROM diagnostic_results")
    suspend fun deleteAll()
}

@Dao
interface DeviceSnapshotDao {
    @Insert
    suspend fun insert(snapshot: DeviceSnapshotEntity)

    @Query("SELECT * FROM device_snapshots ORDER BY capturedAtEpochMs DESC")
    fun observeAll(): Flow<List<DeviceSnapshotEntity>>

    @Query("SELECT * FROM device_snapshots WHERE id = :snapshotId")
    suspend fun getById(snapshotId: String): DeviceSnapshotEntity?
}

@Dao
interface SavedReportDao {
    @Insert
    suspend fun insert(report: SavedReportEntity)

    @Query("SELECT * FROM saved_reports ORDER BY generatedAtEpochMs DESC")
    fun observeAll(): Flow<List<SavedReportEntity>>

    @Query("SELECT COUNT(*) FROM saved_reports")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM saved_reports")
    suspend fun deleteAll()
}

@Dao
interface StorageSnapshotDao {
    @Insert
    suspend fun insert(snapshot: StorageSnapshotEntity)

    @Query("SELECT * FROM storage_snapshots ORDER BY capturedAtEpochMs DESC")
    fun observeAll(): Flow<List<StorageSnapshotEntity>>

    @Query("SELECT * FROM storage_snapshots WHERE id = :id")
    suspend fun getById(id: String): StorageSnapshotEntity?

    @Query("DELETE FROM storage_snapshots WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface NfcTagScanDao {
    @Insert
    suspend fun insert(scan: NfcTagScanEntity)

    @Query("SELECT * FROM nfc_tag_scans ORDER BY scannedAtEpochMs DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<NfcTagScanEntity>>

    @Query("SELECT COUNT(*) FROM nfc_tag_scans")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM nfc_tag_scans")
    suspend fun deleteAll()
}

@Dao
interface ElevatedActionLogDao {
    @Insert
    suspend fun insert(entry: ElevatedActionLogEntity)

    @Query("SELECT * FROM elevated_action_log ORDER BY epochMs DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<ElevatedActionLogEntity>>

    @Query("SELECT COUNT(*) FROM elevated_action_log")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM elevated_action_log")
    suspend fun deleteAll()
}

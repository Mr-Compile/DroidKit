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

    @Query("DELETE FROM saved_reports")
    suspend fun deleteAll()
}

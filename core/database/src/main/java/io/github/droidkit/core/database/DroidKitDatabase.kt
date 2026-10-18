package io.github.droidkit.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DeviceSnapshotEntity::class,
        DiagnosticSessionEntity::class,
        DiagnosticResultEntity::class,
        SavedReportEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class DroidKitDatabase : RoomDatabase() {
    abstract fun sessionDao(): DiagnosticSessionDao
    abstract fun resultDao(): DiagnosticResultDao
    abstract fun snapshotDao(): DeviceSnapshotDao
    abstract fun reportDao(): SavedReportDao
}

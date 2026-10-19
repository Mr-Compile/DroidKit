package io.github.droidkit.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DeviceSnapshotEntity::class,
        DiagnosticSessionEntity::class,
        DiagnosticResultEntity::class,
        SavedReportEntity::class,
        ElevatedActionLogEntity::class,
        StorageSnapshotEntity::class,
        NfcTagScanEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
abstract class DroidKitDatabase : RoomDatabase() {
    abstract fun sessionDao(): DiagnosticSessionDao
    abstract fun resultDao(): DiagnosticResultDao
    abstract fun snapshotDao(): DeviceSnapshotDao
    abstract fun reportDao(): SavedReportDao
    abstract fun elevatedLogDao(): ElevatedActionLogDao
    abstract fun storageSnapshotDao(): StorageSnapshotDao
    abstract fun nfcTagScanDao(): NfcTagScanDao
}

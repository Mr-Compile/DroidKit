package io.github.droidkit.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.github.droidkit.core.privileges.ElevatedAuditEntry
import io.github.droidkit.core.privileges.ElevatedAuditSink
import io.github.droidkit.core.privileges.ProviderKind
import javax.inject.Inject
import javax.inject.Singleton

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `elevated_action_log` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`epochMs` INTEGER NOT NULL, " +
                "`provider` TEXT NOT NULL, " +
                "`command` TEXT NOT NULL, " +
                "`exitCode` INTEGER NOT NULL, " +
                "`stderrSummary` TEXT)",
        )
    }
}

/** Persists every elevated execution for the Settings audit card. */
private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `diagnostic_sessions` ADD COLUMN `preset` TEXT NOT NULL DEFAULT 'FULL'")
        db.execSQL("ALTER TABLE `diagnostic_sessions` ADD COLUMN `customerLabel` TEXT")
        db.execSQL("ALTER TABLE `diagnostic_sessions` ADD COLUMN `deviceLabel` TEXT")
        db.execSQL("ALTER TABLE `diagnostic_sessions` ADD COLUMN `technicianName` TEXT")
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `storage_snapshots` (" +
                "`id` TEXT PRIMARY KEY NOT NULL, " +
                "`capturedAtEpochMs` INTEGER NOT NULL, " +
                "`label` TEXT, " +
                "`volumesData` TEXT NOT NULL)",
        )
    }
}

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `nfc_tag_scans` (" +
                "`id` TEXT PRIMARY KEY NOT NULL, " +
                "`scannedAtEpochMs` INTEGER NOT NULL, " +
                "`tagIdTail` TEXT, " +
                "`techList` TEXT NOT NULL, " +
                "`recordSummary` TEXT)",
        )
    }
}

@Singleton
class RoomElevatedAudit @Inject constructor(
    private val dao: ElevatedActionLogDao,
) : ElevatedAuditSink {
    override suspend fun record(entry: ElevatedAuditEntry) {
        dao.insert(
            ElevatedActionLogEntity(
                epochMs = entry.epochMs,
                provider = entry.provider.name,
                command = entry.command,
                exitCode = entry.exitCode,
                stderrSummary = entry.stderrSummary,
            ),
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DroidKitDatabase =
        Room.databaseBuilder(context, DroidKitDatabase::class.java, "droidkit.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()

    @Provides fun sessionDao(db: DroidKitDatabase) = db.sessionDao()
    @Provides fun resultDao(db: DroidKitDatabase) = db.resultDao()
    @Provides fun snapshotDao(db: DroidKitDatabase) = db.snapshotDao()
    @Provides fun reportDao(db: DroidKitDatabase) = db.reportDao()
    @Provides fun elevatedLogDao(db: DroidKitDatabase) = db.elevatedLogDao()
    @Provides fun storageSnapshotDao(db: DroidKitDatabase) = db.storageSnapshotDao()
    @Provides fun nfcTagScanDao(db: DroidKitDatabase) = db.nfcTagScanDao()

    @Provides
    @IntoSet
    fun provideElevatedAuditSink(impl: RoomElevatedAudit): ElevatedAuditSink = impl
}

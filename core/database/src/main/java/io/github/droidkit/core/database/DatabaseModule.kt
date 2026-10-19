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
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides fun sessionDao(db: DroidKitDatabase) = db.sessionDao()
    @Provides fun resultDao(db: DroidKitDatabase) = db.resultDao()
    @Provides fun snapshotDao(db: DroidKitDatabase) = db.snapshotDao()
    @Provides fun reportDao(db: DroidKitDatabase) = db.reportDao()
    @Provides fun elevatedLogDao(db: DroidKitDatabase) = db.elevatedLogDao()

    @Provides
    @IntoSet
    fun provideElevatedAuditSink(impl: RoomElevatedAudit): ElevatedAuditSink = impl
}

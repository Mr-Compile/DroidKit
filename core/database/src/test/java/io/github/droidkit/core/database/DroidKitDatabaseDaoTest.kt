package io.github.droidkit.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** DAO-level tests against a real in-memory Room database (DK-023). */
// Robolectric 4.15.x supports up to SDK 35; the module's test targetSdk is 36.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DroidKitDatabaseDaoTest {

    private lateinit var db: DroidKitDatabase

    @Before
    fun openDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DroidKitDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = db.close()

    private fun session(id: String = "s1") = DiagnosticSessionEntity(
        id = id,
        startedAtEpochMs = 1_000,
        completedAtEpochMs = null,
        deviceSnapshotId = null,
        overallStatus = "IN_PROGRESS",
        notes = null,
        appVersion = "0.8.0",
        preset = "FULL",
    )

    private fun result(id: String, sessionId: String = "s1") = DiagnosticResultEntity(
        id = id,
        sessionId = sessionId,
        testId = "display.color",
        category = "CORE",
        state = "PASS",
        measuredDataJson = null,
        userNote = null,
        timestampEpochMs = 2_000,
    )

    @Test
    fun `session insert, complete, read and delete`() = runBlocking {
        db.sessionDao().insert(session())
        db.resultDao().insert(result("r1"))
        db.resultDao().insert(result("r2"))

        assertEquals(1, db.sessionDao().observeAll().first().size)
        assertEquals("IN_PROGRESS", db.sessionDao().getById("s1")?.overallStatus)

        db.sessionDao().complete("s1", ts = 3_000, status = "COMPLETED", notes = "ok")
        db.sessionDao().getById("s1")!!.let {
            assertEquals("COMPLETED", it.overallStatus)
            assertEquals(3_000L, it.completedAtEpochMs)
            assertEquals("ok", it.notes)
        }

        assertEquals(2, db.resultDao().getBySession("s1").size)
        db.resultDao().deleteBySession("s1")
        assertEquals(0, db.resultDao().observeBySession("s1").first().size)

        db.sessionDao().deleteById("s1")
        assertNull(db.sessionDao().getById("s1"))
    }

    @Test
    fun `results observe all sessions`() = runBlocking {
        db.sessionDao().insert(session("a"))
        db.sessionDao().insert(session("b"))
        db.resultDao().insert(result("r1", "a"))
        db.resultDao().insert(result("r2", "b"))
        assertEquals(2, db.resultDao().observeAll().first().size)
    }

    @Test
    fun `device snapshot insert and read`() = runBlocking {
        db.snapshotDao().insert(
            DeviceSnapshotEntity(
                id = "snap1", capturedAtEpochMs = 5, manufacturer = "m", model = "x",
                device = "d", product = "p", androidRelease = "15", apiLevel = 35,
                securityPatch = null, primaryAbi = "arm64-v8a", totalRamMb = 8192,
                storageTotalBytes = 1L, storageFreeBytes = 1L,
                displaySummary = "1080x2400", capabilitySummary = "{}",
            ),
        )
        assertEquals("x", db.snapshotDao().getById("snap1")?.model)
    }

    @Test
    fun `saved report insert, count and wipe`() = runBlocking {
        db.sessionDao().insert(session())
        db.reportDao().insert(
            SavedReportEntity(
                id = "rep1", sessionId = "s1", generatedAtEpochMs = 4,
                format = "TEXT", localUri = "content://x",
            ),
        )
        assertEquals(1, db.reportDao().observeCount().first())
        db.reportDao().deleteAll()
        assertEquals(0, db.reportDao().observeCount().first())
    }

    @Test
    fun `storage snapshot insert and delete`() = runBlocking {
        db.storageSnapshotDao().insert(
            StorageSnapshotEntity(
                id = "st1", capturedAtEpochMs = 7, label = "before",
                volumesData = "Internal|/data|mounted|100|50",
            ),
        )
        assertEquals("before", db.storageSnapshotDao().getById("st1")?.label)
        db.storageSnapshotDao().deleteById("st1")
        assertNull(db.storageSnapshotDao().getById("st1"))
    }

    @Test
    fun `nfc scans observe recent respects limit`() = runBlocking {
        repeat(5) { i ->
            db.nfcTagScanDao().insert(
                NfcTagScanEntity(
                    id = "n$i", scannedAtEpochMs = i.toLong(),
                    tagIdTail = "ab$i", techList = "NfcA", recordSummary = null,
                ),
            )
        }
        assertEquals(5, db.nfcTagScanDao().observeCount().first())
        assertEquals(3, db.nfcTagScanDao().observeRecent(3).first().size)
        // Most recent first.
        assertEquals("n4", db.nfcTagScanDao().observeRecent(1).first().first().id)
    }

    @Test
    fun `elevated log records and counts`() = runBlocking {
        db.elevatedLogDao().insert(
            ElevatedActionLogEntity(
                epochMs = 9, provider = "SHIZUKU", command = "id",
                exitCode = 0, stderrSummary = null,
            ),
        )
        assertEquals(1, db.elevatedLogDao().observeCount().first())
        assertEquals("id", db.elevatedLogDao().observeRecent(10).first().first().command)
        db.elevatedLogDao().deleteAll()
        assertEquals(0, db.elevatedLogDao().observeCount().first())
    }
}

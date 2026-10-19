package io.github.droidkit.core.database

import io.github.droidkit.core.database.StorageSnapshotEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StorageSnapshotCodecTest {

    private fun snap(vararg v: VolumeStat) = StorageSnapshotEntity(
        id = "s",
        capturedAtEpochMs = 0,
        label = null,
        volumesData = StorageSnapshotCodec.encode(v.toList()),
    )

    private fun vol(path: String, total: Long, free: Long) =
        VolumeStat(name = path.substringAfterLast('/'), path = path, state = "mounted", totalBytes = total, freeBytes = free)

    @Test
    fun `encode decode round trips`() {
        val volumes = listOf(
            vol("/data", 128_000L, 40_000L),
            VolumeStat("SD card", "/mnt/sd", "mounted", 64_000L, 8_000L),
        )
        assertEquals(volumes, StorageSnapshotCodec.decode(StorageSnapshotCodec.encode(volumes)))
    }

    @Test
    fun `diff reports free space delta per volume`() {
        val before = snap(vol("/data", 100, 40), vol("/mnt/sd", 50, 10))
        val after = snap(vol("/data", 100, 32), vol("/mnt/sd", 50, 10))
        val d = StorageSnapshotCodec.diff(before, after).associateBy { it.name }
        assertEquals(-8L, d.getValue("data").freeDeltaBytes)
        assertEquals(0L, d.getValue("sd").freeDeltaBytes)
    }

    @Test
    fun `volume present only after has null before`() {
        val before = snap(vol("/data", 100, 40))
        val after = snap(vol("/data", 100, 40), vol("/mnt/new", 50, 50))
        val d = StorageSnapshotCodec.diff(before, after).associateBy { it.name }
        assertNull(d.getValue("new").beforeFree)
        assertEquals(50L, d.getValue("new").afterFree)
    }

    @Test
    fun `malformed lines skipped`() {
        assertEquals(emptyList<VolumeStat>(), StorageSnapshotCodec.decode("garbage\n1|2"))
    }
}

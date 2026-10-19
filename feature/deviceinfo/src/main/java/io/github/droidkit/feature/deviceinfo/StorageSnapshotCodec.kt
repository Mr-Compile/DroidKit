package io.github.droidkit.feature.deviceinfo

import io.github.droidkit.core.database.StorageSnapshotEntity

data class VolumeStat(
    val name: String,
    val path: String,
    val state: String,
    val totalBytes: Long,
    val freeBytes: Long,
)

data class VolumeDelta(
    val name: String,
    val beforeFree: Long?,
    val afterFree: Long?,
    val beforeTotal: Long?,
    val afterTotal: Long?,
) {
    /** Negative = space was consumed between snapshots. */
    val freeDeltaBytes: Long? get() =
        if (beforeFree != null && afterFree != null) afterFree - beforeFree else null
}

/**
 * Encodes volume rows as `name|path|state|total|free` lines — dependency-free,
 * stable, and unit-testable without JSON libraries.
 */
object StorageSnapshotCodec {

    fun encode(volumes: List<VolumeStat>): String =
        volumes.joinToString("\n") { v ->
            listOf(
                v.name.replace("|", " "),
                v.path,
                v.state,
                v.totalBytes.toString(),
                v.freeBytes.toString(),
            ).joinToString("|")
        }

    fun decode(data: String): List<VolumeStat> =
        data.lineSequence()
            .mapNotNull { line ->
                val p = line.split("|")
                if (p.size < 5) return@mapNotNull null
                VolumeStat(
                    name = p[0],
                    path = p[1],
                    state = p[2],
                    totalBytes = p[3].toLongOrNull() ?: return@mapNotNull null,
                    freeBytes = p[4].toLongOrNull() ?: return@mapNotNull null,
                )
            }
            .toList()

    /** Per-volume delta between two snapshots, keyed on path (stable identity). */
    fun diff(before: StorageSnapshotEntity, after: StorageSnapshotEntity): List<VolumeDelta> {
        val b = decode(before.volumesData).associateBy { it.path }
        val a = decode(after.volumesData).associateBy { it.path }
        return (b.keys + a.keys).distinct().map { path ->
            val bv = b[path]
            val av = a[path]
            VolumeDelta(
                name = av?.name ?: bv?.name ?: path,
                beforeFree = bv?.freeBytes,
                afterFree = av?.freeBytes,
                beforeTotal = bv?.totalBytes,
                afterTotal = av?.totalBytes,
            )
        }
    }
}

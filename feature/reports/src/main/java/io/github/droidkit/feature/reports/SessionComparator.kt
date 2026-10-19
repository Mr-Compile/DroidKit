package io.github.droidkit.feature.reports

import io.github.droidkit.core.database.DeviceSnapshotEntity
import io.github.droidkit.core.database.DiagnosticResultEntity

/** How a single test's state moved between two sessions. */
enum class Transition { FIXED, REGRESSED, IMPROVED, CHANGED, UNCHANGED, NEW, REMOVED }

data class TestTransition(
    val testId: String,
    val category: String,
    val before: String?, // null = not present in the earlier session
    val after: String?,
    val transition: Transition,
)

data class SnapshotDiff(val label: String, val before: String?, val after: String?)

/**
 * Pure before/after comparison of two diagnostic sessions. No Android deps so
 * the transition rules are unit-tested.
 */
object SessionComparator {

    private val GOOD = setOf("PASS")
    private val BAD = setOf("FAIL")

    /** Latest result per test wins when a session retried a test. */
    private fun latest(results: List<DiagnosticResultEntity>): Map<String, DiagnosticResultEntity> =
        results.groupBy { it.testId }
            .mapValues { (_, rows) -> rows.maxBy { it.timestampEpochMs } }

    fun compare(
        before: List<DiagnosticResultEntity>,
        after: List<DiagnosticResultEntity>,
    ): List<TestTransition> {
        val b = latest(before)
        val a = latest(after)
        val ids = (b.keys + a.keys).sorted()
        return ids.map { id ->
            val bs = b[id]?.state
            val as_ = a[id]?.state
            TestTransition(
                testId = id,
                category = (a[id] ?: b[id])?.category.orEmpty(),
                before = bs,
                after = as_,
                transition = when {
                    bs == null -> Transition.NEW
                    as_ == null -> Transition.REMOVED
                    bs == as_ -> Transition.UNCHANGED
                    bs in BAD && as_ in GOOD -> Transition.FIXED
                    bs in GOOD && as_ in BAD -> Transition.REGRESSED
                    bs in BAD && as_ !in GOOD -> Transition.CHANGED
                    bs !in BAD && as_ in GOOD -> Transition.IMPROVED
                    else -> Transition.CHANGED
                },
            )
        }
    }

    /** Field-level diff of two device snapshots; only changed fields returned. */
    fun diffSnapshots(before: DeviceSnapshotEntity?, after: DeviceSnapshotEntity?): List<SnapshotDiff> {
        if (before == null || after == null) return emptyList()
        fun mb(bytes: Long) = "${bytes / (1024 * 1024)} MB"
        return listOf(
            SnapshotDiff("Device", "${before.manufacturer} ${before.model}", "${after.manufacturer} ${after.model}"),
            SnapshotDiff("Android", before.androidRelease, after.androidRelease),
            SnapshotDiff("API level", before.apiLevel.toString(), after.apiLevel.toString()),
            SnapshotDiff("Security patch", before.securityPatch, after.securityPatch),
            SnapshotDiff("RAM", "${before.totalRamMb} MB", "${after.totalRamMb} MB"),
            SnapshotDiff("Storage total", mb(before.storageTotalBytes), mb(after.storageTotalBytes)),
            SnapshotDiff("Storage free", mb(before.storageFreeBytes), mb(after.storageFreeBytes)),
            SnapshotDiff("Display", before.displaySummary, after.displaySummary),
            SnapshotDiff("Providers", before.capabilitySummary, after.capabilitySummary),
        ).filter { it.before != it.after }
    }
}

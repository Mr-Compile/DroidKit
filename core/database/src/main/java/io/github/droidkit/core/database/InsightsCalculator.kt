package io.github.droidkit.core.database

import io.github.droidkit.core.common.TestResultState

/** Time window applied to the health section of the Insights screen. */
enum class InsightRange {
    DAYS_30, ALL;

    fun cutoff(nowEpochMs: Long): Long = when (this) {
        DAYS_30 -> nowEpochMs - 30L * 86_400_000
        ALL -> Long.MIN_VALUE
    }
}

/** Aggregated verdict of one diagnostic session. */
data class SessionHealth(
    val sessionId: String,
    val startedAtEpochMs: Long,
    val preset: String?,
    val pass: Int,
    val fail: Int,
    val skip: Int,
    val total: Int,
) {
    val passRate: Float get() = if (total == 0) 0f else pass.toFloat() / total
    val hasFailure: Boolean get() = fail > 0
}

/** A test that failed or was skipped/not supported across the ranged sessions. */
data class TestAttention(
    val testId: String,
    val title: String,
    val category: String?,
    val fails: Int,
    val skips: Int,
) {
    /** Fails weigh double — a hard failure matters more than a skip. */
    val severity: Int get() = fails * 2 + skips
}

/** Internal-storage free space at one snapshot point (first/internal volume). */
data class StoragePoint(val atEpochMs: Long, val freeBytes: Long)

/**
 * Pure aggregation for the Insights screen and the dashboard summary card —
 * no Android/framework deps so it runs under plain JUnit.
 */
object InsightsCalculator {

    private fun stateOf(raw: String): TestResultState =
        runCatching { TestResultState.valueOf(raw) }.getOrDefault(TestResultState.UNKNOWN)

    fun health(
        sessionId: String,
        startedAtEpochMs: Long,
        preset: String?,
        results: List<DiagnosticResultEntity>,
    ): SessionHealth {
        var pass = 0
        var fail = 0
        results.forEach { r ->
            when (stateOf(r.state)) {
                TestResultState.PASS -> pass++
                TestResultState.FAIL -> fail++
                else -> {}
            }
        }
        return SessionHealth(
            sessionId = sessionId,
            startedAtEpochMs = startedAtEpochMs,
            preset = preset,
            pass = pass,
            fail = fail,
            skip = results.size - pass - fail,
            total = results.size,
        )
    }

    /** COMPLETED sessions inside [range], oldest first. */
    fun inRange(
        sessions: List<DiagnosticSessionEntity>,
        range: InsightRange,
        nowEpochMs: Long,
    ): List<DiagnosticSessionEntity> {
        val cutoff = range.cutoff(nowEpochMs)
        return sessions
            .filter { it.overallStatus == "COMPLETED" && it.startedAtEpochMs >= cutoff }
            .sortedBy { it.startedAtEpochMs }
    }

    /** Top [limit] tests by severity (fails×2 + skips), resolved via [titleFor]. */
    fun attention(
        results: List<DiagnosticResultEntity>,
        titleFor: (String) -> String?,
        limit: Int = 4,
    ): List<TestAttention> =
        results.groupBy { it.testId }.mapNotNull { (testId, rs) ->
            var fails = 0
            var skips = 0
            rs.forEach { r ->
                when (stateOf(r.state)) {
                    TestResultState.FAIL -> fails++
                    TestResultState.PASS -> {}
                    else -> skips++
                }
            }
            if (fails == 0 && skips == 0) null
            else TestAttention(
                testId = testId,
                title = titleFor(testId) ?: testId,
                category = rs.last().category,
                fails = fails,
                skips = skips,
            )
        }.sortedByDescending { it.severity }.take(limit)

    /** Free-space trend of the internal (first) volume across snapshots. */
    fun storageTrend(snapshots: List<StorageSnapshotEntity>): List<StoragePoint> =
        snapshots.sortedBy { it.capturedAtEpochMs }.mapNotNull { s ->
            StorageSnapshotCodec.decode(s.volumesData).firstOrNull()
                ?.let { StoragePoint(s.capturedAtEpochMs, it.freeBytes) }
        }
}

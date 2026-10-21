package io.github.droidkit.feature.packages

/** Pure presentation helpers for Package Controls — JVM-testable. */
object PackagesInfo {

    /**
     * Human summary of a finished batch run. [results] pairs package names
     * with success; failures are named so the user knows exactly which
     * packages need a retry.
     */
    fun batchSummary(label: String, results: List<Pair<String, Boolean>>): String {
        val failed = results.filterNot { it.second }.map { it.first }
        val okCount = results.size - failed.size
        return when {
            results.isEmpty() -> "Nothing to do."
            failed.isEmpty() -> "$label succeeded for ${okCount} package${s(okCount)}."
            okCount == 0 -> "$label failed for ${failed.joinToString()}."
            else -> "$label: $okCount succeeded, ${failed.size} failed " +
                "(${failed.joinToString()})."
        }
    }

    /** Title/body copy for a batch confirmation — one dialog for the set. */
    fun batchConfirm(label: String, count: Int): Pair<String, String> =
        "$label $count package${s(count)}?" to
            "The action runs on each selected package in turn — " +
            "reversible, but apps may misbehave until you undo it."

    private fun s(n: Int) = if (n == 1) "" else "s"
}

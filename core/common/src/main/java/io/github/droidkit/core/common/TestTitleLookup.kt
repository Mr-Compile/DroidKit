package io.github.droidkit.core.common

/**
 * Maps a diagnostic `testId` to a display title. The diagnostics feature owns
 * the catalog ([PhoneDoctorTests]); it binds an implementation via Hilt so
 * other features can label results without a feature-to-feature dependency.
 */
fun interface TestTitleLookup {
    fun titleFor(testId: String): String?
}

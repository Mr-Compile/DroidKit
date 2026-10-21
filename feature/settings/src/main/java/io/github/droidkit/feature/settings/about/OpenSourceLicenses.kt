package io.github.droidkit.feature.settings.about

/**
 * Third-party components shipped inside DroidKit, derived from the build
 * files (gradle/libs.versions.toml, build-logic convention plugins and
 * module dependencies). Keep this list in sync when dependencies change.
 * Test-only libraries (JUnit, Robolectric, Espresso) are not distributed
 * with the app and are intentionally omitted.
 */
data class LicenseEntry(
    val component: String,
    val copyrightHolder: String,
    val license: String,
)

object OpenSourceLicenses {
    private const val APACHE_2 = "Apache License 2.0"

    val entries: List<LicenseEntry> = listOf(
        LicenseEntry("Kotlin", "JetBrains", APACHE_2),
        LicenseEntry("Jetpack Compose (UI, Material3, Icons)", "Google", APACHE_2),
        LicenseEntry("AndroidX Activity, Core, Lifecycle, Navigation", "Google", APACHE_2),
        LicenseEntry("AndroidX Room", "Google", APACHE_2),
        LicenseEntry("AndroidX DataStore", "Google", APACHE_2),
        LicenseEntry("AndroidX CameraX", "Google", APACHE_2),
        LicenseEntry("Dagger Hilt", "Google", APACHE_2),
        LicenseEntry("kotlinx.coroutines", "JetBrains", APACHE_2),
    )
}

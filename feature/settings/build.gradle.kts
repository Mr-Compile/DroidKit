plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.settings"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(projects.core.datastore)
    implementation(projects.core.ai)
    implementation(projects.core.database)
    implementation(projects.core.ui)
}

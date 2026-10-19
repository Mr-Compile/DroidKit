plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.dashboard"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.privileges)
    implementation(projects.core.system)
    implementation(projects.core.datastore)
    implementation(projects.core.ui)
}

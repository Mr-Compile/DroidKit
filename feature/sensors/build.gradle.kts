plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.sensors"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.system)
    implementation(projects.core.ui)
}

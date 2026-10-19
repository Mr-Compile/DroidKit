plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.display"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.ui)
}

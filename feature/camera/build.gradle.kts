plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.camera"
}

dependencies {
    implementation(projects.core.ai)
    implementation(projects.core.common)
    implementation(projects.core.ui)
}

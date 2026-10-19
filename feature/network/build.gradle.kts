plugins {
    id("droidkit.android.library.compose")
}

android {
    namespace = "io.github.droidkit.feature.network"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(projects.core.ui)
}

plugins {
    id("droidkit.android.library.compose")
}

android {
    namespace = "io.github.droidkit.feature.sensors"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(projects.core.ui)
}

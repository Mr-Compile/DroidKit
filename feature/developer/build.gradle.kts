plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.developer"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(projects.core.ui)
}

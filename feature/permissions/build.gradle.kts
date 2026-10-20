plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.permissions"
}

dependencies {
    implementation(projects.core.ai)
    implementation(projects.core.common)
    implementation(projects.core.permissions)
    implementation(projects.core.privileges)
    implementation(projects.core.ui)
}

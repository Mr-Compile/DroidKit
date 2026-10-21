plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.files"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(projects.core.ui)
    implementation(libs.androidx.documentfile)
    testImplementation(libs.androidx.junit)
}

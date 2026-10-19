plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.nfc"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(projects.core.ui)
    implementation(projects.core.ai)

    testImplementation(libs.androidx.junit)
}

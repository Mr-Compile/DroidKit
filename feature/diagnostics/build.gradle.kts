plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.diagnostics"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(projects.core.database)
    implementation(projects.core.system)
    implementation(projects.core.ui)
    implementation(projects.core.ai)
    implementation(projects.core.permissions)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    testImplementation(libs.androidx.junit)
}

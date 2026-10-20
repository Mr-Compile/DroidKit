plugins {
    id("droidkit.android.library.compose")
}

android {
    namespace = "io.github.droidkit.core.ui"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.permissions)
    implementation(projects.core.privileges)
    implementation(libs.androidx.activity.compose)
    testImplementation(libs.androidx.junit)
}

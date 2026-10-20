plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}
android {
    namespace = "io.github.droidkit.core.ai"
}
dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(projects.core.permissions)
    implementation(projects.core.ui)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.androidx.junit)
}

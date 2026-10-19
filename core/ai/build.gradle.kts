plugins {
    id("droidkit.android.library")
    id("droidkit.hilt")
}
android {
    namespace = "io.github.droidkit.core.ai"
}
dependencies {
    implementation(projects.core.common)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.androidx.junit)
}

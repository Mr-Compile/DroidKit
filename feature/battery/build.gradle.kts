plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.battery"
}

dependencies {
    implementation(projects.core.ai)
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(projects.core.system)
    implementation(projects.core.ui)

    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.androidx.junit)
}

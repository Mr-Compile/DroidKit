plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.insights"
}

dependencies {
    implementation(projects.core.ai)
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.system)
    implementation(projects.core.ui)

    testImplementation(libs.androidx.junit)
}

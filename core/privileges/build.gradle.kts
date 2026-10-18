plugins {
    id("droidkit.android.library")
}

android {
    namespace = "io.github.droidkit.core.privileges"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.permissions)
    implementation(libs.javax.inject)
    testImplementation(libs.androidx.junit)
}

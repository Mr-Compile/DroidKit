plugins {
    id("droidkit.android.library")
}

android {
    namespace = "io.github.droidkit.core.common"
}

dependencies {
    implementation(libs.javax.inject)
    testImplementation(libs.androidx.junit)
}

plugins {
    id("droidkit.android.library")
}

android {
    namespace = "io.github.droidkit.core.permissions"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.javax.inject)
}

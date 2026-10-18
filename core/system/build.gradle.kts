plugins {
    id("droidkit.android.library")
}

android {
    namespace = "io.github.droidkit.core.system"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.javax.inject)
}

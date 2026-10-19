plugins {
    id("droidkit.android.library")
}

android {
    namespace = "io.github.droidkit.core.datastore"
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.javax.inject)
}

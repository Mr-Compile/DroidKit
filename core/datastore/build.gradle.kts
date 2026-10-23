plugins {
    id("droidkit.android.library")
}

android {
    namespace = "io.github.droidkit.core.datastore"
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.javax.inject)

    testImplementation(libs.androidx.junit)
}

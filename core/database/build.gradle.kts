plugins {
    id("droidkit.android.library")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.core.database"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.coroutines.android)
    ksp(libs.androidx.room.compiler)
}

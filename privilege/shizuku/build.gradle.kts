plugins {
    id("droidkit.android.library")
}

android {
    namespace = "io.github.droidkit.privilege.shizuku"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(libs.kotlinx.coroutines.android)
    implementation("dev.rikka.shizuku:api:13.1.5")
}

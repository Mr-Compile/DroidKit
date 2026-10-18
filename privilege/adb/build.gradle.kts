plugins {
    id("droidkit.android.library")
}

android {
    namespace = "io.github.droidkit.privilege.adb"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
}

plugins {
    id("droidkit.android.library")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.core.database"
}

// Persist Room schemas so upgrade paths are testable via MigrationTestHelper.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.coroutines.android)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.androidx.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)

    androidTestImplementation(libs.androidx.room.testing)
}

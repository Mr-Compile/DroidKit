plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("com.android.tools.build:gradle:9.0.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20")
    implementation("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:2.2.20")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.59.2")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.2.20-2.0.3")
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "droidkit.android.library"
            implementationClass = "droidkit.AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "droidkit.android.library.compose"
            implementationClass = "droidkit.AndroidLibraryComposeConventionPlugin"
        }
        register("androidApplication") {
            id = "droidkit.android.application"
            implementationClass = "droidkit.AndroidApplicationConventionPlugin"
        }
        register("hilt") {
            id = "droidkit.hilt"
            implementationClass = "droidkit.HiltConventionPlugin"
        }
    }
}

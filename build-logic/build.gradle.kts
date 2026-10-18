plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly("com.android.tools.build:gradle:9.0.1")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.20")
    compileOnly("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:2.3.20")
    compileOnly("com.google.dagger:hilt-android-gradle-plugin:2.52")
    compileOnly("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.3.20-2.0.3")
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

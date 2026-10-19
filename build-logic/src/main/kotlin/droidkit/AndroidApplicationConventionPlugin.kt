package droidkit

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        // AGP 9 has built-in Kotlin; org.jetbrains.kotlin.android is forbidden
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<ApplicationExtension> {
            compileSdk = 36
            defaultConfig {
                applicationId = "io.github.droidkit"
                minSdk = 28
                targetSdk = 36
                versionCode = 8
                versionName = "0.8.0"
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            lint {
                baseline = file("lint-baseline.xml")
                abortOnError = true
                checkReleaseBuilds = false
            }
            buildFeatures { compose = true; buildConfig = true }
        }

        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            }
        }

        dependencies {
            add("implementation", platform("androidx.compose:compose-bom:2025.09.01"))
            add("implementation", "androidx.core:core-ktx:1.17.0")
            add("implementation", "androidx.activity:activity-compose:1.11.0")
            add("implementation", "androidx.compose.ui:ui")
            add("implementation", "androidx.compose.ui:ui-tooling-preview")
            add("implementation", "androidx.compose.material3:material3")
            add("implementation", "androidx.compose.material:material-icons-extended")
            add("implementation", "androidx.navigation:navigation-compose:2.9.4")
            add("implementation", "androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
            add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
            add("debugImplementation", "androidx.compose.ui:ui-tooling")
        }
    }
}

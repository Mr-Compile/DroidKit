package droidkit

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        // AGP 9 has built-in Kotlin; org.jetbrains.kotlin.android is forbidden (issuetracker 438678642)

        extensions.configure<LibraryExtension> {
            compileSdk = 36
            defaultConfig {
                minSdk = 28
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            testOptions {
                targetSdk = 36
                unitTests.isIncludeAndroidResources = true
            }
            lint {
                // lint-baseline.xml freezes existing findings; only new violations fail.
                baseline = file("lint-baseline.xml")
                abortOnError = true
                checkReleaseBuilds = false
            }
        }

        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            }
        }

        // Hilt's ASM transform generates empty unit-test source dirs in modules
        // without tests; Gradle 9 fails hard on undiscovered tests by default.
        tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
            failOnNoDiscoveredTests.set(false)
        }
    }
}

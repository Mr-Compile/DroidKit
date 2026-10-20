package droidkit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import com.android.build.api.dsl.LibraryExtension
import org.gradle.kotlin.dsl.configure

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("droidkit.android.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<LibraryExtension> {
            buildFeatures { compose = true }
        }

        dependencies {
            add("implementation", platform("androidx.compose:compose-bom:2025.09.01"))
            add("implementation", "androidx.compose.ui:ui")
            add("implementation", "androidx.compose.ui:ui-tooling-preview")
            add("implementation", "androidx.compose.material3:material3")
            add("implementation", "androidx.compose.material3:material3-adaptive-navigation-suite")
            add("implementation", "androidx.compose.material3.adaptive:adaptive")
            add("implementation", "androidx.compose.material3.adaptive:adaptive-layout")
            add("implementation", "androidx.compose.material3.adaptive:adaptive-navigation")
            add("implementation", "androidx.compose.material:material-icons-extended")
            add("implementation", "androidx.navigation:navigation-compose:2.9.4")
            add("implementation", "androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
            add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
            add("debugImplementation", "androidx.compose.ui:ui-tooling")
        }
    }
}

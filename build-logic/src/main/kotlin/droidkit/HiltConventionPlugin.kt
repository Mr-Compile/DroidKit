package droidkit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("dagger.hilt.android.plugin")

        dependencies {
            add("implementation", "com.google.dagger:hilt-android:2.59.2")
            add("ksp", "com.google.dagger:hilt-android-compiler:2.59.2")
            add("implementation", "androidx.hilt:hilt-navigation-compose:1.3.0")
            add("implementation", "androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
        }
    }
}

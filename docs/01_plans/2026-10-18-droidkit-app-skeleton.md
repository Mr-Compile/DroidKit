# DroidKit App Skeleton Implementation Plan

> **STATUS: IMPLEMENTED** — all 21 tasks executed on branch `feature/j05-j06-skeleton`
> (2026-10-19). `:app:assembleDebug` builds `app-debug.apk`; `testDebugUnitTest`
> passes all 12 unit tests (ToolRegistryTest 4, CapabilityManagerTest 8).
> Documented deviations: Kotlin 2.2.20 + KSP 2.2.20-2.0.3 (no KSP for 2.3.x),
> Hilt 2.59.2 (AGP 9 support), `android.disallowKotlinSourceSets=false` (AGP 9
> KSP workaround), manifest theme `Theme.Material.NoActionBar`,
> `failOnNoDiscoveredTests=false` for empty unit-test source sets.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Scaffold the complete multi-module DroidKit project (31 Gradle modules) with working foundation services and all feature screens present as capability-gated stubs.

**Architecture:** UI -> ViewModel -> Repository -> Data Source -> Capability Provider. Features declare tool requirements; `CapabilityManager` resolves them against hardware/API/permission state and the provider chain (Standard -> Shizuku -> ADB -> Root). Stub tools render precise `Blocked`/`NeedsSetup` states instead of dead ends.

**Tech Stack:** Kotlin, Jetpack Compose (Material3), Hilt, Navigation Compose, Room, DataStore, Coroutines/Flow.

## Global Constraints

- Spec: `docs/00_specs/2026-10-18-droidkit-app-skeleton-design.md`
- Package root: `io.github.droidkit`; every module namespace = `io.github.droidkit.<module path with dots>` (e.g. `io.github.droidkit.core.common`)
- applicationId: `io.github.droidkit`; minSdk 28, compileSdk 36, targetSdk 36
- Gradle 9.1.0 (`all` dist, already cached on this machine), AGP 9.0.1, Kotlin 2.3.20
- JDK for builds: `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"` (Android Studio bundled JBR; no system Java on PATH)
- Gradle binary before wrapper exists: `"$HOME/.gradle/wrapper/dists/gradle-9.1.0-all/7wzd0jkjit61aq2p43wpjgij9/gradle-9.1.0/bin/gradle"`
- Version fallbacks if resolution fails: KSP `${kotlin}-2.0.3` -> try `-2.0.4`/`-2.0.2`; Kotlin 2.3.20 -> 2.2.20 (then KSP `2.2.20-2.0.3`); compose-bom 2025.09.01 -> 2025.08.00; hilt 2.52 -> 2.51.1
- Git Bash environment; project root `C:\wamp64\www\DroidKit` = `/c/wamp64/www/DroidKit`
- No direct Shizuku/ADB/root checks in feature code — features only declare requirements
- `local.properties` is gitignored and contains `sdk.dir`
- Commit after every task

---

### Task 1: Root project scaffolding

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `local.properties`

**Interfaces:**
- Produces: version catalog accessors used by every later module; `includeBuild("build-logic")` so convention plugin ids resolve; `projects.core.common`-style typed project accessors (`TYPESAFE_PROJECT_ACCESSORS`)

- [x] **Step 1: Write `settings.gradle.kts`**

```kotlin
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DroidKit"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

include(":core:common")
include(":core:ui")
include(":core:database")
include(":core:datastore")
include(":core:permissions")
include(":core:system")
include(":core:privileges")

include(":privilege:standard")
include(":privilege:shizuku")
include(":privilege:adb")
include(":privilege:root")

include(":feature:dashboard")
include(":feature:tools")
include(":feature:diagnostics")
include(":feature:battery")
include(":feature:display")
include(":feature:audio")
include(":feature:camera")
include(":feature:sensors")
include(":feature:network")
include(":feature:bluetooth")
include(":feature:usb")
include(":feature:nfc")
include(":feature:apps")
include(":feature:permissions")
include(":feature:appops")
include(":feature:packages")
include(":feature:adb")
include(":feature:developer")
include(":feature:reports")
include(":feature:deviceinfo")
include(":feature:settings")
```

- [x] **Step 2: Write root `build.gradle.kts`**

```kotlin
// Root build file. All build behavior lives in build-logic convention plugins.
tasks.register("moduleCount") {
    doLast {
        println("Modules: ${rootProject.subprojects.size}")
    }
}
```

- [x] **Step 3: Write `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
org.gradle.caching=true
org.gradle.parallel=true
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
```

- [x] **Step 4: Write `gradle/libs.versions.toml`**

```toml
[versions]
agp = "9.0.1"
kotlin = "2.3.20"
ksp = "2.3.20-2.0.3"
hilt = "2.52"
composeBom = "2025.09.01"
navigation = "2.9.4"
lifecycle = "2.9.4"
activity = "1.11.0"
coreKtx = "1.17.0"
room = "2.7.2"
datastore = "1.1.7"
coroutines = "1.10.2"
work = "2.10.5"
hiltNavigation = "1.3.0"
serialization = "1.8.1"
junit = "4.13.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigation" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
androidx-work-runtime = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-gradle-plugin = { group = "com.google.dagger", name = "hilt-android-gradle-plugin", version.ref = "hilt" }
androidx-junit = { group = "junit", name = "junit", version.ref = "junit" }

[plugins]
# convention plugins are applied by literal id; no catalog entries needed for them
```

- [x] **Step 5: Write `local.properties`**

```properties
sdk.dir=C:\\Users\\myrus\\AppData\\Local\\Android\\Sdk
```

- [x] **Step 6: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle.properties gradle/libs.versions.toml
git commit -m "chore: root gradle scaffolding and version catalog"
```

---

### Task 2: build-logic convention plugins

**Files:**
- Create: `build-logic/settings.gradle.kts`, `build-logic/build.gradle.kts`
- Create: `build-logic/src/main/kotlin/droidkit/AndroidLibraryConventionPlugin.kt`
- Create: `build-logic/src/main/kotlin/droidkit/AndroidLibraryComposeConventionPlugin.kt`
- Create: `build-logic/src/main/kotlin/droidkit/AndroidApplicationConventionPlugin.kt`
- Create: `build-logic/src/main/kotlin/droidkit/HiltConventionPlugin.kt`

**Interfaces:**
- Produces: plugin ids `droidkit.android.library`, `droidkit.android.library.compose`, `droidkit.android.application`, `droidkit.hilt` — applied by literal id in every module `build.gradle.kts`

- [x] **Step 1: `build-logic/settings.gradle.kts`**

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
rootProject.name = "build-logic"
```

- [x] **Step 2: `build-logic/build.gradle.kts`**

```kotlin
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
```

- [x] **Step 3: `AndroidLibraryConventionPlugin.kt`**

```kotlin
package droidkit

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")

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
        }

        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            }
        }
    }
}
```

- [x] **Step 4: `AndroidLibraryComposeConventionPlugin.kt`**

```kotlin
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
            add("implementation", "androidx.compose.material:material-icons-extended")
            add("implementation", "androidx.navigation:navigation-compose:2.9.4")
            add("implementation", "androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
            add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
            add("debugImplementation", "androidx.compose.ui:ui-tooling")
        }
    }
}
```

- [x] **Step 5: `AndroidApplicationConventionPlugin.kt`**

```kotlin
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
        pluginManager.apply("org.jetbrains.kotlin.android")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<ApplicationExtension> {
            compileSdk = 36
            defaultConfig {
                applicationId = "io.github.droidkit"
                minSdk = 28
                targetSdk = 36
                versionCode = 1
                versionName = "0.1.0"
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
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
```

- [x] **Step 6: `HiltConventionPlugin.kt`**

```kotlin
package droidkit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("dagger.hilt.android.plugin")

        dependencies {
            add("implementation", "com.google.dagger:hilt-android:2.52")
            add("ksp", "com.google.dagger:hilt-android-compiler:2.52")
            add("implementation", "androidx.hilt:hilt-navigation-compose:1.3.0")
            add("implementation", "androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
        }
    }
}
```

- [x] **Step 7: Verify build-logic compiles**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
"$HOME/.gradle/wrapper/dists/gradle-9.1.0-all/7wzd0jkjit61aq2p43wpjgij9/gradle-9.1.0/bin/gradle" help
```

Expected: build-logic compiles; `help` succeeds (module build files don't exist yet — that fails later only when evaluated; `help` configures root only). If `help` fails on missing included modules, temporarily comment `include` lines for uncreated modules, or create all module dirs with empty `build.gradle.kts`+manifest stubs first — prefer creating the stub dirs.

- [x] **Step 8: Generate the Gradle wrapper**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
"$HOME/.gradle/wrapper/dists/gradle-9.1.0-all/7wzd0jkjit61aq2p43wpjgij9/gradle-9.1.0/bin/gradle" wrapper --gradle-version 9.1.0 --distribution-type all
```

Expected: `gradle/wrapper/gradle-wrapper.jar` + `gradlew` + `gradlew.bat` created. All later tasks use `./gradlew`.

- [x] **Step 9: Commit**

```bash
git add build-logic gradle/wrapper gradlew gradlew.bat
git commit -m "chore: build-logic convention plugins and gradle wrapper"
```

---

### Task 3: `:core:common` — tool model + registry (TDD)

**Files:**
- Create: `core/common/build.gradle.kts`, `core/common/src/main/AndroidManifest.xml`
- Create: `core/common/src/main/java/io/github/droidkit/core/common/Tool.kt`
- Create: `core/common/src/main/java/io/github/droidkit/core/common/ToolRegistry.kt`
- Test: `core/common/src/test/java/io/github/droidkit/core/common/ToolRegistryTest.kt`

**Interfaces:**
- Produces (consumed everywhere): `Tool`, `ToolCategory`, `SafetyLevel`, `ToolPhase`, `Capability`, `TestResultState`, `FeatureContribution`, `ToolRegistry(tools)` with `tools`, `search(String)`, `byCategory(ToolCategory)`, `byId(String)`

- [x] **Step 1: `core/common/build.gradle.kts`**

```kotlin
plugins {
    id("droidkit.android.library")
}

android {
    namespace = "io.github.droidkit.core.common"
}

dependencies {
    testImplementation(libs.androidx.junit)
}
```

- [x] **Step 2: `core/common/src/main/AndroidManifest.xml`** (identical minimal manifest in every library module)

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest />
```

- [x] **Step 3: Write the failing test**

```kotlin
package io.github.droidkit.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolRegistryTest {

    private val contributions = setOf(
        object : FeatureContribution {
            override val tools = listOf(
                Tool("a", "USB Devices", "List USB", ToolCategory.HARDWARE, "usb"),
                Tool("b", "Sensor List", "Show sensors", ToolCategory.DEVICE, "sensors"),
            )
        },
        object : FeatureContribution {
            override val tools = listOf(
                Tool("c", "AppOps", "App ops", ToolCategory.APPS, "appops"),
            )
        },
    )

    private val registry = ToolRegistry(contributions)

    @Test
    fun `registry flattens contributions sorted by title`() {
        assertEquals(listOf("AppOps", "Sensor List", "USB Devices"), registry.tools.map { it.title })
    }

    @Test
    fun `search matches title case-insensitively`() {
        assertEquals(listOf("a"), registry.search("usb").map { it.id })
    }

    @Test
    fun `byCategory filters`() {
        assertEquals(1, registry.byCategory(ToolCategory.APPS).size)
    }

    @Test
    fun `byId returns tool or null`() {
        assertEquals("USB Devices", registry.byId("a")?.title)
        assertTrue(registry.byId("zzz") == null)
    }
}
```

- [x] **Step 4: `Tool.kt`**

```kotlin
package io.github.droidkit.core.common

enum class SafetyLevel { SAFE, MINOR, SYSTEM_CHANGE, HIGH_IMPACT, ADVANCED }

enum class ToolCategory { DEVICE, DIAGNOSTICS, APPS, CONNECTIVITY, HARDWARE, DEVELOPER, REPORTS }

enum class ToolPhase { V1, V2, FUTURE }

enum class Capability { STANDARD_API, RUNTIME_PERMISSION, SHIZUKU_SERVICE, ADB_SHELL, ROOT }

enum class TestResultState { PASS, FAIL, NOT_TESTED, NOT_SUPPORTED, PERMISSION_REQUIRED, UNKNOWN }

data class Tool(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val route: String,
    val safetyLevel: SafetyLevel = SafetyLevel.SAFE,
    val requiredCapabilities: Set<Capability> = setOf(Capability.STANDARD_API),
    val requiredPermissions: List<String> = emptyList(),
    val requiredFeatures: Set<String> = emptySet(),
    val minSdk: Int? = null,
    val phase: ToolPhase = ToolPhase.V1,
    val keywords: Set<String> = emptySet(),
)

interface FeatureContribution {
    val tools: List<Tool>
}
```

- [x] **Step 5: `ToolRegistry.kt`**

```kotlin
package io.github.droidkit.core.common

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor(
    contributions: Set<@JvmSuppressWildcards FeatureContribution>,
) {
    val tools: List<Tool> = contributions
        .flatMap { it.tools }
        .sortedBy { it.title }

    fun search(query: String): List<Tool> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return tools
        return tools.filter {
            it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.keywords.any { k -> k.lowercase().contains(q) }
        }
    }

    fun byCategory(category: ToolCategory): List<Tool> = tools.filter { it.category == category }

    fun byId(id: String): Tool? = tools.firstOrNull { it.id == id }
}
```

- [x] **Step 6: Run tests**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :core:common:testDebugUnitTest
```

Expected: 4 tests PASS.

- [x] **Step 7: Commit**

```bash
git add core/common
git commit -m "feat(core): tool model and registry"
```

---

### Task 4: `:core:system` — hardware/API probe

**Files:**
- Create: `core/system/build.gradle.kts`, `core/system/src/main/AndroidManifest.xml`
- Create: `core/system/src/main/java/io/github/droidkit/core/system/DeviceSystemProbe.kt`

**Interfaces:**
- Produces: `DeviceSystemProbe(context)` — `sdkInt`, `manufacturer`, `model`, `device`, `supportedAbis: List<String>`, `totalRamMb: Long`, `storage: StorageInfo`, `sensors: List<ProbeSensor>`, `cameraIds: List<String>`, `batteryCapacityPct: Float`, `displayInfo: DisplayInfo`, `hasFeature(String): Boolean`, `summary(): String`

- [x] **Step 1: `core/system/build.gradle.kts` + manifest**

```kotlin
plugins {
    id("droidkit.android.library")
}

android {
    namespace = "io.github.droidkit.core.system"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
```

Manifest: same `<manifest />` stub.

- [x] **Step 2: `DeviceSystemProbe.kt`**

```kotlin
package io.github.droidkit.core.system

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import javax.inject.Inject
import javax.inject.Singleton

data class ProbeSensor(val name: String, val vendor: String, val type: Int)

data class StorageInfo(val totalBytes: Long, val freeBytes: Long)

data class DisplayInfo(val widthPx: Int, val heightPx: Int, val densityDpi: Int, val refreshHz: Float)

@Singleton
class DeviceSystemProbe @Inject constructor(
    private val context: Context,
) {
    val sdkInt: Int = Build.VERSION.SDK_INT
    val manufacturer: String = Build.MANUFACTURER
    val model: String = Build.MODEL
    val device: String = Build.DEVICE
    val product: String = Build.PRODUCT
    val androidRelease: String = Build.VERSION.RELEASE
    val securityPatch: String? = Build.VERSION.SECURITY_PATCH
    val supportedAbis: List<String> = Build.SUPPORTED_ABIS.toList()

    val appVersionName: String by lazy {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "unknown"
        }
    }

    val totalRamMb: Long by lazy {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        info.totalMem / (1024 * 1024)
    }

    val storage: StorageInfo by lazy {
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        StorageInfo(stat.totalBytes, stat.availableBytes)
    }

    val sensors: List<ProbeSensor> by lazy {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        sm?.getSensorList(android.hardware.Sensor.TYPE_ALL)
            ?.map { ProbeSensor(it.name, it.vendor, it.type) }
            ?: emptyList()
    }

    val cameraIds: List<String> by lazy {
        val cm = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        try {
            cm?.cameraIdList?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Battery charge counter percent; -1f when the platform does not expose it. */
    val batteryCapacityPct: Float
        get() {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val v = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            return if (v in 0..100) v.toFloat() else -1f
        }

    val displayInfo: DisplayInfo
        get() {
            val dm: DisplayMetrics = context.resources.displayMetrics
            val display = context.display
            return DisplayInfo(
                widthPx = dm.widthPixels,
                heightPx = dm.heightPixels,
                densityDpi = dm.densityDpi,
                refreshHz = display?.refreshRate ?: 0f,
            )
        }

    fun hasFeature(feature: String): Boolean =
        context.packageManager.hasSystemFeature(feature)

    fun capabilitySummary(): String = buildString {
        append("nfc=").append(hasFeature("android.hardware.nfc"))
        append(" bt=").append(hasFeature("android.hardware.bluetooth"))
        append(" usbHost=").append(hasFeature("android.hardware.usb.host"))
        append(" telephony=").append(hasFeature("android.hardware.telephony"))
        append(" cameras=").append(cameraIds.size)
        append(" sensors=").append(sensors.size)
    }

    fun summary(): String = buildString {
        append("$manufacturer $model")
        append(" | Android $androidRelease (API $sdkInt)")
        append(" | ABI ${supportedAbis.firstOrNull().orEmpty()}")
        append(" | RAM ${totalRamMb}MB")
        append(" | Storage free ${storage.freeBytes / (1024 * 1024 * 1024)}GB/${storage.totalBytes / (1024 * 1024 * 1024)}GB")
    }
}
```

The constructor takes a plain `Context` — `:core:system` carries no Hilt dependency. `:app` passes `@ApplicationContext` when providing it (Task 19).

- [x] **Step 3: Verify compile**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :core:system:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [x] **Step 4: Commit**

```bash
git add core/system
git commit -m "feat(core): device system probe"
```

---

### Task 5: `:core:permissions` — permission checking

**Files:**
- Create: `core/permissions/build.gradle.kts`, `core/permissions/src/main/AndroidManifest.xml`
- Create: `core/permissions/src/main/java/io/github/droidkit/core/permissions/PermissionChecker.kt`
- Create: `core/permissions/src/main/java/io/github/droidkit/core/permissions/PermissionManager.kt`

**Interfaces:**
- Produces: `fun interface PermissionChecker { fun isGranted(permission: String): Boolean }`; `class PermissionManager(context) : PermissionChecker` with `isGranted(String)`, `manifestPermissions(): List<String>`

- [x] **Step 1: build file + manifest** (library plugin, namespace `io.github.droidkit.core.permissions`, dep `androidx.core:core-ktx`)

```kotlin
plugins {
    id("droidkit.android.library")
}

android {
    namespace = "io.github.droidkit.core.permissions"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
```

- [x] **Step 2: `PermissionChecker.kt`**

```kotlin
package io.github.droidkit.core.permissions

fun interface PermissionChecker {
    fun isGranted(permission: String): Boolean
}
```

- [x] **Step 3: `PermissionManager.kt`**

```kotlin
package io.github.droidkit.core.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
    private val context: Context,
) : PermissionChecker {

    override fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /** Permissions declared in the merged manifest — what the app may request. */
    fun manifestPermissions(): List<String> = try {
        context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions?.toList() ?: emptyList()
    } catch (e: PackageManager.NameNotFoundException) {
        emptyList()
    }
}
```

- [x] **Step 4: Compile + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :core:permissions:compileDebugKotlin
git add core/permissions
git commit -m "feat(core): permission manager"
```

---

### Task 6: `:core:privileges` — capability resolution (TDD)

**Files:**
- Create: `core/privileges/build.gradle.kts`, `core/privileges/src/main/AndroidManifest.xml`
- Create: `core/privileges/src/main/java/io/github/droidkit/core/privileges/CapabilityProvider.kt`
- Create: `core/privileges/src/main/java/io/github/droidkit/core/privileges/ToolResolution.kt`
- Create: `core/privileges/src/main/java/io/github/droidkit/core/privileges/CapabilityManager.kt`
- Test: `core/privileges/src/test/java/io/github/droidkit/core/privileges/CapabilityManagerTest.kt`

**Interfaces:**
- Consumes: `Tool`, `Capability`, `PermissionChecker`, `DeviceSystemProbe` (constructor-injected, faked in tests via subclass — see note below)
- Produces: `ProviderKind`, `ProviderStatus`, `CapabilityProvider`, `BlockedReason`, `ToolResolution`, `CapabilityManager(providers, probe, permissionChecker)` with `resolve(Tool): ToolResolution`, `providerStatuses(): Map<ProviderKind, ProviderStatus>`

Note for tests: `DeviceSystemProbe` is a concrete class needing a `Context`. Make `hasFeature`/`sdkInt` testable by extracting a minimal seam: CapabilityManager should depend on two lambdas instead — `sdkCheck: (Int) -> Boolean` (isSdkAtLeast) and `featureCheck: (String) -> Boolean` (hasFeature). Provide them from the probe in `:app`. This keeps `:core:privileges` free of Android dependencies and unit-testable. `PermissionChecker` is already an interface — fake it directly.

- [x] **Step 1: build file**

```kotlin
plugins {
    id("droidkit.android.library")
}

android {
    namespace = "io.github.droidkit.core.privileges"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.permissions)
    testImplementation(libs.androidx.junit)
}
```

(No `core:system` dep — the probe seam is injected as lambdas. Manifest: `<manifest />`.)

- [x] **Step 2: Write the failing test**

```kotlin
package io.github.droidkit.core.privileges

import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.permissions.PermissionChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityManagerTest {

    private val standardProvider = object : CapabilityProvider {
        override val kind = ProviderKind.STANDARD
        override val status = ProviderStatus.Ready
        override val capabilities = setOf(Capability.STANDARD_API, Capability.RUNTIME_PERMISSION)
    }

    private val shizukuStub = object : CapabilityProvider {
        override val kind = ProviderKind.SHIZUKU
        override val status = ProviderStatus.Unavailable("Coming in Phase 7")
        override val capabilities = setOf(Capability.SHIZUKU_SERVICE)
    }

    private fun manager(
        permissions: Set<String> = emptySet(),
        features: Set<String> = emptySet(),
        sdk: Int = 36,
    ) = CapabilityManager(
        providers = setOf(standardProvider, shizukuStub),
        sdkInt = sdk,
        hasFeature = { it in features },
        permissionChecker = PermissionChecker { it in permissions },
    )

    @Test
    fun `standard tool with no requirements is ready`() {
        val tool = Tool("t", "T", "", ToolCategory.DEVICE, "r")
        assertEquals(ToolResolution.Ready, manager().resolve(tool))
    }

    @Test
    fun `missing permission produces blocked reason`() {
        val tool = Tool("t", "T", "", ToolCategory.DEVICE, "r",
            requiredCapabilities = setOf(Capability.RUNTIME_PERMISSION),
            requiredPermissions = listOf("android.permission.CAMERA"))
        val res = manager().resolve(tool)
        assertTrue(res is ToolResolution.Blocked)
        assertTrue((res as ToolResolution.Blocked).reasons.any { it is BlockedReason.MissingPermission })
    }

    @Test
    fun `granted permission is ready`() {
        val tool = Tool("t", "T", "", ToolCategory.DEVICE, "r",
            requiredPermissions = listOf("android.permission.CAMERA"))
        assertEquals(ToolResolution.Ready, manager(permissions = setOf("android.permission.CAMERA")).resolve(tool))
    }

    @Test
    fun `missing hardware feature blocks`() {
        val tool = Tool("t", "T", "", ToolCategory.HARDWARE, "r",
            requiredFeatures = setOf("android.hardware.nfc"))
        val res = manager().resolve(tool)
        assertTrue((res as ToolResolution.Blocked).reasons.any { it is BlockedReason.MissingHardware })
    }

    @Test
    fun `minSdk gate blocks on old devices`() {
        val tool = Tool("t", "T", "", ToolCategory.DEVICE, "r", minSdk = 35)
        val res = manager(sdk = 28).resolve(tool)
        assertTrue((res as ToolResolution.Blocked).reasons.any { it is BlockedReason.RequiresApi })
    }

    @Test
    fun `shizuku capability resolves to needs-provider with stub status`() {
        val tool = Tool("t", "T", "", ToolCategory.APPS, "r",
            requiredCapabilities = setOf(Capability.SHIZUKU_SERVICE))
        val res = manager().resolve(tool)
        val reason = (res as ToolResolution.Blocked).reasons.filterIsInstance<BlockedReason.NeedsProvider>().single()
        assertEquals(ProviderKind.SHIZUKU, reason.kind)
        assertTrue(reason.status is ProviderStatus.Unavailable)
    }

    @Test
    fun `capability with no provider reports unavailability`() {
        val tool = Tool("t", "T", "", ToolCategory.DEVELOPER, "r",
            requiredCapabilities = setOf(Capability.ROOT))
        val res = CapabilityManager(
            providers = setOf(standardProvider),
            sdkInt = 36,
            hasFeature = { true },
            permissionChecker = PermissionChecker { true },
        ).resolve(tool)
        val reason = (res as ToolResolution.Blocked).reasons.filterIsInstance<BlockedReason.NeedsProvider>().single()
        assertTrue(reason.status is ProviderStatus.Unavailable)
    }

    @Test
    fun `providerStatuses maps all providers`() {
        val statuses = manager().providerStatuses()
        assertEquals(ProviderStatus.Ready, statuses[ProviderKind.STANDARD])
        assertTrue(statuses[ProviderKind.SHIZUKU] is ProviderStatus.Unavailable)
    }
}
```

- [x] **Step 3: `CapabilityProvider.kt`**

```kotlin
package io.github.droidkit.core.privileges

import io.github.droidkit.core.common.Capability

enum class ProviderKind { STANDARD, SHIZUKU, ADB, ROOT }

sealed interface ProviderStatus {
    data object Ready : ProviderStatus
    data class NeedsSetup(val instructions: String) : ProviderStatus
    data class Unavailable(val reason: String) : ProviderStatus
}

interface CapabilityProvider {
    val kind: ProviderKind
    val status: ProviderStatus
    val capabilities: Set<Capability>
}
```

- [x] **Step 4: `ToolResolution.kt`**

```kotlin
package io.github.droidkit.core.privileges

sealed interface BlockedReason {
    data class MissingPermission(val permission: String) : BlockedReason
    data class MissingHardware(val feature: String) : BlockedReason
    data class RequiresApi(val minSdk: Int) : BlockedReason
    data class NeedsProvider(val kind: ProviderKind, val status: ProviderStatus) : BlockedReason
}

sealed interface ToolResolution {
    data object Ready : ToolResolution
    data class Blocked(val reasons: List<BlockedReason>) : ToolResolution
}
```

- [x] **Step 5: `CapabilityManager.kt`**

```kotlin
package io.github.droidkit.core.privileges

import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.permissions.PermissionChecker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapabilityManager @Inject constructor(
    providers: Set<@JvmSuppressWildcards CapabilityProvider>,
    private val sdkInt: Int,
    private val hasFeature: (String) -> Boolean,
    private val permissionChecker: PermissionChecker,
) {
    private val providersByKind: Map<ProviderKind, CapabilityProvider> =
        providers.associateBy { it.kind }
    private val providersByCapability: Map<Capability, List<CapabilityProvider>> =
        providers.flatMap { p -> p.capabilities.map { c -> c to p } }
            .groupBy({ it.first }, { it.second })

    fun providerStatuses(): Map<ProviderKind, ProviderStatus> =
        providersByKind.mapValues { it.value.status }

    fun resolve(tool: Tool): ToolResolution {
        val reasons = mutableListOf<BlockedReason>()

        tool.minSdk?.let { required ->
            if (sdkInt < required) reasons += BlockedReason.RequiresApi(required)
        }
        tool.requiredFeatures.forEach { feature ->
            if (!hasFeature(feature)) reasons += BlockedReason.MissingHardware(feature)
        }
        tool.requiredPermissions.forEach { permission ->
            if (!permissionChecker.isGranted(permission)) {
                reasons += BlockedReason.MissingPermission(permission)
            }
        }
        tool.requiredCapabilities.forEach { capability ->
            val candidates = providersByCapability[capability].orEmpty()
            if (candidates.none { it.status == ProviderStatus.Ready }) {
                val best = candidates.minByOrNull { statusRank(it.status) }
                reasons += BlockedReason.NeedsProvider(
                    kind = best?.kind ?: providerKindFor(capability),
                    status = best?.status ?: ProviderStatus.Unavailable("No provider registered"),
                )
            }
        }

        return if (reasons.isEmpty()) ToolResolution.Ready else ToolResolution.Blocked(reasons)
    }

    private fun statusRank(status: ProviderStatus): Int = when (status) {
        ProviderStatus.Ready -> 0
        is ProviderStatus.NeedsSetup -> 1
        is ProviderStatus.Unavailable -> 2
    }

    private fun providerKindFor(capability: Capability): ProviderKind = when (capability) {
        Capability.STANDARD_API, Capability.RUNTIME_PERMISSION -> ProviderKind.STANDARD
        Capability.SHIZUKU_SERVICE -> ProviderKind.SHIZUKU
        Capability.ADB_SHELL -> ProviderKind.ADB
        Capability.ROOT -> ProviderKind.ROOT
    }
}
```

`@Inject` on a constructor with `(String) -> Boolean` and `Int` params won't be Hilt-provided — Task 19 constructs it via `@Provides` in `:app`. The annotation is harmless.

- [x] **Step 6: Run tests**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :core:privileges:testDebugUnitTest
```

Expected: 8 tests PASS.

- [x] **Step 7: Commit**

```bash
git add core/privileges
git commit -m "feat(core): capability resolution engine"
```

---

### Task 7: `:core:database` — Room

**Files:**
- Create: `core/database/build.gradle.kts`, `core/database/src/main/AndroidManifest.xml`
- Create: `core/database/src/main/java/io/github/droidkit/core/database/Entities.kt`
- Create: `core/database/src/main/java/io/github/droidkit/core/database/Daos.kt`
- Create: `core/database/src/main/java/io/github/droidkit/core/database/DroidKitDatabase.kt`
- Create: `core/database/src/main/java/io/github/droidkit/core/database/DatabaseModule.kt`

**Interfaces:**
- Produces (schema matches `06_DroidKit_Data_Model_and_Registry.md`): `DiagnosticSessionEntity`, `DiagnosticResultEntity`, `DeviceSnapshotEntity`, `SavedReportEntity`; `DiagnosticSessionDao` (`insert`, `complete(sessionId, ts, status)`, `observeAll(): Flow<List>`), `DiagnosticResultDao` (`insert`, `observeBySession(id)`, `observeAll()`), `DeviceSnapshotDao` (`insert`, `observeAll()`), `SavedReportDao` (`insert`, `observeAll()`), `DroidKitDatabase`, `DatabaseModule` providing DB + DAOs

- [x] **Step 1: build file** (needs ksp for Room compiler)

```kotlin
plugins {
    id("droidkit.android.library")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.core.database"
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.coroutines.android)
    ksp(libs.androidx.room.compiler)
}
```

- [x] **Step 2: `Entities.kt`**

```kotlin
package io.github.droidkit.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "device_snapshots")
data class DeviceSnapshotEntity(
    @PrimaryKey val id: String,
    val capturedAtEpochMs: Long,
    val manufacturer: String,
    val model: String,
    val device: String,
    val product: String,
    val androidRelease: String,
    val apiLevel: Int,
    val securityPatch: String?,
    val primaryAbi: String,
    val totalRamMb: Long,
    val storageTotalBytes: Long,
    val storageFreeBytes: Long,
    val displaySummary: String,
    val capabilitySummary: String,
)

@Entity(tableName = "diagnostic_sessions", indices = [Index("deviceSnapshotId")])
data class DiagnosticSessionEntity(
    @PrimaryKey val id: String,
    val startedAtEpochMs: Long,
    val completedAtEpochMs: Long?,
    val deviceSnapshotId: String?,
    val overallStatus: String, // IN_PROGRESS | COMPLETED | ABANDONED
    val notes: String?,
    val appVersion: String,
)

@Entity(tableName = "diagnostic_results", indices = [Index("sessionId")])
data class DiagnosticResultEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val testId: String,
    val category: String,
    val state: String, // io.github.droidkit.core.common.TestResultState name
    val measuredDataJson: String?,
    val userNote: String?,
    val timestampEpochMs: Long,
)

@Entity(tableName = "saved_reports", indices = [Index("sessionId")])
data class SavedReportEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val generatedAtEpochMs: Long,
    val format: String, // TEXT | PDF | ...
    val localUri: String,
)
```

- [x] **Step 3: `Daos.kt`**

```kotlin
package io.github.droidkit.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosticSessionDao {
    @Insert
    suspend fun insert(session: DiagnosticSessionEntity)

    @Query("UPDATE diagnostic_sessions SET completedAtEpochMs = :ts, overallStatus = :status WHERE id = :sessionId")
    suspend fun complete(sessionId: String, ts: Long, status: String)

    @Query("SELECT * FROM diagnostic_sessions ORDER BY startedAtEpochMs DESC")
    fun observeAll(): Flow<List<DiagnosticSessionEntity>>
}

@Dao
interface DiagnosticResultDao {
    @Insert
    suspend fun insert(result: DiagnosticResultEntity)

    @Query("SELECT * FROM diagnostic_results WHERE sessionId = :sessionId")
    fun observeBySession(sessionId: String): Flow<List<DiagnosticResultEntity>>

    @Query("SELECT * FROM diagnostic_results")
    fun observeAll(): Flow<List<DiagnosticResultEntity>>
}

@Dao
interface DeviceSnapshotDao {
    @Insert
    suspend fun insert(snapshot: DeviceSnapshotEntity)

    @Query("SELECT * FROM device_snapshots ORDER BY capturedAtEpochMs DESC")
    fun observeAll(): Flow<List<DeviceSnapshotEntity>>
}

@Dao
interface SavedReportDao {
    @Insert
    suspend fun insert(report: SavedReportEntity)

    @Query("SELECT * FROM saved_reports ORDER BY generatedAtEpochMs DESC")
    fun observeAll(): Flow<List<SavedReportEntity>>
}
```

- [x] **Step 4: `DroidKitDatabase.kt`**

```kotlin
package io.github.droidkit.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DeviceSnapshotEntity::class,
        DiagnosticSessionEntity::class,
        DiagnosticResultEntity::class,
        SavedReportEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class DroidKitDatabase : RoomDatabase() {
    abstract fun sessionDao(): DiagnosticSessionDao
    abstract fun resultDao(): DiagnosticResultDao
    abstract fun snapshotDao(): DeviceSnapshotDao
    abstract fun reportDao(): SavedReportDao
}
```

- [x] **Step 5: `DatabaseModule.kt`**

```kotlin
package io.github.droidkit.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DroidKitDatabase =
        Room.databaseBuilder(context, DroidKitDatabase::class.java, "droidkit.db").build()

    @Provides fun sessionDao(db: DroidKitDatabase) = db.sessionDao()
    @Provides fun resultDao(db: DroidKitDatabase) = db.resultDao()
    @Provides fun snapshotDao(db: DroidKitDatabase) = db.snapshotDao()
    @Provides fun reportDao(db: DroidKitDatabase) = db.reportDao()
}
```

- [x] **Step 6: Compile + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :core:database:compileDebugKotlin
git add core/database
git commit -m "feat(core): room database schema v1"
```

---

### Task 8: `:core:datastore` — settings

**Files:**
- Create: `core/datastore/build.gradle.kts`, `core/datastore/src/main/AndroidManifest.xml`
- Create: `core/datastore/src/main/java/io/github/droidkit/core/datastore/SettingsRepository.kt`

**Interfaces:**
- Produces: `ThemeMode { SYSTEM, LIGHT, DARK }`; `SettingsRepository(context)` — `themeMode: Flow<ThemeMode>`, `favoriteToolIds: Flow<Set<String>>`, `recentToolIds: Flow<List<String>>`, `setThemeMode(ThemeMode)`, `toggleFavorite(String)`, `recordRecent(String)`

- [x] **Step 1: build file + manifest**

```kotlin
plugins {
    id("droidkit.android.library")
}

android {
    namespace = "io.github.droidkit.core.datastore"
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
}
```

- [x] **Step 2: `SettingsRepository.kt`**

```kotlin
package io.github.droidkit.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val Context.settingsStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    private val context: Context,
) {
    private val themeKey = stringPreferencesKey("theme_mode")
    private val favoritesKey = stringSetPreferencesKey("favorite_tools")
    private val recentsKey = stringPreferencesKey("recent_tools")
    private val onboardingKey = androidx.datastore.preferences.core.booleanPreferencesKey("onboarding_completed")

    val onboardingCompleted: Flow<Boolean> =
        context.settingsStore.data.map { it[onboardingKey] ?: false }

    suspend fun setOnboardingCompleted() {
        context.settingsStore.edit { it[onboardingKey] = true }
    }

    val themeMode: Flow<ThemeMode> = context.settingsStore.data.map {
        runCatching { ThemeMode.valueOf(it[themeKey] ?: "SYSTEM") }.getOrDefault(ThemeMode.SYSTEM)
    }

    val favoriteToolIds: Flow<Set<String>> =
        context.settingsStore.data.map { it[favoritesKey] ?: emptySet() }

    val recentToolIds: Flow<List<String>> = context.settingsStore.data.map {
        it[recentsKey]?.split(",")?.filter { s -> s.isNotBlank() } ?: emptyList()
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsStore.edit { it[themeKey] = mode.name }
    }

    suspend fun toggleFavorite(toolId: String) {
        context.settingsStore.edit { prefs ->
            val current = prefs[favoritesKey] ?: emptySet()
            prefs[favoritesKey] =
                if (toolId in current) current - toolId else current + toolId
        }
    }

    suspend fun recordRecent(toolId: String) {
        context.settingsStore.edit { prefs ->
            val current = prefs[recentsKey]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            prefs[recentsKey] = (listOf(toolId) + current.filter { it != toolId }).take(8).joinToString(",")
        }
    }
}
```

- [x] **Step 3: Compile + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :core:datastore:compileDebugKotlin
git add core/datastore
git commit -m "feat(core): settings datastore"
```

---

### Task 9: `:core:ui` — design system + shared screens

**Files:**
- Create: `core/ui/build.gradle.kts`, `core/ui/src/main/AndroidManifest.xml`
- Create: `core/ui/src/main/java/io/github/droidkit/core/ui/theme/Theme.kt`
- Create: `core/ui/src/main/java/io/github/droidkit/core/ui/components/ToolCard.kt`
- Create: `core/ui/src/main/java/io/github/droidkit/core/ui/components/Feedback.kt`
- Create: `core/ui/src/main/java/io/github/droidkit/core/ui/FeatureStubScreen.kt`

**Interfaces:**
- Produces: `DroidKitTheme(darkTheme: Boolean = isSystemInDarkTheme(), dynamicColor: Boolean = true, content)`; `ToolCard(tool, resolution, isFavorite, onFavorite, onClick)`; `AvailabilityBadge(resolution)`; `SectionHeader(text)`; `EmptyState(text)`; `BlockedReasonList(reasons)`; `FeatureStubScreen(title, subtitle, tools, resolve)` — full stub screen used by 15 feature modules

- [x] **Step 1: build file**

```kotlin
plugins {
    id("droidkit.android.library.compose")
}

android {
    namespace = "io.github.droidkit.core.ui"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
}
```

- [x] **Step 2: `theme/Theme.kt`** (Material3, dynamic color on S+, light/dark schemes default)

```kotlin
package io.github.droidkit.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun DroidKitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
```

- [x] **Step 3: `components/ToolCard.kt`**

```kotlin
package io.github.droidkit.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution

@Composable
fun AvailabilityBadge(resolution: ToolResolution) {
    val (label, color) = when (resolution) {
        ToolResolution.Ready -> "Ready" to MaterialTheme.colorScheme.primary
        is ToolResolution.Blocked -> "Blocked" to MaterialTheme.colorScheme.error
    }
    SuggestionChip(onClick = {}, label = { Text(label, color = color) })
}

@Composable
fun ToolCard(
    tool: Tool,
    resolution: ToolResolution,
    isFavorite: Boolean = false,
    onFavorite: (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tool.title, style = MaterialTheme.typography.titleMedium)
                Text(tool.description, style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SuggestionChip(onClick = {}, label = { Text(tool.category.name) })
                    if (tool.safetyLevel != SafetyLevel.SAFE) {
                        SuggestionChip(onClick = {}, label = { Text("L${tool.safetyLevel.ordinal}") })
                    }
                    if (tool.phase != ToolPhase.V1) {
                        SuggestionChip(onClick = {}, label = { Text(tool.phase.name) })
                    }
                }
            }
            AvailabilityBadge(resolution)
            if (onFavorite != null) {
                IconButton(onClick = onFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Favorite",
                    )
                }
            }
        }
    }
}
```

- [x] **Step 4: `components/Feedback.kt`**

```kotlin
package io.github.droidkit.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.privileges.BlockedReason
import io.github.droidkit.core.privileges.ProviderStatus

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
fun EmptyState(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

fun BlockedReason.describe(): String = when (this) {
    is BlockedReason.MissingPermission -> "Permission required: $permission"
    is BlockedReason.MissingHardware -> "Hardware missing: $feature"
    is BlockedReason.RequiresApi -> "Requires Android API $minSdk+"
    is BlockedReason.NeedsProvider -> buildString {
        append("Needs ${kind.name} provider: ")
        append(
            when (val s = status) {
                ProviderStatus.Ready -> "ready"
                is ProviderStatus.NeedsSetup -> "setup required — ${s.instructions}"
                is ProviderStatus.Unavailable -> s.reason
            },
        )
    }
}

@Composable
fun BlockedReasonList(reasons: List<BlockedReason>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Why this is blocked", style = MaterialTheme.typography.titleSmall)
        reasons.forEach { reason ->
            Text("• ${reason.describe()}", style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}
```

- [x] **Step 5: `FeatureStubScreen.kt`** — the shared stub screen

```kotlin
package io.github.droidkit.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.components.BlockedReasonList
import io.github.droidkit.core.ui.components.ToolCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureStubScreen(
    title: String,
    subtitle: String,
    tools: List<Tool>,
    resolve: (Tool) -> ToolResolution,
) {
    var selected by remember { mutableStateOf<Tool?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            items(tools, key = { it.id }) { tool ->
                ToolCard(
                    tool = tool,
                    resolution = resolve(tool),
                    onClick = { selected = tool },
                )
            }
        }
    }

    selected?.let { tool ->
        val resolution = resolve(tool)
        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("OK") } },
            title = { Text(tool.title) },
            text = {
                when (resolution) {
                    ToolResolution.Ready ->
                        Text("Available — implementation lands in phase ${tool.phase.name}.")
                    is ToolResolution.Blocked -> BlockedReasonList(resolution.reasons)
                }
            },
        )
    }
}
```

- [x] **Step 6: Compile + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :core:ui:compileDebugKotlin
git add core/ui
git commit -m "feat(core): compose design system and stub screen"
```

---

### Task 10: `:privilege:*` — providers

**Files:**
- Create: `privilege/standard/build.gradle.kts`, manifest, `privilege/standard/src/main/java/io/github/droidkit/privilege/standard/StandardAndroidProvider.kt`
- Create: `privilege/shizuku/build.gradle.kts`, manifest, `privilege/shizuku/src/main/java/io/github/droidkit/privilege/shizuku/ShizukuProvider.kt`
- Create: `privilege/adb/build.gradle.kts`, manifest, `privilege/adb/src/main/java/io/github/droidkit/privilege/adb/WirelessAdbProvider.kt`
- Create: `privilege/root/build.gradle.kts`, manifest, `privilege/root/src/main/java/io/github/droidkit/privilege/root/RootProvider.kt`

**Interfaces:**
- Produces: 4 `CapabilityProvider` implementations constructed in `:app`'s DI module (Task 19): `StandardAndroidProvider()`, `ShizukuProvider()`, `WirelessAdbProvider()`, `RootProvider()` — all no-arg except none take Context (Standard is Ready unconditionally; hardware/feature gating lives in CapabilityManager, not the provider)

- [x] **Step 1: `:privilege:standard`** — build file:

```kotlin
plugins {
    id("droidkit.android.library")
}

android {
    namespace = "io.github.droidkit.privilege.standard"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
}
```

`StandardAndroidProvider.kt`:

```kotlin
package io.github.droidkit.privilege.standard

import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.privileges.CapabilityProvider
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus

class StandardAndroidProvider : CapabilityProvider {
    override val kind = ProviderKind.STANDARD
    override val status = ProviderStatus.Ready
    override val capabilities = setOf(Capability.STANDARD_API, Capability.RUNTIME_PERMISSION)
}
```

- [x] **Step 2: `:privilege:shizuku`** — build file identical shape (`namespace = "io.github.droidkit.privilege.shizuku"`, same deps):

```kotlin
package io.github.droidkit.privilege.shizuku

import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.privileges.CapabilityProvider
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus

class ShizukuProvider : CapabilityProvider {
    override val kind = ProviderKind.SHIZUKU
    override val status = ProviderStatus.Unavailable("Coming in Phase 7 — Shizuku provider not implemented yet")
    override val capabilities = setOf(Capability.SHIZUKU_SERVICE)
}
```

- [x] **Step 3: `:privilege:adb`** (`namespace = "io.github.droidkit.privilege.adb"`):

```kotlin
package io.github.droidkit.privilege.adb

import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.privileges.CapabilityProvider
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus

class WirelessAdbProvider : CapabilityProvider {
    override val kind = ProviderKind.ADB
    override val status = ProviderStatus.Unavailable("Coming in Phase 9 — wireless ADB provider not implemented yet")
    override val capabilities = setOf(Capability.ADB_SHELL)
}
```

- [x] **Step 4: `:privilege:root`** (`namespace = "io.github.droidkit.privilege.root"`):

```kotlin
package io.github.droidkit.privilege.root

import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.privileges.CapabilityProvider
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus

class RootProvider : CapabilityProvider {
    override val kind = ProviderKind.ROOT
    override val status = ProviderStatus.Unavailable("Postponed — optional future provider")
    override val capabilities = setOf(Capability.ROOT)
}
```

- [x] **Step 5: Compile + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :privilege:standard:compileDebugKotlin :privilege:shizuku:compileDebugKotlin :privilege:adb:compileDebugKotlin :privilege:root:compileDebugKotlin
git add privilege
git commit -m "feat(privilege): standard provider + stub provider chain"
```

---

### Task 11: `:feature:dashboard` — Home tab (real)

**Files:**
- Create: `feature/dashboard/build.gradle.kts`, `feature/dashboard/src/main/AndroidManifest.xml`
- Create: `feature/dashboard/src/main/java/io/github/droidkit/feature/dashboard/DashboardFeature.kt`
- Create: `feature/dashboard/src/main/java/io/github/droidkit/feature/dashboard/DashboardViewModel.kt`
- Create: `feature/dashboard/src/main/java/io/github/droidkit/feature/dashboard/DashboardScreen.kt`

**Interfaces:**
- Produces: `object DashboardFeature : FeatureContribution { const val ROUTE = "home" }`, `fun NavGraphBuilder.dashboardGraph(nav: NavHostController)`
- Consumes: `DeviceSystemProbe`, `CapabilityManager`, `ToolRegistry`, `SettingsRepository`, `FeatureStubScreen`-style ToolCard, `DroidKitDestinations` nav strings (literal route strings)

- [x] **Step 1: build file**

```kotlin
plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.dashboard"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(projects.core.system)
    implementation(projects.core.datastore)
    implementation(projects.core.ui)
}
```

- [x] **Step 2: `DashboardFeature.kt`**

```kotlin
package io.github.droidkit.feature.dashboard

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory

object DashboardFeature : FeatureContribution {
    const val ROUTE = "home"

    override val tools = listOf(
        Tool(
            id = "dashboard.overview",
            title = "Device Overview",
            description = "Summary of this device and its capabilities",
            category = ToolCategory.DEVICE,
            route = ROUTE,
        ),
    )
}

fun NavGraphBuilder.dashboardGraph(nav: NavHostController) {
    composable(DashboardFeature.ROUTE) {
        DashboardScreen(
            onNavigateToTools = { nav.navigate("tools") },
            onNavigateToDiagnose = { nav.navigate("diagnose") },
            onOpenTool = { route -> nav.navigate(route) },
        )
    }
}
```

- [x] **Step 3: `DashboardViewModel.kt`**

```kotlin
package io.github.droidkit.feature.dashboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolRegistry
import io.github.droidkit.core.datastore.SettingsRepository
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.system.DeviceSystemProbe
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class DashboardViewModel @Inject constructor(
    val probe: DeviceSystemProbe,
    private val capabilityManager: CapabilityManager,
    private val registry: ToolRegistry,
    val settings: SettingsRepository,
) : ViewModel() {

    val recentToolIds: Flow<List<String>> = settings.recentToolIds

    fun providerStatuses(): Map<ProviderKind, ProviderStatus> =
        capabilityManager.providerStatuses()

    fun quickTools(): List<Tool> =
        registry.tools.filter { capabilityManager.resolve(it) == ToolResolution.Ready }.take(6)

    fun resolve(tool: Tool): ToolResolution = capabilityManager.resolve(tool)

    fun toolById(id: String): Tool? = registry.byId(id)
}
```

- [x] **Step 4: `DashboardScreen.kt`**

```kotlin
package io.github.droidkit.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.SectionHeader
import io.github.droidkit.core.ui.components.ToolCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTools: () -> Unit,
    onNavigateToDiagnose: () -> Unit,
    onOpenTool: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val recents by viewModel.recentToolIds.collectAsState(initial = emptyList())
    val recentTools = recents.mapNotNull { viewModel.toolById(it) }

    Scaffold(topBar = { TopAppBar(title = { Text("DroidKit") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("This device", style = MaterialTheme.typography.titleSmall)
                        Text(viewModel.probe.summary(), style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Elevated access", style = MaterialTheme.typography.titleSmall)
                        viewModel.providerStatuses().forEach { (kind, status) ->
                            val label = when (status) {
                                ProviderStatus.Ready -> "ready"
                                is ProviderStatus.NeedsSetup -> "setup required"
                                is ProviderStatus.Unavailable -> status.reason
                            }
                            Text("${kind.name}: $label",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                Button(onClick = onNavigateToDiagnose, modifier = Modifier.fillMaxWidth()) {
                    Text("Start Full Diagnostic")
                }
            }

            item { SectionHeader("Recent tools") }
            if (recentTools.isEmpty()) {
                item { EmptyState("No recent tools yet — browse the Tools tab.") }
            } else {
                items(recentTools, key = { it.id }) { tool ->
                    ToolCard(
                        tool = tool,
                        resolution = viewModel.resolve(tool),
                        onClick = { onOpenTool(tool.route) },
                    )
                }
            }

            item { SectionHeader("Ready now") }
            items(viewModel.quickTools(), key = { it.id }) { tool ->
                ToolCard(
                    tool = tool,
                    resolution = viewModel.resolve(tool),
                    onClick = { onOpenTool(tool.route) },
                )
            }
        }
    }
}
```

- [x] **Step 5: Compile + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :feature:dashboard:compileDebugKotlin
git add feature/dashboard
git commit -m "feat(dashboard): home tab shell"
```

---

### Task 12: `:feature:tools` — Tools tab (real)

**Files:**
- Create: `feature/tools/build.gradle.kts`, manifest
- Create: `feature/tools/src/main/java/io/github/droidkit/feature/tools/ToolsFeature.kt`
- Create: `feature/tools/src/main/java/io/github/droidkit/feature/tools/ToolsViewModel.kt`
- Create: `feature/tools/src/main/java/io/github/droidkit/feature/tools/ToolsScreen.kt`

**Interfaces:**
- Produces: `ToolsFeature.ROUTE = "tools"`, `fun NavGraphBuilder.toolsGraph(nav)`

- [x] **Step 1: build file** — same as dashboard (namespace `io.github.droidkit.feature.tools`; same dep set)

```kotlin
plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.tools"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(projects.core.datastore)
    implementation(projects.core.ui)
}
```

- [x] **Step 2: `ToolsFeature.kt`**

```kotlin
package io.github.droidkit.feature.tools

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution

object ToolsFeature : FeatureContribution {
    const val ROUTE = "tools"
    override val tools = emptyList<io.github.droidkit.core.common.Tool>()
}

fun NavGraphBuilder.toolsGraph(nav: NavHostController) {
    composable(ToolsFeature.ROUTE) {
        ToolsScreen(onOpenTool = { route -> nav.navigate(route) })
    }
}
```

- [x] **Step 3: `ToolsViewModel.kt`**

```kotlin
package io.github.droidkit.feature.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolRegistry
import io.github.droidkit.core.datastore.SettingsRepository
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.ToolResolution
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ToolsViewModel @Inject constructor(
    private val registry: ToolRegistry,
    private val capabilityManager: CapabilityManager,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _category = MutableStateFlow<ToolCategory?>(null)
    val category: StateFlow<ToolCategory?> = _category

    val categories: List<ToolCategory> = ToolCategory.entries

    val favorites: Flow<Set<String>> = settings.favoriteToolIds

    fun setQuery(q: String) { _query.value = q }

    fun setCategory(c: ToolCategory?) { _category.value = c }

    fun visibleTools(): List<Tool> {
        val base = _category.value?.let { registry.byCategory(it) } ?: registry.search(_query.value)
        return if (_category.value != null && _query.value.isNotBlank()) {
            base.filter {
                it.title.contains(_query.value, true) || it.description.contains(_query.value, true)
            }
        } else base
    }

    fun resolve(tool: Tool): ToolResolution = capabilityManager.resolve(tool)

    fun toggleFavorite(tool: Tool) {
        viewModelScope.launch { settings.toggleFavorite(tool.id) }
    }

    fun recordOpen(tool: Tool) {
        viewModelScope.launch { settings.recordRecent(tool.id) }
    }
}
```

- [x] **Step 4: `ToolsScreen.kt`**

```kotlin
package io.github.droidkit.feature.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.ToolCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onOpenTool: (String) -> Unit,
    viewModel: ToolsViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsState()
    val category by viewModel.category.collectAsState()
    val favorites by viewModel.favorites.collectAsState(initial = emptySet())

    Scaffold(topBar = { TopAppBar(title = { Text("Tools") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::setQuery,
                    label = { Text("Search tools") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = category == null,
                            onClick = { viewModel.setCategory(null) },
                            label = { Text("All") },
                        )
                    }
                    items(viewModel.categories) { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { viewModel.setCategory(if (category == c) null else c) },
                            label = { Text(c.name) },
                        )
                    }
                }
            }
            val tools = viewModel.visibleTools()
            if (tools.isEmpty()) {
                item { EmptyState("No tools match.") }
            } else {
                items(tools, key = { it.id }) { tool ->
                    ToolCard(
                        tool = tool,
                        resolution = viewModel.resolve(tool),
                        isFavorite = tool.id in favorites,
                        onFavorite = { viewModel.toggleFavorite(tool) },
                        onClick = {
                            viewModel.recordOpen(tool)
                            onOpenTool(tool.route)
                        },
                    )
                }
            }
        }
    }
}
```

- [x] **Step 5: Compile + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :feature:tools:compileDebugKotlin
git add feature/tools
git commit -m "feat(tools): searchable registry browser"
```

---

### Task 13: `:feature:diagnostics` + `:feature:reports` — Diagnose hub + reports (real-ish)

**Files:**
- Create: `feature/diagnostics/{build.gradle.kts, src/main/AndroidManifest.xml}` + `DiagnosticsFeature.kt`, `DiagnosticsViewModel.kt`, `DiagnosticsScreen.kt`
- Create: `feature/reports/{build.gradle.kts, src/main/AndroidManifest.xml}` + `ReportsFeature.kt`, `ReportsViewModel.kt`, `ReportsScreen.kt`

**Interfaces:**
- Produces: `DiagnosticsFeature.ROUTE = "diagnose"`, `diagnosticsGraph(nav)`; `ReportsFeature.ROUTE = "reports"`, `reportsGraph(nav)`
- Consumes: `DiagnosticSessionDao`, `DiagnosticResultDao`, `DeviceSystemProbe`

- [x] **Step 1: `:feature:diagnostics` build file**

```kotlin
plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.diagnostics"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(projects.core.database)
    implementation(projects.core.system)
    implementation(projects.core.ui)
}
```

- [x] **Step 2: `DiagnosticsFeature.kt`**

```kotlin
package io.github.droidkit.feature.diagnostics

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory

object DiagnosticsFeature : FeatureContribution {
    const val ROUTE = "diagnose"

    override val tools = listOf(
        Tool(
            id = "diagnostics.full",
            title = "Full Diagnostic",
            description = "Guided end-to-end hardware check with a saved report",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
            safetyLevel = SafetyLevel.SAFE,
            requiredCapabilities = setOf(Capability.STANDARD_API),
        ),
        Tool(
            id = "diagnostics.quick",
            title = "Quick Tests",
            description = "Jump straight to a single hardware test",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
        ),
    )
}

fun NavGraphBuilder.diagnosticsGraph(nav: NavHostController) {
    composable(DiagnosticsFeature.ROUTE) {
        DiagnosticsScreen(
            onOpenTool = { route -> nav.navigate(route) },
            onOpenReports = { nav.navigate("reports") },
        )
    }
}
```

- [x] **Step 3: `DiagnosticsViewModel.kt`**

```kotlin
package io.github.droidkit.feature.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.database.DeviceSnapshotDao
import io.github.droidkit.core.database.DeviceSnapshotEntity
import io.github.droidkit.core.database.DiagnosticResultDao
import io.github.droidkit.core.database.DiagnosticSessionDao
import io.github.droidkit.core.database.DiagnosticSessionEntity
import io.github.droidkit.core.system.DeviceSystemProbe
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val sessionDao: DiagnosticSessionDao,
    private val resultDao: DiagnosticResultDao,
    private val snapshotDao: DeviceSnapshotDao,
    private val probe: DeviceSystemProbe,
) : ViewModel() {

    val sessions: StateFlow<List<DiagnosticSessionEntity>> =
        sessionDao.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Creates a session + captures a device snapshot (doc flow 4). Guided tests land in Phase 3. */
    fun startSession() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val snapshot = DeviceSnapshotEntity(
                id = UUID.randomUUID().toString(),
                capturedAtEpochMs = now,
                manufacturer = probe.manufacturer,
                model = probe.model,
                device = probe.device,
                product = probe.product,
                androidRelease = probe.androidRelease,
                apiLevel = probe.sdkInt,
                securityPatch = probe.securityPatch,
                primaryAbi = probe.supportedAbis.firstOrNull().orEmpty(),
                totalRamMb = probe.totalRamMb,
                storageTotalBytes = probe.storage.totalBytes,
                storageFreeBytes = probe.storage.freeBytes,
                displaySummary = with(probe.displayInfo) { "${widthPx}x${heightPx} ${densityDpi}dpi ${refreshHz}Hz" },
                capabilitySummary = probe.capabilitySummary(),
            )
            snapshotDao.insert(snapshot)
            sessionDao.insert(
                DiagnosticSessionEntity(
                    id = UUID.randomUUID().toString(),
                    startedAtEpochMs = now,
                    completedAtEpochMs = null,
                    deviceSnapshotId = snapshot.id,
                    overallStatus = "IN_PROGRESS",
                    notes = null,
                    appVersion = probe.appVersionName,
                ),
            )
        }
    }
}
```

- [x] **Step 4: `DiagnosticsScreen.kt`**

```kotlin
package io.github.droidkit.feature.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.droidkit.core.ui.components.EmptyState
import io.github.droidkit.core.ui.components.SectionHeader
import java.text.DateFormat
import java.util.Date

private val quickTests = listOf(
    "Display & touch" to "display",
    "Audio" to "audio",
    "Camera & flash" to "camera",
    "Sensors" to "sensors",
    "Battery & charging" to "battery",
    "Network" to "network",
    "Bluetooth" to "bluetooth",
    "USB" to "usb",
    "NFC" to "nfc",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onOpenTool: (String) -> Unit,
    onOpenReports: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val sessions by viewModel.sessions.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Diagnose") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Button(onClick = { viewModel.startSession() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Start Full Diagnostic")
                }
            }
            item {
                OutlinedButton(onClick = onOpenReports, modifier = Modifier.fillMaxWidth()) {
                    Text("Saved reports")
                }
            }

            item { SectionHeader("Quick tests") }
            items(quickTests, key = { it.second }) { (label, route) ->
                Card(onClick = { onOpenTool(route) }, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            item { SectionHeader("History") }
            if (sessions.isEmpty()) {
                item { EmptyState("No diagnostic sessions yet.") }
            } else {
                items(sessions, key = { it.id }) { session ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                DateFormat.getDateTimeInstance().format(Date(session.startedAtEpochMs)),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(session.overallStatus, style = MaterialTheme.typography.bodySmall)
                            Text("v${session.appVersion}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
```

- [x] **Step 5: `:feature:reports`** — build file identical shape (namespace `io.github.droidkit.feature.reports`, deps `core.common/privileges/database/ui`)

```kotlin
plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.reports"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.ui)
}
```

`ReportsFeature.kt`:

```kotlin
package io.github.droidkit.feature.reports

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolPhase

object ReportsFeature : FeatureContribution {
    const val ROUTE = "reports"

    override val tools = listOf(
        Tool(
            id = "reports.export",
            title = "Export Report",
            description = "Share a diagnostic report as text",
            category = ToolCategory.REPORTS,
            route = ROUTE,
            safetyLevel = SafetyLevel.MINOR,
            phase = ToolPhase.V1,
        ),
    )
}

fun NavGraphBuilder.reportsGraph(nav: NavHostController) {
    composable(ReportsFeature.ROUTE) { ReportsScreen() }
}
```

`ReportsViewModel.kt`:

```kotlin
package io.github.droidkit.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.database.DiagnosticResultDao
import io.github.droidkit.core.database.DiagnosticResultEntity
import io.github.droidkit.core.database.DiagnosticSessionDao
import io.github.droidkit.core.database.DiagnosticSessionEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ReportRow(
    val session: DiagnosticSessionEntity,
    val results: List<DiagnosticResultEntity>,
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    sessionDao: DiagnosticSessionDao,
    resultDao: DiagnosticResultDao,
) : ViewModel() {

    val rows: StateFlow<List<ReportRow>> =
        combine(sessionDao.observeAll(), resultDao.observeAll()) { sessions, results ->
            sessions.map { s -> ReportRow(s, results.filter { it.sessionId == s.id }) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
```

`ReportsScreen.kt`:

```kotlin
package io.github.droidkit.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.droidkit.core.ui.components.EmptyState
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val rows by viewModel.rows.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Reports") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (rows.isEmpty()) {
                item { EmptyState("No saved reports. Run a diagnostic first.") }
            }
            items(rows, key = { it.session.id }) { row ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            DateFormat.getDateTimeInstance().format(Date(row.session.startedAtEpochMs)),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            "${row.session.overallStatus} — app v${row.session.appVersion}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        val counts = row.results.groupingBy { it.state }.eachCount()
                        Text(
                            if (counts.isEmpty()) "No test results recorded"
                            else counts.entries.joinToString { "${it.key}: ${it.value}" },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
```

- [x] **Step 6: Compile + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :feature:diagnostics:compileDebugKotlin :feature:reports:compileDebugKotlin
git add feature/diagnostics feature/reports
git commit -m "feat(diagnostics): diagnose hub, sessions, reports list"
```

---

### Task 14: `:feature:settings` — Settings tab (real)

**Files:**
- Create: `feature/settings/build.gradle.kts`, manifest
- Create: `feature/settings/src/main/java/io/github/droidkit/feature/settings/SettingsFeature.kt`
- Create: `feature/settings/src/main/java/io/github/droidkit/feature/settings/SettingsViewModel.kt`
- Create: `feature/settings/src/main/java/io/github/droidkit/feature/settings/SettingsScreen.kt`

**Interfaces:**
- Produces: `SettingsFeature.ROUTE = "settings"`, `settingsGraph(nav)`

- [x] **Step 1: build file** (namespace `io.github.droidkit.feature.settings`; deps common/privileges/datastore/ui)

```kotlin
plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.settings"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(projects.core.datastore)
    implementation(projects.core.ui)
}
```

- [x] **Step 2: `SettingsFeature.kt`**

```kotlin
package io.github.droidkit.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution

object SettingsFeature : FeatureContribution {
    const val ROUTE = "settings"
    override val tools = emptyList<io.github.droidkit.core.common.Tool>()
}

fun NavGraphBuilder.settingsGraph(nav: NavHostController) {
    composable(SettingsFeature.ROUTE) { SettingsScreen() }
}
```

- [x] **Step 3: `SettingsViewModel.kt`**

```kotlin
package io.github.droidkit.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.datastore.SettingsRepository
import io.github.droidkit.core.datastore.ThemeMode
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val capabilityManager: CapabilityManager,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> =
        settings.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    fun providerStatuses(): Map<ProviderKind, ProviderStatus> =
        capabilityManager.providerStatuses()

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }
}
```

- [x] **Step 4: `SettingsScreen.kt`**

```kotlin
package io.github.droidkit.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.droidkit.core.datastore.ThemeMode
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val theme by viewModel.themeMode.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader("Appearance") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = theme == mode,
                                    onClick = { viewModel.setTheme(mode) },
                                )
                                Text(mode.name)
                            }
                        }
                    }
                }
            }

            item { SectionHeader("Advanced access") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        viewModel.providerStatuses().forEach { (kind, status) ->
                            val label = when (status) {
                                ProviderStatus.Ready -> "Ready"
                                is ProviderStatus.NeedsSetup -> "Setup required — ${status.instructions}"
                                is ProviderStatus.Unavailable -> status.reason
                            }
                            Text(
                                "${kind.name} — $label",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            item { SectionHeader("Safety") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "High-impact actions always show their exact consequence and ask " +
                                "for explicit confirmation. DroidKit never performs security " +
                                "or credential bypass operations.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item { SectionHeader("About") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("DroidKit 0.1.0", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Android technician & power toolkit.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
```

- [x] **Step 5: Compile + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :feature:settings:compileDebugKotlin
git add feature/settings
git commit -m "feat(settings): settings tab shell"
```

---

### Task 15: `:feature:deviceinfo` — Device Inspector (mostly real)

**Files:**
- Create: `feature/deviceinfo/build.gradle.kts`, manifest
- Create: `feature/deviceinfo/src/main/java/io/github/droidkit/feature/deviceinfo/DeviceInfoFeature.kt`
- Create: `feature/deviceinfo/src/main/java/io/github/droidkit/feature/deviceinfo/DeviceInfoScreen.kt` (uses probe via a small VM)

- [x] **Step 1: build file**

```kotlin
plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit.feature.deviceinfo"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(projects.core.system)
    implementation(projects.core.ui)
}
```

- [x] **Step 2: `DeviceInfoFeature.kt`**

```kotlin
package io.github.droidkit.feature.deviceinfo

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory

object DeviceInfoFeature : FeatureContribution {
    const val ROUTE = "deviceinfo"

    override val tools = listOf(
        Tool(
            id = "deviceinfo.overview",
            title = "Device Inspector",
            description = "Build, CPU/ABI, memory, storage, display, battery, sensors, cameras",
            category = ToolCategory.DEVICE,
            route = ROUTE,
        ),
    )
}

fun NavGraphBuilder.deviceInfoGraph(nav: NavHostController) {
    composable(DeviceInfoFeature.ROUTE) { DeviceInfoScreen() }
}
```

- [x] **Step 3: `DeviceInfoViewModel.kt` + `DeviceInfoScreen.kt`**

```kotlin
package io.github.droidkit.feature.deviceinfo

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.system.DeviceSystemProbe
import javax.inject.Inject

@HiltViewModel
class DeviceInfoViewModel @Inject constructor(
    val probe: DeviceSystemProbe,
) : ViewModel()
```

```kotlin
package io.github.droidkit.feature.deviceinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreen(viewModel: DeviceInfoViewModel = hiltViewModel()) {
    val probe = viewModel.probe
    val display = probe.displayInfo
    val battery = probe.batteryCapacityPct

    fun section(title: String, lines: List<Pair<String, String>>): @Composable () -> Unit = {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                lines.forEach { (k, v) ->
                    Text("$k: $v", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    val sections = listOf(
        section("Device", listOf(
            "Manufacturer" to probe.manufacturer,
            "Model" to probe.model,
            "Device" to probe.device,
            "Product" to probe.product,
        )),
        section("Android", listOf(
            "Release" to probe.androidRelease,
            "API level" to probe.sdkInt.toString(),
        )),
        section("CPU / ABI", listOf("Supported ABIs" to probe.supportedAbis.joinToString())),
        section("Memory", listOf("Total RAM" to "${probe.totalRamMb} MB")),
        section("Storage", listOf(
            "Total" to "${probe.storage.totalBytes / (1024 * 1024 * 1024)} GB",
            "Free" to "${probe.storage.freeBytes / (1024 * 1024 * 1024)} GB",
        )),
        section("Display", listOf(
            "Resolution" to "${display.widthPx} x ${display.heightPx}",
            "Density" to "${display.densityDpi} dpi",
            "Refresh rate" to "${display.refreshHz} Hz",
        )),
        section("Battery", listOf(
            "Charge level" to if (battery >= 0f) "${battery.toInt()}%" else "Not exposed by platform",
        )),
        section("Sensors", listOf("Count" to probe.sensors.size.toString()) +
            probe.sensors.take(12).map { it.name to it.vendor }),
        section("Cameras", listOf("Camera ids" to probe.cameraIds.joinToString().ifBlank { "None" })),
        section("Hardware features", listOf(
            "NFC" to probe.hasFeature("android.hardware.nfc").toString(),
            "Bluetooth" to probe.hasFeature("android.hardware.bluetooth").toString(),
            "USB host" to probe.hasFeature("android.hardware.usb.host").toString(),
            "Telephony" to probe.hasFeature("android.hardware.telephony").toString(),
        )),
    )

    Scaffold(topBar = { TopAppBar(title = { Text("Device Inspector") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(sections.size) { i -> sections[i]() }
        }
    }
}
```

- [x] **Step 4: Compile + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :feature:deviceinfo:compileDebugKotlin
git add feature/deviceinfo
git commit -m "feat(deviceinfo): device inspector with live probes"
```

---

### Task 16: Phone Doctor stub features — battery, display, audio, camera, sensors

**Pattern (identical structure per module):**
- `feature/<name>/build.gradle.kts` — `droidkit.android.library.compose` only (no Hilt), deps `core.common` + `core.privileges` + `core.ui`. Exact contents, one per module — `feature/battery/build.gradle.kts` is shown in Step 1; the other four are byte-identical except `namespace`:

  | Module | namespace value |
  |--------|-----------------|
  | `:feature:display` | `"io.github.droidkit.feature.display"` |
  | `:feature:audio` | `"io.github.droidkit.feature.audio"` |
  | `:feature:camera` | `"io.github.droidkit.feature.camera"` |
  | `:feature:sensors` | `"io.github.droidkit.feature.sensors"` |

- `feature/<name>/src/main/AndroidManifest.xml` — exact contents per module:

  | Module | Manifest |
  |--------|----------|
  | `:feature:battery` | `<?xml version="1.0" encoding="utf-8"?><manifest />` |
  | `:feature:display` | `<?xml version="1.0" encoding="utf-8"?><manifest />` |
  | `:feature:audio` | shown in Step 3 (RECORD_AUDIO + VIBRATE) |
  | `:feature:camera` | shown in Step 4 (CAMERA + uses-feature camera.any) |
  | `:feature:sensors` | `<?xml version="1.0" encoding="utf-8"?><manifest />` |

- `feature/<name>/src/main/java/io/github/droidkit/feature/<name>/<Name>Feature.kt` — `object <Name>Feature : FeatureContribution` with `const val ROUTE` + `tools`, plus `fun NavGraphBuilder.<name>Graph(nav, resolve)` calling `FeatureStubScreen`

**Shared graph signature** (all stub features use this):

```kotlin
fun NavGraphBuilder.<name>Graph(
    nav: NavHostController,
    resolve: (Tool) -> io.github.droidkit.core.privileges.ToolResolution,
) {
    composable(<Name>Feature.ROUTE) {
        FeatureStubScreen(
            title = "<Title>",
            subtitle = "<copy>",
            tools = <Name>Feature.tools,
            resolve = resolve,
        )
    }
}
```

`<name>Graph` needs `core.privileges` on the classpath for the `ToolResolution` type — add `implementation(projects.core.privileges)` to stub build files too (compile-time dep only, no provider checks inside features).

- [x] **Step 1: `:feature:battery`**

build.gradle.kts:

```kotlin
plugins {
    id("droidkit.android.library.compose")
}

android {
    namespace = "io.github.droidkit.feature.battery"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.privileges)
    implementation(projects.core.ui)
}
```

`BatteryFeature.kt`:

```kotlin
package io.github.droidkit.feature.battery

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object BatteryFeature : FeatureContribution {
    const val ROUTE = "battery"

    override val tools = listOf(
        Tool(
            id = "battery.charge",
            title = "Charging Test",
            description = "Detect charger presence, rate and USB/AC/wireless source",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
        ),
        Tool(
            id = "battery.health",
            title = "Battery Health",
            description = "Estimated capacity vs design — needs elevated stats access",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
            requiredCapabilities = setOf(Capability.ADB_SHELL),
            phase = ToolPhase.V2,
        ),
    )
}

fun NavGraphBuilder.batteryGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(BatteryFeature.ROUTE) {
        FeatureStubScreen(
            title = "Battery & Charging",
            subtitle = "Guided battery checks ship in Phase 3 (Phone Doctor).",
            tools = BatteryFeature.tools,
            resolve = resolve,
        )
    }
}
```

- [x] **Step 2: `:feature:display`** (namespace `io.github.droidkit.feature.display`)

```kotlin
package io.github.droidkit.feature.display

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object DisplayFeature : FeatureContribution {
    const val ROUTE = "display"

    override val tools = listOf(
        Tool(
            id = "display.panel",
            title = "Display Test",
            description = "Color fills, dead-pixel and backlight check",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
        ),
        Tool(
            id = "display.touch",
            title = "Touch Test",
            description = "Single-touch grid coverage check",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
        ),
        Tool(
            id = "display.multitouch",
            title = "Multitouch Test",
            description = "Count simultaneous touch points",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
        ),
    )
}

fun NavGraphBuilder.displayGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(DisplayFeature.ROUTE) {
        FeatureStubScreen(
            title = "Display & Touch",
            subtitle = "Interactive display and touch tests ship in Phase 3 (Phone Doctor).",
            tools = DisplayFeature.tools,
            resolve = resolve,
        )
    }
}
```

- [x] **Step 3: `:feature:audio`** (namespace `io.github.droidkit.feature.audio`)

Manifest declares mic permission:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
</manifest>
```

`AudioFeature.kt`:

```kotlin
package io.github.droidkit.feature.audio

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object AudioFeature : FeatureContribution {
    const val ROUTE = "audio"

    override val tools = listOf(
        Tool(
            id = "audio.speaker",
            title = "Speaker Test",
            description = "Play test tones through each output",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
        ),
        Tool(
            id = "audio.microphone",
            title = "Microphone Test",
            description = "Record and meter microphone input",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.RECORD_AUDIO"),
        ),
        Tool(
            id = "audio.vibration",
            title = "Vibration Test",
            description = "Run haptic patterns and confirm motor response",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.VIBRATE"),
        ),
    )
}

fun NavGraphBuilder.audioGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(AudioFeature.ROUTE) {
        FeatureStubScreen(
            title = "Audio & Haptics",
            subtitle = "Speaker, microphone and vibration tests ship in Phase 3 (Phone Doctor).",
            tools = AudioFeature.tools,
            resolve = resolve,
        )
    }
}
```

Add `VIBRATE` to the manifest too (`<uses-permission android:name="android.permission.VIBRATE" />`).

- [x] **Step 4: `:feature:camera`** (namespace `io.github.droidkit.feature.camera`)

Manifest:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-feature android:name="android.hardware.camera.any" android:required="false" />
</manifest>
```

`CameraFeature.kt`:

```kotlin
package io.github.droidkit.feature.camera

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object CameraFeature : FeatureContribution {
    const val ROUTE = "camera"

    override val tools = listOf(
        Tool(
            id = "camera.capture",
            title = "Camera Test",
            description = "Preview each camera and capture a test frame",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.CAMERA"),
            requiredFeatures = setOf("android.hardware.camera.any"),
        ),
        Tool(
            id = "camera.flash",
            title = "Flash Test",
            description = "Toggle torch on each camera unit",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
            requiredFeatures = setOf("android.hardware.camera.flash"),
        ),
    )
}

fun NavGraphBuilder.cameraGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(CameraFeature.ROUTE) {
        FeatureStubScreen(
            title = "Camera & Flash",
            subtitle = "Camera and flash tests ship in Phase 3 (Phone Doctor).",
            tools = CameraFeature.tools,
            resolve = resolve,
        )
    }
}
```

- [x] **Step 5: `:feature:sensors`** (namespace `io.github.droidkit.feature.sensors`)

```kotlin
package io.github.droidkit.feature.sensors

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object SensorsFeature : FeatureContribution {
    const val ROUTE = "sensors"

    override val tools = listOf(
        Tool(
            id = "sensors.live",
            title = "Live Sensor Test",
            description = "Accelerometer, gyroscope, compass, proximity readouts",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
        ),
        Tool(
            id = "sensors.inventory",
            title = "Sensor Inventory",
            description = "Full sensor list with vendor and power info",
            category = ToolCategory.DEVICE,
            route = ROUTE,
        ),
    )
}

fun NavGraphBuilder.sensorsGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(SensorsFeature.ROUTE) {
        FeatureStubScreen(
            title = "Sensors",
            subtitle = "Guided sensor tests ship in Phase 3 (Phone Doctor).",
            tools = SensorsFeature.tools,
            resolve = resolve,
        )
    }
}
```

- [x] **Step 6: Compile all five + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :feature:battery:compileDebugKotlin :feature:display:compileDebugKotlin :feature:audio:compileDebugKotlin :feature:camera:compileDebugKotlin :feature:sensors:compileDebugKotlin
git add feature/battery feature/display feature/audio feature/camera feature/sensors
git commit -m "feat(diagnostics): phone doctor test category stubs"
```

---

### Task 17: Connectivity/hardware stub features — network, bluetooth, usb, nfc

Same file layout as Task 16: `build.gradle.kts` uses `droidkit.android.library.compose` with deps `core.common` + `core.privileges` + `core.ui` (byte-identical to Task 16 Step 1 except `namespace` = `io.github.droidkit.feature.<name>`). Manifests: network/bluetooth/nfc shown inline below; `:feature:usb` gets `<?xml version="1.0" encoding="utf-8"?><manifest />`.

- [x] **Step 1: `:feature:network`** (namespace `io.github.droidkit.feature.network`)

Manifest:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
</manifest>
```

`NetworkFeature.kt`:

```kotlin
package io.github.droidkit.feature.network

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object NetworkFeature : FeatureContribution {
    const val ROUTE = "network"

    override val tools = listOf(
        Tool(
            id = "network.wifi",
            title = "Wi-Fi Info",
            description = "SSID, signal, link speed, IP details",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            requiredPermissions = listOf(
                "android.permission.ACCESS_WIFI_STATE",
                "android.permission.ACCESS_FINE_LOCATION",
            ),
        ),
        Tool(
            id = "network.interfaces",
            title = "Network Interfaces",
            description = "Cellular, VPN and interface state",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.ACCESS_NETWORK_STATE"),
        ),
    )
}

fun NavGraphBuilder.networkGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(NetworkFeature.ROUTE) {
        FeatureStubScreen(
            title = "Network",
            subtitle = "Wi-Fi, cellular and interface tools. Identifier access is OS/version/permission dependent.",
            tools = NetworkFeature.tools,
            resolve = resolve,
        )
    }
}
```

- [x] **Step 2: `:feature:bluetooth`** (namespace `io.github.droidkit.feature.bluetooth`)

Manifest:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
</manifest>
```

`BluetoothFeature.kt`:

```kotlin
package io.github.droidkit.feature.bluetooth

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object BluetoothFeature : FeatureContribution {
    const val ROUTE = "bluetooth"

    override val tools = listOf(
        Tool(
            id = "bluetooth.status",
            title = "Bluetooth Status",
            description = "Adapter state, name, paired devices",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.BLUETOOTH_CONNECT"),
            minSdk = 31,
        ),
        Tool(
            id = "bluetooth.scan",
            title = "BLE Scan",
            description = "Scan for nearby BLE devices",
            category = ToolCategory.CONNECTIVITY,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.BLUETOOTH_CONNECT"),
            requiredFeatures = setOf("android.hardware.bluetooth"),
            minSdk = 31,
        ),
    )
}

fun NavGraphBuilder.bluetoothGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(BluetoothFeature.ROUTE) {
        FeatureStubScreen(
            title = "Bluetooth",
            subtitle = "Bluetooth tools ship with the connectivity pass.",
            tools = BluetoothFeature.tools,
            resolve = resolve,
        )
    }
}
```

- [x] **Step 3: `:feature:usb`** (namespace `io.github.droidkit.feature.usb`)

```kotlin
package io.github.droidkit.feature.usb

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object UsbFeature : FeatureContribution {
    const val ROUTE = "usb"

    override val tools = listOf(
        Tool(
            id = "usb.host",
            title = "USB Host Check",
            description = "Detect USB host capability and connected devices",
            category = ToolCategory.HARDWARE,
            route = ROUTE,
            requiredFeatures = setOf("android.hardware.usb.host"),
        ),
        Tool(
            id = "usb.descriptors",
            title = "USB Descriptors",
            description = "VID/PID, interfaces and endpoints for attached devices",
            category = ToolCategory.HARDWARE,
            route = ROUTE,
            requiredFeatures = setOf("android.hardware.usb.host"),
        ),
    )
}

fun NavGraphBuilder.usbGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(UsbFeature.ROUTE) {
        FeatureStubScreen(
            title = "USB Lab",
            subtitle = "USB host detection, permission flow and descriptor reads ship in Phase 6.",
            tools = UsbFeature.tools,
            resolve = resolve,
        )
    }
}
```

- [x] **Step 4: `:feature:nfc`** (namespace `io.github.droidkit.feature.nfc`)

Manifest: `<uses-permission android:name="android.permission.NFC" />`

`NfcFeature.kt`:

```kotlin
package io.github.droidkit.feature.nfc

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object NfcFeature : FeatureContribution {
    const val ROUTE = "nfc"

    override val tools = listOf(
        Tool(
            id = "nfc.read",
            title = "NFC Tag Read",
            description = "Tag technologies and NDEF record reading",
            category = ToolCategory.HARDWARE,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.NFC"),
            requiredFeatures = setOf("android.hardware.nfc"),
        ),
        Tool(
            id = "nfc.write",
            title = "NFC Write / Format",
            description = "Write, erase and format supported tags",
            category = ToolCategory.HARDWARE,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.NFC"),
            requiredFeatures = setOf("android.hardware.nfc"),
        ),
    )
}

fun NavGraphBuilder.nfcGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(NfcFeature.ROUTE) {
        FeatureStubScreen(
            title = "NFC Lab",
            subtitle = "Tag tech detection, NDEF read and write/format ship in Phase 6.",
            tools = NfcFeature.tools,
            resolve = resolve,
        )
    }
}
```

- [x] **Step 5: Compile all four + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :feature:network:compileDebugKotlin :feature:bluetooth:compileDebugKotlin :feature:usb:compileDebugKotlin :feature:nfc:compileDebugKotlin
git add feature/network feature/bluetooth feature/usb feature/nfc
git commit -m "feat(features): connectivity and hardware lab stubs"
```

---

### Task 18: App/elevated stub features — apps, permissions, appops, packages, adb, developer

Same file layout as Task 16: `build.gradle.kts` uses `droidkit.android.library.compose` with deps `core.common` + `core.privileges` + `core.ui` (byte-identical to Task 16 Step 1 except `namespace` = `io.github.droidkit.feature.<name>`). All six modules get the `<?xml version="1.0" encoding="utf-8"?><manifest />` stub manifest. These demonstrate the provider-gating story (Shizuku/ADB) and higher safety levels.

- [x] **Step 1: `:feature:apps`** (namespace `io.github.droidkit.feature.apps`)

```kotlin
package io.github.droidkit.feature.apps

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object AppsFeature : FeatureContribution {
    const val ROUTE = "apps"

    override val tools = listOf(
        Tool(
            id = "apps.installed",
            title = "Installed Apps",
            description = "User and system apps with package metadata",
            category = ToolCategory.APPS,
            route = ROUTE,
        ),
        Tool(
            id = "apps.apk",
            title = "APK Inspector",
            description = "Metadata and components of installed packages",
            category = ToolCategory.APPS,
            route = ROUTE,
        ),
    )
}

fun NavGraphBuilder.appsGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(AppsFeature.ROUTE) {
        FeatureStubScreen(
            title = "App Inspector",
            subtitle = "Package visibility on modern Android limits what's listed — full inspector ships in Phase 5.",
            tools = AppsFeature.tools,
            resolve = resolve,
        )
    }
}
```

- [x] **Step 2: `:feature:permissions`** (namespace `io.github.droidkit.feature.permissions`)

```kotlin
package io.github.droidkit.feature.permissions

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object PermissionsFeature : FeatureContribution {
    const val ROUTE = "permissions"

    override val tools = listOf(
        Tool(
            id = "permissions.inspector",
            title = "Permission Inspector",
            description = "Per-app granted/declared permissions and runtime state",
            category = ToolCategory.APPS,
            route = ROUTE,
            requiredCapabilities = setOf(Capability.SHIZUKU_SERVICE),
            phase = ToolPhase.V2,
        ),
    )
}

fun NavGraphBuilder.permissionsGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(PermissionsFeature.ROUTE) {
        FeatureStubScreen(
            title = "Permissions",
            subtitle = "Runtime grant state for other apps needs elevated access — lands with the Shizuku provider in Phase 7.",
            tools = PermissionsFeature.tools,
            resolve = resolve,
        )
    }
}
```

- [x] **Step 3: `:feature:appops`** (namespace `io.github.droidkit.feature.appops`)

```kotlin
package io.github.droidkit.feature.appops

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object AppOpsFeature : FeatureContribution {
    const val ROUTE = "appops"

    override val tools = listOf(
        Tool(
            id = "appops.view",
            title = "AppOps Viewer",
            description = "Per-app AppOps modes (usage access, overlays, background)",
            category = ToolCategory.APPS,
            route = ROUTE,
            requiredCapabilities = setOf(Capability.SHIZUKU_SERVICE),
            phase = ToolPhase.V2,
        ),
        Tool(
            id = "appops.set",
            title = "AppOps Controls",
            description = "Change AppOps modes — modifies system permission state",
            category = ToolCategory.APPS,
            route = ROUTE,
            safetyLevel = SafetyLevel.SYSTEM_CHANGE,
            requiredCapabilities = setOf(Capability.SHIZUKU_SERVICE),
            phase = ToolPhase.V2,
        ),
    )
}

fun NavGraphBuilder.appOpsGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(AppOpsFeature.ROUTE) {
        FeatureStubScreen(
            title = "AppOps",
            subtitle = "Requires the Shizuku provider — Phase 7/8.",
            tools = AppOpsFeature.tools,
            resolve = resolve,
        )
    }
}
```

- [x] **Step 4: `:feature:packages`** (namespace `io.github.droidkit.feature.packages`)

```kotlin
package io.github.droidkit.feature.packages

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object PackagesFeature : FeatureContribution {
    const val ROUTE = "packages"

    override val tools = listOf(
        Tool(
            id = "packages.disable",
            title = "Disable Package",
            description = "Enable/disable packages — bloatware control",
            category = ToolCategory.APPS,
            route = ROUTE,
            safetyLevel = SafetyLevel.HIGH_IMPACT,
            requiredCapabilities = setOf(Capability.ADB_SHELL),
            phase = ToolPhase.V2,
        ),
        Tool(
            id = "packages.clear",
            title = "Clear App Data",
            description = "Wipe an app's data — irreversible",
            category = ToolCategory.APPS,
            route = ROUTE,
            safetyLevel = SafetyLevel.HIGH_IMPACT,
            requiredCapabilities = setOf(Capability.ADB_SHELL),
            phase = ToolPhase.V2,
        ),
        Tool(
            id = "packages.uninstall",
            title = "Uninstall for User",
            description = "Uninstall package for current user profile",
            category = ToolCategory.APPS,
            route = ROUTE,
            safetyLevel = SafetyLevel.HIGH_IMPACT,
            requiredCapabilities = setOf(Capability.ADB_SHELL),
            phase = ToolPhase.V2,
        ),
    )
}

fun NavGraphBuilder.packagesGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(PackagesFeature.ROUTE) {
        FeatureStubScreen(
            title = "Package Controls",
            subtitle = "High-impact package operations gated on wireless ADB — Phase 8/9. " +
                "Each action will show its exact consequence before running.",
            tools = PackagesFeature.tools,
            resolve = resolve,
        )
    }
}
```

- [x] **Step 5: `:feature:adb`** (namespace `io.github.droidkit.feature.adb`)

```kotlin
package io.github.droidkit.feature.adb

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object AdbFeature : FeatureContribution {
    const val ROUTE = "adb"

    override val tools = listOf(
        Tool(
            id = "adb.pairing",
            title = "Wireless ADB Pairing",
            description = "Pair with the device's own wireless debugging service",
            category = ToolCategory.DEVELOPER,
            route = ROUTE,
            safetyLevel = SafetyLevel.SYSTEM_CHANGE,
            requiredCapabilities = setOf(Capability.ADB_SHELL),
            phase = ToolPhase.V2,
        ),
        Tool(
            id = "adb.shell",
            title = "ADB Shell",
            description = "Run shell commands through the wireless ADB provider",
            category = ToolCategory.DEVELOPER,
            route = ROUTE,
            safetyLevel = SafetyLevel.ADVANCED,
            requiredCapabilities = setOf(Capability.ADB_SHELL),
            phase = ToolPhase.V2,
        ),
    )
}

fun NavGraphBuilder.adbGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(AdbFeature.ROUTE) {
        FeatureStubScreen(
            title = "Wireless ADB",
            subtitle = "Pairing/session provider ships in Phase 9.",
            tools = AdbFeature.tools,
            resolve = resolve,
        )
    }
}
```

- [x] **Step 6: `:feature:developer`** (namespace `io.github.droidkit.feature.developer`)

```kotlin
package io.github.droidkit.feature.developer

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SafetyLevel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolPhase
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object DeveloperFeature : FeatureContribution {
    const val ROUTE = "developer"

    override val tools = listOf(
        Tool(
            id = "developer.shortcuts",
            title = "Developer Shortcuts",
            description = "Jump to Developer options, USB debugging and system settings",
            category = ToolCategory.DEVELOPER,
            route = ROUTE,
            safetyLevel = SafetyLevel.MINOR,
        ),
        Tool(
            id = "developer.logs",
            title = "Log Viewer",
            description = "Read device logs where authorized",
            category = ToolCategory.DEVELOPER,
            route = ROUTE,
            safetyLevel = SafetyLevel.ADVANCED,
            requiredCapabilities = setOf(Capability.ADB_SHELL),
            phase = ToolPhase.V2,
        ),
    )
}

fun NavGraphBuilder.developerGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(DeveloperFeature.ROUTE) {
        FeatureStubScreen(
            title = "Developer Lab",
            subtitle = "Shortcuts, dumpsys helpers and authorized log access — Phase 10.",
            tools = DeveloperFeature.tools,
            resolve = resolve,
        )
    }
}
```

- [x] **Step 7: Compile all six + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :feature:apps:compileDebugKotlin :feature:permissions:compileDebugKotlin :feature:appops:compileDebugKotlin :feature:packages:compileDebugKotlin :feature:adb:compileDebugKotlin :feature:developer:compileDebugKotlin
git add feature/apps feature/permissions feature/appops feature/packages feature/adb feature/developer
git commit -m "feat(features): app and elevated tool stubs"
```

---

### Task 19: `:app` — application wiring

**Files:**
- Create: `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `app/src/main/res/drawable/ic_launcher.xml`
- Create: `app/src/main/java/io/github/droidkit/DroidKitApp.kt`
- Create: `app/src/main/java/io/github/droidkit/di/AppModule.kt`
- Create: `app/src/main/java/io/github/droidkit/MainActivity.kt`
- Create: `app/src/main/java/io/github/droidkit/navigation/DroidKitNavHost.kt`

**Interfaces:**
- Consumes: every feature `object`/`graph` function, every provider class, all core services
- Produces: the runnable app; bottom nav routes `home`/`tools`/`diagnose`/`settings`

- [x] **Step 1: `app/build.gradle.kts`**

```kotlin
plugins {
    id("droidkit.android.application")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.ui)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.permissions)
    implementation(projects.core.system)
    implementation(projects.core.privileges)

    implementation(projects.privilege.standard)
    implementation(projects.privilege.shizuku)
    implementation(projects.privilege.adb)
    implementation(projects.privilege.root)

    implementation(projects.feature.dashboard)
    implementation(projects.feature.tools)
    implementation(projects.feature.diagnostics)
    implementation(projects.feature.reports)
    implementation(projects.feature.settings)
    implementation(projects.feature.deviceinfo)
    implementation(projects.feature.battery)
    implementation(projects.feature.display)
    implementation(projects.feature.audio)
    implementation(projects.feature.camera)
    implementation(projects.feature.sensors)
    implementation(projects.feature.network)
    implementation(projects.feature.bluetooth)
    implementation(projects.feature.usb)
    implementation(projects.feature.nfc)
    implementation(projects.feature.apps)
    implementation(projects.feature.permissions)
    implementation(projects.feature.appops)
    implementation(projects.feature.packages)
    implementation(projects.feature.adb)
    implementation(projects.feature.developer)
}
```

- [x] **Step 2: `app/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:name=".DroidKitApp"
        android:label="DroidKit"
        android:icon="@drawable/ic_launcher"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.NoTitleBar">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [x] **Step 3: `app/src/main/res/drawable/ic_launcher.xml`** (simple vector)

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="48dp"
    android:height="48dp"
    android:viewportWidth="48"
    android:viewportHeight="48">
    <path
        android:fillColor="#3DDC84"
        android:pathData="M8,8h32v32h-32z" />
    <path
        android:fillColor="#00393D"
        android:pathData="M18,18h12v12h-12z" />
</vector>
```

- [x] **Step 4: `DroidKitApp.kt`** (includes `DLog` wiring — `DLog` is defined in Task 20 Step 3; create that file before compiling, or add the two lines then)

```kotlin
package io.github.droidkit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.droidkit.core.common.DLog

@HiltAndroidApp
class DroidKitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DLog.enabled = BuildConfig.DEBUG
    }
}
```

- [x] **Step 5: `di/AppModule.kt`**

```kotlin
package io.github.droidkit.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.ToolRegistry
import io.github.droidkit.core.datastore.SettingsRepository
import io.github.droidkit.core.permissions.PermissionChecker
import io.github.droidkit.core.permissions.PermissionManager
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.CapabilityProvider
import io.github.droidkit.core.system.DeviceSystemProbe
import io.github.droidkit.feature.adb.AdbFeature
import io.github.droidkit.feature.appops.AppOpsFeature
import io.github.droidkit.feature.apps.AppsFeature
import io.github.droidkit.feature.audio.AudioFeature
import io.github.droidkit.feature.battery.BatteryFeature
import io.github.droidkit.feature.bluetooth.BluetoothFeature
import io.github.droidkit.feature.camera.CameraFeature
import io.github.droidkit.feature.dashboard.DashboardFeature
import io.github.droidkit.feature.developer.DeveloperFeature
import io.github.droidkit.feature.deviceinfo.DeviceInfoFeature
import io.github.droidkit.feature.diagnostics.DiagnosticsFeature
import io.github.droidkit.feature.display.DisplayFeature
import io.github.droidkit.feature.network.NetworkFeature
import io.github.droidkit.feature.nfc.NfcFeature
import io.github.droidkit.feature.packages.PackagesFeature
import io.github.droidkit.feature.permissions.PermissionsFeature
import io.github.droidkit.feature.reports.ReportsFeature
import io.github.droidkit.feature.sensors.SensorsFeature
import io.github.droidkit.feature.settings.SettingsFeature
import io.github.droidkit.feature.tools.ToolsFeature
import io.github.droidkit.feature.usb.UsbFeature
import io.github.droidkit.privilege.adb.WirelessAdbProvider
import io.github.droidkit.privilege.root.RootProvider
import io.github.droidkit.privilege.shizuku.ShizukuProvider
import io.github.droidkit.privilege.standard.StandardAndroidProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideProbe(@ApplicationContext context: Context) = DeviceSystemProbe(context)

    @Provides
    @Singleton
    fun providePermissionManager(@ApplicationContext context: Context) = PermissionManager(context)

    @Provides
    fun providePermissionChecker(manager: PermissionManager): PermissionChecker = manager

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context) = SettingsRepository(context)

    @Provides
    @Singleton
    fun provideProviders(): Set<CapabilityProvider> = setOf(
        StandardAndroidProvider(),
        ShizukuProvider(),
        WirelessAdbProvider(),
        RootProvider(),
    )

    @Provides
    @Singleton
    fun provideCapabilityManager(
        providers: Set<CapabilityProvider>,
        probe: DeviceSystemProbe,
        permissionChecker: PermissionChecker,
    ) = CapabilityManager(
        providers = providers,
        sdkInt = probe.sdkInt,
        hasFeature = probe::hasFeature,
        permissionChecker = permissionChecker,
    )

    @Provides
    @Singleton
    fun provideContributions(): Set<FeatureContribution> = setOf(
        DashboardFeature,
        ToolsFeature,
        DiagnosticsFeature,
        ReportsFeature,
        SettingsFeature,
        DeviceInfoFeature,
        BatteryFeature,
        DisplayFeature,
        AudioFeature,
        CameraFeature,
        SensorsFeature,
        NetworkFeature,
        BluetoothFeature,
        UsbFeature,
        NfcFeature,
        AppsFeature,
        PermissionsFeature,
        AppOpsFeature,
        PackagesFeature,
        AdbFeature,
        DeveloperFeature,
    )

    @Provides
    @Singleton
    fun provideToolRegistry(contributions: Set<FeatureContribution>) = ToolRegistry(contributions)
}
```

- [x] **Step 6: `navigation/DroidKitNavHost.kt`**

```kotlin
package io.github.droidkit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.feature.adb.adbGraph
import io.github.droidkit.feature.appops.appOpsGraph
import io.github.droidkit.feature.apps.appsGraph
import io.github.droidkit.feature.audio.audioGraph
import io.github.droidkit.feature.battery.batteryGraph
import io.github.droidkit.feature.bluetooth.bluetoothGraph
import io.github.droidkit.feature.camera.cameraGraph
import io.github.droidkit.feature.dashboard.dashboardGraph
import io.github.droidkit.feature.developer.developerGraph
import io.github.droidkit.feature.deviceinfo.deviceInfoGraph
import io.github.droidkit.feature.diagnostics.diagnosticsGraph
import io.github.droidkit.feature.display.displayGraph
import io.github.droidkit.feature.network.networkGraph
import io.github.droidkit.feature.nfc.nfcGraph
import io.github.droidkit.feature.packages.packagesGraph
import io.github.droidkit.feature.permissions.permissionsGraph
import io.github.droidkit.feature.reports.reportsGraph
import io.github.droidkit.feature.sensors.sensorsGraph
import io.github.droidkit.feature.settings.settingsGraph
import io.github.droidkit.feature.tools.toolsGraph
import io.github.droidkit.feature.usb.usbGraph

@Composable
fun DroidKitNavHost(
    nav: NavHostController,
    capabilityManager: CapabilityManager,
) {
    NavHost(navController = nav, startDestination = "home") {
        dashboardGraph(nav)
        toolsGraph(nav)
        diagnosticsGraph(nav)
        reportsGraph(nav)
        settingsGraph(nav)
        deviceInfoGraph(nav)

        val resolve = capabilityManager::resolve
        batteryGraph(nav, resolve)
        displayGraph(nav, resolve)
        audioGraph(nav, resolve)
        cameraGraph(nav, resolve)
        sensorsGraph(nav, resolve)
        networkGraph(nav, resolve)
        bluetoothGraph(nav, resolve)
        usbGraph(nav, resolve)
        nfcGraph(nav, resolve)
        appsGraph(nav, resolve)
        permissionsGraph(nav, resolve)
        appOpsGraph(nav, resolve)
        packagesGraph(nav, resolve)
        adbGraph(nav, resolve)
        developerGraph(nav, resolve)
    }
}
```

- [x] **Step 7: `MainActivity.kt`**

```kotlin
package io.github.droidkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.droidkit.core.datastore.SettingsRepository
import io.github.droidkit.core.datastore.ThemeMode
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.ui.theme.DroidKitTheme
import io.github.droidkit.navigation.DroidKitNavHost
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var capabilityManager: CapabilityManager
    @Inject lateinit var settings: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by settings.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }
            DroidKitTheme(darkTheme = darkTheme) {
                DroidKitScaffold(capabilityManager)
            }
        }
    }
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination("home", "Home", Icons.Filled.Home),
    TopLevelDestination("tools", "Tools", Icons.Filled.Build),
    TopLevelDestination("diagnose", "Diagnose", Icons.Filled.Info),
    TopLevelDestination("settings", "Settings", Icons.Filled.Settings),
)

@Composable
private fun DroidKitScaffold(capabilityManager: CapabilityManager) {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                topLevelDestinations.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            nav.navigate(dest.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                    )
                }
            }
        },
    ) { padding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) {
            DroidKitNavHost(nav, capabilityManager)
        }
    }
}
```

- [x] **Step 8: Full build**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :app:assembleDebug
```

Expected: BUILD SUCCESSFUL, `app/build/outputs/apk/debug/app-debug.apk` exists. Fix any resolution/compile errors surfaced (check version fallbacks in Global Constraints first).

- [x] **Step 9: Commit**

```bash
git add app
git commit -m "feat(app): application wiring, nav host, bottom navigation"
```

---

### Task 20: Repo hygiene, logging, instrumented-test foundation

Per the Journey Checklist (Journey 5): README, CHANGELOG, logging strategy, instrumented-test foundation. LICENSE is a user decision — leave a TODO placeholder in README, do not create LICENSE.

- [x] **Step 1: `README.md`** (root)

```markdown
# DroidKit

Android technician & power toolkit — device inspection, guided hardware
diagnostics, app inspection, USB/NFC utilities and reporting. Useful without
elevated access; Shizuku / wireless ADB / (future) root are optional capability
providers that unlock extra tools.

## Status

v0.1.0 Foundation — multi-module skeleton. All feature areas are registered in
the ToolRegistry and render capability-gated states; foundation services
(registry, capability resolution, permissions, system probes, Room, DataStore,
design system, navigation) are functional.

## Build

Requirements: Android Studio (bundled JBR is the JDK), Android SDK 36.

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"   # Windows/git-bash
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
```

## Structure

- `app/` — application wiring, NavHost, bottom navigation
- `core/` — common (tool model/registry), privileges (capability contracts +
  resolver), permissions, system probes, database, datastore, ui (design system)
- `privilege/` — capability providers: standard (working), shizuku/adb/root (stubs)
- `feature/` — one module per feature area; each exposes a `FeatureContribution`
  and a `NavGraphBuilder` extension
- `build-logic/` — Gradle convention plugins

License: TBD.
```

- [x] **Step 2: `CHANGELOG.md`** (root)

```markdown
# Changelog

## [0.1.0] - Unreleased

### Added
- Multi-module project skeleton (app, 7 core, 4 privilege, 21 feature modules)
- Design system (Material3, dynamic color, shared components)
- ToolRegistry with search/category/favorites foundations
- CapabilityManager + provider chain (Standard ready; Shizuku/ADB/Root stubs)
- PermissionManager, DeviceSystemProbe (hardware/API detection)
- Room schema v1 (diagnostic sessions, results, technician notes)
- DataStore settings (theme, favorites, recents)
- Home / Tools / Diagnose / Settings navigation shells
- Device Inspector screen with live system probes
```

- [x] **Step 3: Basic logging — `core/common/src/main/java/io/github/droidkit/core/common/DLog.kt`**

```kotlin
package io.github.droidkit.core.common

import android.util.Log

/** Thin log facade — swap internals for Timber later without touching call sites. */
object DLog {
    var enabled: Boolean = true

    fun d(tag: String, message: String) {
        if (enabled) Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        if (enabled) Log.i(tag, message)
    }

    fun w(tag: String, message: String, t: Throwable? = null) {
        if (enabled) Log.w(tag, message, t)
    }

    fun e(tag: String, message: String, t: Throwable? = null) {
        if (enabled) Log.e(tag, message, t)
    }
}
```

Wire `DLog.enabled = BuildConfig.DEBUG` in `DroidKitApp.onCreate` — update Task 19's `DroidKitApp.kt`:

```kotlin
package io.github.droidkit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.droidkit.core.common.DLog

@HiltAndroidApp
class DroidKitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DLog.enabled = BuildConfig.DEBUG
    }
}
```

- [x] **Step 4: Instrumented-test foundation**

In the `droidkit.android.application` convention plugin (Task 2, Step 5), add to `defaultConfig`:

```kotlin
testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
```

Add to `app/build.gradle.kts` dependencies:

```kotlin
androidTestImplementation("androidx.test.ext:junit:1.2.1")
androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
```

Create `app/src/androidTest/java/io/github/droidkit/AppSmokeTest.kt`:

```kotlin
package io.github.droidkit

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppSmokeTest {
    @Test
    fun appContextHasPackageName() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("io.github.droidkit", context.packageName)
    }
}
```

(Runs only when a device/emulator is attached — foundation in place, not executed in this plan.)

- [x] **Step 5: Commit**

```bash
git add README.md CHANGELOG.md core/common/src/main/java/io/github/droidkit/core/common/DLog.kt app build-logic
git commit -m "chore: readme, changelog, logging facade, androidTest foundation"
```

---

### Task 21: Verification pass

- [x] **Step 1: Run all unit tests**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew testDebugUnitTest
```

Expected: all modules' tests pass (ToolRegistryTest 4, CapabilityManagerTest 8).

- [x] **Step 2: Clean build sanity**

```bash
./gradlew :app:assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [x] **Step 3: Commit**

```bash
git add -A
git commit -m "chore: skeleton verification pass" || echo "nothing to commit"
```

---

## Notes for the implementer

- `JAVA_HOME` export is required in EVERY shell invocation — no system Java exists.
- If `gradlew` doesn't exist yet, bootstrap per Task 2 Step 8.
- KSP/Hilt modules: `app`, `core:database`, `feature:dashboard`, `feature:tools`, `feature:diagnostics`, `feature:reports`, `feature:settings`, `feature:deviceinfo`. All other modules are KSP-free.
- Stub feature graph functions take `resolve` as a parameter — stub features never see `CapabilityManager` directly (keeps Hilt out of leaf modules).
- Package-private rule of thumb for later phases: features get capabilities via `ToolResolution`, never via provider imports.

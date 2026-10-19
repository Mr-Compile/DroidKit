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

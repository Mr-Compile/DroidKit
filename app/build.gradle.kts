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

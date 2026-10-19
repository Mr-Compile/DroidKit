plugins {
    id("droidkit.android.application")
    id("droidkit.hilt")
}

android {
    namespace = "io.github.droidkit"

    // DK-081: release signing comes from the environment — never from files
    // or values committed to this repo. Required env vars for a signed
    // release build:
    //   DROIDKIT_KEYSTORE             — path to the upload keystore (.jks/.keystore)
    //   DROIDKIT_KEYSTORE_PASSWORD    — keystore password
    //   DROIDKIT_KEY_ALIAS            — key alias
    //   DROIDKIT_KEY_PASSWORD         — key password
    // When unset, release falls back to the debug signing config so CI can
    // still produce a build-verified (but not distributable) artifact.
    val releaseKeystore = providers.environmentVariable("DROIDKIT_KEYSTORE").orNull

    signingConfigs {
        if (releaseKeystore != null) {
            create("release") {
                storeFile = file(releaseKeystore)
                storePassword = providers.environmentVariable("DROIDKIT_KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("DROIDKIT_KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("DROIDKIT_KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Unsigned APKs can't be installed — without env signing we sign
            // with the debug key so validation artifacts stay installable.
            // Distribution builds MUST set the env keystore (different cert).
            signingConfig = signingConfigs.getByName(
                if (releaseKeystore != null) "release" else "debug",
            )
        }
    }
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
    implementation(projects.feature.insights)
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

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

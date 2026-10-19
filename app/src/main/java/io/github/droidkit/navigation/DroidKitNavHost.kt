package io.github.droidkit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.droidkit.core.datastore.SettingsRepository
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.onboarding.OnboardingScreen
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
    settings: SettingsRepository,
    startDestination: String = "home",
) {
    NavHost(navController = nav, startDestination = startDestination) {
        composable("onboarding") {
            OnboardingScreen(settings = settings) {
                nav.navigate("home") {
                    popUpTo("onboarding") { inclusive = true }
                }
            }
        }
        dashboardGraph(nav)
        toolsGraph(nav)
        diagnosticsGraph(nav)
        reportsGraph(nav)
        settingsGraph(nav)
        deviceInfoGraph(nav)

        val resolve = capabilityManager::resolve
        batteryGraph(nav)
        displayGraph(nav)
        audioGraph(nav)
        cameraGraph(nav)
        sensorsGraph(nav)
        networkGraph(nav, resolve)
        bluetoothGraph(nav, resolve)
        usbGraph(nav, resolve)
        nfcGraph(nav, resolve)
        appsGraph(nav, resolve)
        permissionsGraph(nav, resolve)
        appOpsGraph(nav, resolve)
        packagesGraph(nav, resolve)
        adbGraph(nav, resolve)
        developerGraph(nav)
    }
}

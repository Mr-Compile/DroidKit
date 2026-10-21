package io.github.droidkit

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.droidkit.core.datastore.SettingsRepository
import io.github.droidkit.core.datastore.ThemeMode
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.ui.navigation.DroidKitNavigationSuite
import io.github.droidkit.core.ui.navigation.navigateTopLevel
import io.github.droidkit.core.ui.theme.DroidKitTheme
import io.github.droidkit.navigation.DroidKitNavHost
import javax.inject.Inject
import kotlinx.coroutines.delay

private const val SPLASH_MIN_MS = 1_500L
private const val TAG = "DroidKit"

/** Debug builds only: dumps the top insets once so device runs can confirm the
 * app bar gap equals the status bar (i.e. no letterboxing/double inset). */
@Composable
private fun DebugInsetProbe() {
    val density = LocalDensity.current
    val statusTop = WindowInsets.statusBars.getTop(density)
    val cutoutTop = WindowInsets.displayCutout.getTop(density)
    LaunchedEffect(statusTop, cutoutTop) {
        Log.d(TAG, "insets: statusBarTop=${statusTop}px cutoutTop=${cutoutTop}px")
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var capabilityManager: CapabilityManager
    @Inject lateinit var settings: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Some OEMs letterbox below a display cutout even when the theme asks
        // for shortEdges — set it on the window too so the app bar isn't
        // pushed down under a black band on punch-hole devices.
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        enableEdgeToEdge()
        setContent {
            if (BuildConfig.DEBUG) DebugInsetProbe()
            val themeMode by settings.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }
            val dynamicColor by settings.dynamicColor.collectAsState(initial = true)
            DroidKitTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
                DroidKitScaffold(capabilityManager, settings)
            }
        }
    }
}

@Composable
private fun DroidKitScaffold(
    capabilityManager: CapabilityManager,
    settings: SettingsRepository,
) {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    // null = still reading DataStore; show the splash rather than flashing
    // Home before the onboarding redirect. Held a minimum duration so the
    // branding doesn't flash by on fast devices.
    val onboardingDone by settings.onboardingCompleted.collectAsState(initial = null)
    var minSplashElapsed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(SPLASH_MIN_MS)
        minSplashElapsed = true
    }

    if (onboardingDone == null || !minSplashElapsed) {
        SplashScreen()
        return
    }

    // Adaptive chrome (plan 2026-10-20): NavigationBar at compact sizes, rail
    // at medium/expanded; chrome only on top-level destinations — secondary
    // surfaces keep the Up affordance.
    DroidKitNavigationSuite(
        currentRoute = currentRoute,
        onNavigate = nav::navigateTopLevel,
    ) {
        onboardingDone?.let { done ->
            DroidKitNavHost(
                nav = nav,
                capabilityManager = capabilityManager,
                settings = settings,
                startDestination = if (done) "home" else "onboarding",
            )
        }
    }
}

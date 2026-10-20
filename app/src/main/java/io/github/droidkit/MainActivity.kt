package io.github.droidkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlinx.coroutines.delay

private const val SPLASH_MIN_MS = 1_500L

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
                DroidKitScaffold(capabilityManager, settings)
            }
        }
    }
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination("home", "Home", Icons.Filled.Home),
    TopLevelDestination("tools", "Tools", Icons.Filled.Build),
    TopLevelDestination("diagnose", "Diagnose", Icons.Filled.Info),
    TopLevelDestination("settings", "Settings", Icons.Filled.Settings),
)

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

    Scaffold(
        bottomBar = {
            // Bottom nav lives only on top-level destinations (UI v6 plan 16);
            // secondary/detail surfaces use the Up affordance instead.
            if (currentRoute in topLevelDestinations.map { it.route }) {
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
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
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
}

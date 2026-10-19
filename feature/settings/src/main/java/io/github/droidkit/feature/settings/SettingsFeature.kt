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
    composable(SettingsFeature.ROUTE) {
        SettingsScreen(onOpenAdb = { nav.navigate("adb") })
    }
}

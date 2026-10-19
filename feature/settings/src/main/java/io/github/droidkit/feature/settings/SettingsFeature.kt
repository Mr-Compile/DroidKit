package io.github.droidkit.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.ai.AiChatScreen
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.feature.settings.about.AboutFeaturesScreen
import io.github.droidkit.feature.settings.about.AboutHelpScreen
import io.github.droidkit.feature.settings.about.AboutLicensesScreen
import io.github.droidkit.feature.settings.about.AboutScreen
import io.github.droidkit.feature.settings.about.AboutWhatsNewScreen
import io.github.droidkit.feature.settings.about.AboutWhatIsScreen
import io.github.droidkit.feature.settings.about.PrivacyCenterScreen

object SettingsFeature : FeatureContribution {
    const val ROUTE = "settings"
    const val AI_CHAT_ROUTE = "settings/ai_chat"
    const val PRIVACY_ROUTE = "settings/privacy"
    const val ABOUT_ROUTE = "settings/about"
    const val ABOUT_WHAT_IS_ROUTE = "settings/about/what_is"
    const val ABOUT_FEATURES_ROUTE = "settings/about/features"
    const val ABOUT_WHATS_NEW_ROUTE = "settings/about/whats_new"
    const val ABOUT_HELP_ROUTE = "settings/about/help"
    const val ABOUT_LICENSES_ROUTE = "settings/about/licenses"
    override val tools = emptyList<io.github.droidkit.core.common.Tool>()
}

fun NavGraphBuilder.settingsGraph(nav: NavHostController) {
    composable(SettingsFeature.ROUTE) {
        SettingsScreen(
            onOpenAdb = { nav.navigate("adb") },
            onOpenAiChat = { nav.navigate(SettingsFeature.AI_CHAT_ROUTE) },
            onOpenAbout = { nav.navigate(SettingsFeature.ABOUT_ROUTE) },
            onOpenPermissions = { nav.navigate("permissions") },
            onOpenPrivacy = { nav.navigate(SettingsFeature.PRIVACY_ROUTE) },
            onOpenHelp = { nav.navigate(SettingsFeature.ABOUT_HELP_ROUTE) },
            onOpenWhatsNew = { nav.navigate(SettingsFeature.ABOUT_WHATS_NEW_ROUTE) },
        )
    }
    composable(SettingsFeature.AI_CHAT_ROUTE) {
        AiChatScreen(onNavigate = { route -> nav.navigate(route) })
    }
    composable(SettingsFeature.PRIVACY_ROUTE) {
        PrivacyCenterScreen(
            onBack = { nav.navigateUp() },
            onOpenPermissions = { nav.navigate("permissions") },
        )
    }
    composable(SettingsFeature.ABOUT_ROUTE) {
        AboutScreen(
            onBack = { nav.navigateUp() },
            onOpenWhatIs = { nav.navigate(SettingsFeature.ABOUT_WHAT_IS_ROUTE) },
            onOpenFeatures = { nav.navigate(SettingsFeature.ABOUT_FEATURES_ROUTE) },
            onOpenWhatsNew = { nav.navigate(SettingsFeature.ABOUT_WHATS_NEW_ROUTE) },
            onOpenHelp = { nav.navigate(SettingsFeature.ABOUT_HELP_ROUTE) },
            onOpenLicenses = { nav.navigate(SettingsFeature.ABOUT_LICENSES_ROUTE) },
        )
    }
    composable(SettingsFeature.ABOUT_WHAT_IS_ROUTE) {
        AboutWhatIsScreen(onBack = { nav.navigateUp() })
    }
    composable(SettingsFeature.ABOUT_FEATURES_ROUTE) {
        AboutFeaturesScreen(
            onBack = { nav.navigateUp() },
            onOpenTool = { route -> nav.navigate(route) },
        )
    }
    composable(SettingsFeature.ABOUT_WHATS_NEW_ROUTE) {
        AboutWhatsNewScreen(onBack = { nav.navigateUp() })
    }
    composable(SettingsFeature.ABOUT_HELP_ROUTE) {
        AboutHelpScreen(onBack = { nav.navigateUp() })
    }
    composable(SettingsFeature.ABOUT_LICENSES_ROUTE) {
        AboutLicensesScreen(onBack = { nav.navigateUp() })
    }
}

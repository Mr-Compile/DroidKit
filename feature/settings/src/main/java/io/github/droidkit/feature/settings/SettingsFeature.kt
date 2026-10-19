package io.github.droidkit.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.ai.AiChatScreen
import io.github.droidkit.core.common.FeatureContribution

object SettingsFeature : FeatureContribution {
    const val ROUTE = "settings"
    const val AI_CHAT_ROUTE = "settings/ai_chat"
    override val tools = emptyList<io.github.droidkit.core.common.Tool>()
}

fun NavGraphBuilder.settingsGraph(nav: NavHostController) {
    composable(SettingsFeature.ROUTE) {
        SettingsScreen(
            onOpenAdb = { nav.navigate("adb") },
            onOpenAiChat = { nav.navigate(SettingsFeature.AI_CHAT_ROUTE) },
        )
    }
    composable(SettingsFeature.AI_CHAT_ROUTE) {
        AiChatScreen(onNavigate = { route -> nav.navigate(route) })
    }
}

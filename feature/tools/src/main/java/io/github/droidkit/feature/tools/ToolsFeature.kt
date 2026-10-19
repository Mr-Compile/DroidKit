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

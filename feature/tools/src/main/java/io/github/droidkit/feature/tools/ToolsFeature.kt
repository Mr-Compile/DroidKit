package io.github.droidkit.feature.tools

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.SearchEntry
import io.github.droidkit.core.common.SearchResultType
import io.github.droidkit.core.common.ToolIconKeys
import io.github.droidkit.core.ui.navigation.openRoute

object ToolsFeature : FeatureContribution {
    const val ROUTE = "tools"
    override val tools = emptyList<io.github.droidkit.core.common.Tool>()

    /**
     * Non-tool destinations (UI v6 plan file 10) — real routes only.
     * Formerly the hand-listed SearchPages object; the entries now live on
     * the shared contribution contract so they get ranked search, validated
     * icon keys and result types for free.
     */
    override val searchEntries: List<SearchEntry> = listOf(
        SearchEntry(
            id = "page.settings",
            title = "Settings",
            subtitle = "Theme, AI assistant, elevated access",
            route = "settings",
            type = SearchResultType.SETTING,
            iconKey = ToolIconKeys.SETTINGS,
            keywords = setOf("preferences", "options", "theme", "dark mode", "appearance"),
        ),
        SearchEntry(
            id = "page.permissions",
            title = "Permissions & Access",
            subtitle = "Android permissions and elevated providers",
            route = "permissions",
            type = SearchResultType.PAGE,
            iconKey = ToolIconKeys.SECURITY,
            keywords = setOf("permission", "root", "adb", "access", "elevated"),
        ),
        SearchEntry(
            id = "page.privacy",
            title = "Privacy Center",
            subtitle = "What stays on-device and what can go online",
            route = "settings/privacy",
            type = SearchResultType.SETTING,
            iconKey = ToolIconKeys.PRIVACY,
            keywords = setOf("privacy", "data", "local", "offline"),
        ),
        SearchEntry(
            id = "page.about",
            title = "About DroidKit",
            subtitle = "Version, support and legal information",
            route = "settings/about",
            type = SearchResultType.HELP,
            iconKey = ToolIconKeys.INFO,
            keywords = setOf("version", "about", "legal", "developer"),
        ),
        SearchEntry(
            id = "page.whats_new",
            title = "What's New",
            subtitle = "Release notes for this build",
            route = "settings/about/whats_new",
            type = SearchResultType.HELP,
            iconKey = ToolIconKeys.WHATS_NEW,
            keywords = setOf("changelog", "release", "new", "version"),
        ),
        SearchEntry(
            id = "page.help",
            title = "Help & FAQ",
            subtitle = "Guides, permissions and troubleshooting",
            route = "settings/about/help",
            type = SearchResultType.HELP,
            iconKey = ToolIconKeys.HELP,
            keywords = setOf("help", "faq", "troubleshoot", "support", "guide"),
        ),
        SearchEntry(
            id = "page.reports",
            title = "Reports",
            subtitle = "Saved diagnostic sessions and exports",
            route = "reports",
            type = SearchResultType.REPORT,
            iconKey = ToolIconKeys.REPORTS,
            keywords = setOf("report", "history", "session", "export", "pdf"),
        ),
        SearchEntry(
            id = "page.insights",
            title = "Insights",
            subtitle = "On-device analytics for your diagnostics",
            route = "insights",
            type = SearchResultType.PAGE,
            iconKey = ToolIconKeys.INSIGHTS,
            keywords = setOf("insights", "analytics", "trends", "stats"),
        ),
        SearchEntry(
            id = "page.home",
            title = "Home",
            subtitle = "Device summary and pinned tools",
            route = "home",
            type = SearchResultType.PAGE,
            iconKey = ToolIconKeys.HOME,
            keywords = setOf("home", "dashboard", "start"),
        ),
    )
}

fun NavGraphBuilder.toolsGraph(nav: NavHostController) {
    composable(ToolsFeature.ROUTE) {
        ToolsScreen(onOpenTool = { route -> nav.openRoute(route) })
    }
}

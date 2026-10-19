package io.github.droidkit.feature.tools

/**
 * Searchable non-tool destinations — real routes only, kept deliberately
 * small. Settings/Help/About pages are registered here instead of the
 * ToolRegistry because they are app destinations, not tools.
 */
data class SearchPage(
    val title: String,
    val subtitle: String,
    val route: String,
    val keywords: Set<String> = emptySet(),
)

object SearchPages {
    val pages: List<SearchPage> = listOf(
        SearchPage(
            title = "Settings",
            subtitle = "Theme, AI assistant, elevated access",
            route = "settings",
            keywords = setOf("preferences", "options", "theme", "dark mode", "appearance"),
        ),
        SearchPage(
            title = "Permissions & Access",
            subtitle = "Android permissions and elevated providers",
            route = "permissions",
            keywords = setOf("permission", "shizuku", "root", "adb", "access"),
        ),
        SearchPage(
            title = "Privacy Center",
            subtitle = "What stays on-device and what can go online",
            route = "settings/privacy",
            keywords = setOf("privacy", "data", "local", "offline"),
        ),
        SearchPage(
            title = "About DroidKit",
            subtitle = "Version, support and legal information",
            route = "settings/about",
            keywords = setOf("version", "about", "legal", "developer"),
        ),
        SearchPage(
            title = "What's New",
            subtitle = "Release notes for this build",
            route = "settings/about/whats_new",
            keywords = setOf("changelog", "release", "new", "version"),
        ),
        SearchPage(
            title = "Help & FAQ",
            subtitle = "Guides, permissions and troubleshooting",
            route = "settings/about/help",
            keywords = setOf("help", "faq", "troubleshoot", "support", "guide"),
        ),
        SearchPage(
            title = "DroidKit Chat",
            subtitle = "Optional AI assistant",
            route = "settings/ai_chat",
            keywords = setOf("ai", "chat", "assistant", "groq", "intelligence"),
        ),
        SearchPage(
            title = "Reports",
            subtitle = "Saved diagnostic sessions and exports",
            route = "reports",
            keywords = setOf("report", "history", "session", "export", "pdf"),
        ),
        SearchPage(
            title = "Insights",
            subtitle = "On-device analytics for your diagnostics",
            route = "insights",
            keywords = setOf("insights", "analytics", "trends", "stats"),
        ),
        SearchPage(
            title = "Home",
            subtitle = "Device summary and pinned tools",
            route = "home",
            keywords = setOf("home", "dashboard", "start"),
        ),
    )

    /** Case-insensitive match on title/subtitle/keywords; blank → no results. */
    fun search(query: String): List<SearchPage> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return pages.filter {
            it.title.lowercase().contains(q) ||
                it.subtitle.lowercase().contains(q) ||
                it.keywords.any { k -> k.contains(q) }
        }
    }
}

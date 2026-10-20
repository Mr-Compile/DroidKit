package io.github.droidkit.core.common

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor(
    contributions: Set<@JvmSuppressWildcards FeatureContribution>,
) {
    val tools: List<Tool> = contributions
        .flatMap { it.tools }
        .sortedBy { it.title }

    /** Non-tool searchable destinations contributed by features. */
    val searchEntries: List<SearchEntry> = contributions
        .flatMap { it.searchEntries }

    init {
        val ids = tools.groupingBy { it.id }.eachCount()
        val dupes = ids.filterValues { it > 1 }.keys
        require(dupes.isEmpty()) { "Duplicate tool ids in registry: $dupes" }
    }

    /**
     * Deterministic ranked search (UI v6): exact matches beat prefixes,
     * which beat contains; keywords rank below title hits, descriptions
     * lowest. Ties resolve by title so the order is stable and offline.
     * Matching normalizes case and separators, so "wifi" finds "Wi-Fi".
     */
    fun search(query: String): List<Tool> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return tools
        val nq = norm(q)
        if (nq.isEmpty()) return emptyList()
        return tools
            .mapNotNull { tool -> score(tool, nq)?.let { tool to it } }
            .sortedWith(compareByDescending<Pair<Tool, Int>> { it.second }.thenBy { it.first.title })
            .map { it.first }
    }

    /** Ranked search over contributed [SearchEntry] destinations. */
    fun searchEntries(query: String): List<SearchEntry> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        val nq = norm(q)
        if (nq.isEmpty()) return emptyList()
        return searchEntries
            .mapNotNull { entry -> score(entry, nq)?.let { entry to it } }
            .sortedWith(compareByDescending<Pair<SearchEntry, Int>> { it.second }.thenBy { it.first.title })
            .map { it.first }
    }

    /** Lowercase alphanumerics only — "Wi-Fi" and "wifi" compare equal. */
    private fun norm(s: String): String = s.lowercase().filter { it.isLetterOrDigit() }

    private fun score(tool: Tool, nq: String): Int? {
        val title = norm(tool.title)
        val keywords = tool.keywords.map(::norm)
        return when {
            title == nq -> 100
            title.startsWith(nq) -> 80
            title.contains(nq) -> 60
            keywords.any { it == nq } -> 50
            keywords.any { it.startsWith(nq) } -> 40
            keywords.any { it.contains(nq) } -> 30
            norm(tool.description).contains(nq) -> 20
            else -> null
        }
    }

    private fun score(entry: SearchEntry, nq: String): Int? {
        val title = norm(entry.title)
        val keywords = entry.keywords.map(::norm)
        return when {
            title == nq -> 100
            title.startsWith(nq) -> 80
            title.contains(nq) -> 60
            keywords.any { it == nq } -> 50
            keywords.any { it.startsWith(nq) } -> 40
            keywords.any { it.contains(nq) } -> 30
            norm(entry.subtitle).contains(nq) -> 20
            else -> null
        }
    }

    fun byCategory(category: ToolCategory): List<Tool> = tools.filter { it.category == category }

    fun byId(id: String): Tool? = tools.firstOrNull { it.id == id }
}

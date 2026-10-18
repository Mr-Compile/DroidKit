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

    fun search(query: String): List<Tool> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return tools
        return tools.filter {
            it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.keywords.any { k -> k.lowercase().contains(q) }
        }
    }

    fun byCategory(category: ToolCategory): List<Tool> = tools.filter { it.category == category }

    fun byId(id: String): Tool? = tools.firstOrNull { it.id == id }
}

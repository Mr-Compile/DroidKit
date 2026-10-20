package io.github.droidkit.core.common

/** Search result groups, ordered the way they are grouped on screen. */
enum class SearchResultType(val label: String) {
    PAGE("Pages"),
    SETTING("Settings"),
    DIAGNOSTIC("Diagnostics"),
    HELP("Help"),
    REPORT("Reports"),
}

/**
 * A searchable non-tool destination. Like [Tool], [route] must be directly
 * navigable — concrete destinations only, no argument placeholders.
 */
data class SearchEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val route: String,
    val type: SearchResultType = SearchResultType.PAGE,
    val iconKey: String = "",
    val keywords: Set<String> = emptySet(),
) {
    init {
        require('{' !in route && '}' !in route) {
            "SearchEntry $id route must be directly navigable, found placeholder in '$route'"
        }
        require(iconKey.isEmpty() || iconKey in ToolIconKeys.ALL) {
            "SearchEntry $id iconKey '$iconKey' is not in ToolIconKeys.ALL"
        }
    }
}

package io.github.droidkit.core.common

enum class SafetyLevel { SAFE, MINOR, SYSTEM_CHANGE, HIGH_IMPACT, ADVANCED }

enum class ToolCategory { DEVICE, DIAGNOSTICS, APPS, CONNECTIVITY, HARDWARE, DEVELOPER, REPORTS }

enum class ToolPhase { V1, V2, FUTURE }

enum class Capability { STANDARD_API, RUNTIME_PERMISSION, ELEVATED_SHELL, SHIZUKU_SERVICE, ADB_SHELL, ROOT }

enum class TestResultState { PASS, FAIL, NOT_TESTED, NOT_SUPPORTED, PERMISSION_REQUIRED, UNKNOWN }

data class Tool(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val route: String,
    val safetyLevel: SafetyLevel = SafetyLevel.SAFE,
    val requiredCapabilities: Set<Capability> = setOf(Capability.STANDARD_API),
    val requiredPermissions: List<String> = emptyList(),
    val requiredFeatures: Set<String> = emptySet(),
    val minSdk: Int? = null,
    val phase: ToolPhase = ToolPhase.V1,
    val keywords: Set<String> = emptySet(),
    /**
     * Semantic icon key from [ToolIconKeys]; blank falls back to the
     * category icon. One key drives every surface (Home, Tools, Search,
     * Favorites, Recents, info dialog) — never map icons per screen.
     */
    val iconKey: String = "",
) {
    init {
        // A tool's route is a generic launch route (nav.navigate(tool.route)).
        // Parameterized nav patterns belong in the NavGraph, never here.
        require('{' !in route && '}' !in route) {
            "Tool $id route must be directly navigable, found argument placeholder in '$route'"
        }
        require(iconKey.isEmpty() || iconKey in ToolIconKeys.ALL) {
            "Tool $id iconKey '$iconKey' is not in ToolIconKeys.ALL"
        }
    }
}

interface FeatureContribution {
    val tools: List<Tool>

    /**
     * Non-tool searchable destinations this feature owns (pages, settings,
     * diagnostics, help, reports). Entries share the same one-metadata-path
     * contract as tools: concrete routes only, validated icon keys.
     */
    val searchEntries: List<SearchEntry>
        get() = emptyList()
}

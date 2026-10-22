package io.github.droidkit.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val Context.settingsStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    private val context: Context,
) {
    private val themeKey = stringPreferencesKey("theme_mode")
    private val favoritesKey = stringSetPreferencesKey("favorite_tools")
    private val recentsKey = stringPreferencesKey("recent_tools")
    private val toolUseKey = stringPreferencesKey("tool_use_counts")
    private val onboardingKey = androidx.datastore.preferences.core.booleanPreferencesKey("onboarding_completed")
    private val technicianKey = stringPreferencesKey("technician_name")
    private val dynamicColorKey =
        androidx.datastore.preferences.core.booleanPreferencesKey("dynamic_color")

    val onboardingCompleted: Flow<Boolean> =
        context.settingsStore.data.map { it[onboardingKey] ?: false }

    suspend fun setOnboardingCompleted() {
        context.settingsStore.edit { it[onboardingKey] = true }
    }

    val themeMode: Flow<ThemeMode> = context.settingsStore.data.map {
        runCatching { ThemeMode.valueOf(it[themeKey] ?: "SYSTEM") }.getOrDefault(ThemeMode.SYSTEM)
    }

    val favoriteToolIds: Flow<Set<String>> =
        context.settingsStore.data.map { it[favoritesKey] ?: emptySet() }

    val recentToolIds: Flow<List<String>> = context.settingsStore.data.map {
        it[recentsKey]?.split(",")?.filter { s -> s.isNotBlank() } ?: emptyList()
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsStore.edit { it[themeKey] = mode.name }
    }

    /** Material You wallpaper colors — opt-in; the Dark Instrument brand palette is the default. No-op below API 31 — the theme ignores it. */
    val dynamicColor: Flow<Boolean> =
        context.settingsStore.data.map { it[dynamicColorKey] ?: false }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.settingsStore.edit { it[dynamicColorKey] = enabled }
    }

    suspend fun toggleFavorite(toolId: String) {
        context.settingsStore.edit { prefs ->
            val current = prefs[favoritesKey] ?: emptySet()
            prefs[favoritesKey] =
                if (toolId in current) current - toolId else current + toolId
        }
    }

    /** Technician name/signature stamped on diagnostic sessions and reports. */
    val technicianName: Flow<String> =
        context.settingsStore.data.map { it[technicianKey].orEmpty() }

    suspend fun setTechnicianName(name: String) {
        context.settingsStore.edit { it[technicianKey] = name.trim() }
    }

    /** Times each tool was opened — feeds Insights "most used". */
    val toolUseCounts: Flow<Map<String, Int>> = context.settingsStore.data.map { prefs ->
        prefs[toolUseKey].orEmpty().split(",")
            .mapNotNull {
                val n = it.substringAfterLast(':').toIntOrNull() ?: return@mapNotNull null
                it.substringBeforeLast(':') to n
            }
            .toMap()
    }

    suspend fun recordRecent(toolId: String) {
        context.settingsStore.edit { prefs ->
            val current = prefs[recentsKey]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            prefs[recentsKey] = (listOf(toolId) + current.filter { it != toolId }).take(8).joinToString(",")
            val counts = prefs[toolUseKey].orEmpty().split(",")
                .mapNotNull {
                    val n = it.substringAfterLast(':').toIntOrNull() ?: return@mapNotNull null
                    it.substringBeforeLast(':') to n
                }
                .toMap().toMutableMap()
            counts[toolId] = (counts[toolId] ?: 0) + 1
            prefs[toolUseKey] = counts.entries.joinToString(",") { "${it.key}:${it.value}" }
        }
    }

    suspend fun clearFavorites() {
        context.settingsStore.edit { it.remove(favoritesKey) }
    }

    /** Clears the recent-tools list and the per-tool open counters. */
    suspend fun clearRecents() {
        context.settingsStore.edit { prefs ->
            prefs.remove(recentsKey)
            prefs.remove(toolUseKey)
        }
    }

    /**
     * Resets every value in the settings store to defaults (theme, technician
     * name, favorites, recents, use counts). The onboarding-completed flag is
     * kept so reset does not replay the intro. AI configuration and its
     * encrypted key live in a separate store and are not touched.
     */
    suspend fun resetAll() {
        context.settingsStore.edit { prefs ->
            val onboardingDone = prefs[onboardingKey]
            prefs.clear()
            if (onboardingDone != null) prefs[onboardingKey] = onboardingDone
        }
    }
}

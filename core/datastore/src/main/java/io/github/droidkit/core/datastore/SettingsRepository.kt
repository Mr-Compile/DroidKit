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
    private val onboardingKey = androidx.datastore.preferences.core.booleanPreferencesKey("onboarding_completed")

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

    suspend fun toggleFavorite(toolId: String) {
        context.settingsStore.edit { prefs ->
            val current = prefs[favoritesKey] ?: emptySet()
            prefs[favoritesKey] =
                if (toolId in current) current - toolId else current + toolId
        }
    }

    suspend fun recordRecent(toolId: String) {
        context.settingsStore.edit { prefs ->
            val current = prefs[recentsKey]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            prefs[recentsKey] = (listOf(toolId) + current.filter { it != toolId }).take(8).joinToString(",")
        }
    }
}

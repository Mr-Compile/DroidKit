package io.github.droidkit.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    private val wirelessAdbPairedKey =
        androidx.datastore.preferences.core.booleanPreferencesKey("wireless_adb_paired")
    private val wirelessAdbAutoConnectKey =
        androidx.datastore.preferences.core.booleanPreferencesKey("wireless_adb_auto_connect")
    private val adbTerminalEnabledKey =
        androidx.datastore.preferences.core.booleanPreferencesKey("adb_terminal_enabled")

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

    /**
     * True once the device's adbd has accepted our ADB key — i.e. a pairing
     * has been followed by a successful connect. Pairing alone is not enough:
     * the key is only proven when the TLS session authenticates.
     */
    val wirelessAdbPaired: Flow<Boolean> =
        context.settingsStore.data.map { it[wirelessAdbPairedKey] ?: false }

    suspend fun setWirelessAdbPaired(paired: Boolean) {
        context.settingsStore.edit { it[wirelessAdbPairedKey] = paired }
    }

    /**
     * Keep the wireless-ADB session connected without manual steps — the
     * Shizuku-style "start with app" behaviour. Defaults on; only meaningful
     * once [wirelessAdbPaired] is true.
     */
    val wirelessAdbAutoConnect: Flow<Boolean> =
        context.settingsStore.data.map { it[wirelessAdbAutoConnectKey] ?: true }

    suspend fun setWirelessAdbAutoConnect(enabled: Boolean) {
        context.settingsStore.edit { it[wirelessAdbAutoConnectKey] = enabled }
    }

    /**
     * Opt-in gate for the raw ADB terminal — off by default. Enabling is a
     * level-4 decision confirmed in the UI; this flag is what the terminal
     * entry point checks. Every command still goes through
     * `ElevatedOps.runRawCommand` and the audit log.
     */
    val adbTerminalEnabled: Flow<Boolean> =
        context.settingsStore.data.map { it[adbTerminalEnabledKey] ?: false }

    suspend fun setAdbTerminalEnabled(enabled: Boolean) {
        context.settingsStore.edit { it[adbTerminalEnabledKey] = enabled }
    }

    /**
     * Serializes the exportable preferences to a versioned JSON backup.
     * Device-bound (`wireless_adb_paired`), onboarding and AI-key state are
     * never exported — see [SettingsSnapshot].
     */
    suspend fun exportJson(): String {
        val prefs = context.settingsStore.data.first()
        val snapshot = SettingsSnapshot(
            themeMode = runCatching {
                ThemeMode.valueOf(prefs[themeKey] ?: "SYSTEM")
            }.getOrDefault(ThemeMode.SYSTEM).name,
            dynamicColor = prefs[dynamicColorKey] ?: false,
            technicianName = prefs[technicianKey].orEmpty(),
            favoriteTools = prefs[favoritesKey] ?: emptySet(),
            recentTools = prefs[recentsKey]?.split(",")?.filter { it.isNotBlank() }
                ?: emptyList(),
            toolUseCounts = prefs[toolUseKey].orEmpty().split(",")
                .mapNotNull {
                    val n = it.substringAfterLast(':').toIntOrNull()
                        ?: return@mapNotNull null
                    it.substringBeforeLast(':') to n
                }
                .toMap(),
            wirelessAdbAutoConnect = prefs[wirelessAdbAutoConnectKey] ?: true,
        )
        return SettingsBackupCodec.encode(snapshot, System.currentTimeMillis())
    }

    /**
     * Restores a backup written by [exportJson]. Every exported key is
     * overwritten; keys absent from the snapshot type (device state, AI key)
     * are untouched.
     */
    suspend fun importJson(json: String): Result<Unit> =
        SettingsBackupCodec.decode(json).mapCatching { snapshot ->
            context.settingsStore.edit { prefs ->
                prefs[themeKey] = snapshot.themeMode
                prefs[dynamicColorKey] = snapshot.dynamicColor
                prefs[technicianKey] = snapshot.technicianName
                prefs[favoritesKey] = snapshot.favoriteTools
                prefs[recentsKey] = snapshot.recentTools.joinToString(",")
                prefs[toolUseKey] = snapshot.toolUseCounts.entries
                    .joinToString(",") { "${it.key}:${it.value}" }
                prefs[wirelessAdbAutoConnectKey] = snapshot.wirelessAdbAutoConnect
            }
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

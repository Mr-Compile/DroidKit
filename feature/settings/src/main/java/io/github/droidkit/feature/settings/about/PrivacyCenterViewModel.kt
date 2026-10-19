package io.github.droidkit.feature.settings.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.ai.AiSettingsStore
import io.github.droidkit.core.database.ElevatedHistoryRepository
import io.github.droidkit.core.datastore.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PrivacyCenterViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val elevatedHistory: ElevatedHistoryRepository,
    aiStore: AiSettingsStore,
) : ViewModel() {

    /** Counts shown next to each clear control so the action is never blind. */
    val favoritesCount: StateFlow<Int> = settings.favoriteToolIds
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val recentsCount: StateFlow<Int> = settings.recentToolIds
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val elevatedLogCount: StateFlow<Int> = elevatedHistory.recent(500)
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val aiEnabled: StateFlow<Boolean> = aiStore.enabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun clearFavorites() {
        viewModelScope.launch { settings.clearFavorites() }
    }

    fun clearRecents() {
        viewModelScope.launch { settings.clearRecents() }
    }

    fun clearElevatedLog() {
        viewModelScope.launch { elevatedHistory.clear() }
    }
}

package io.github.droidkit.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.ai.AiSettingsStore
import io.github.droidkit.core.ai.AiUseCase
import io.github.droidkit.core.ai.describe
import io.github.droidkit.core.database.ElevatedActionLogEntity
import io.github.droidkit.core.database.ElevatedHistoryRepository
import io.github.droidkit.core.datastore.SettingsRepository
import io.github.droidkit.core.datastore.ThemeMode
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.ElevatedOps
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val capabilityManager: CapabilityManager,
    private val elevatedOps: ElevatedOps,
    private val aiStore: AiSettingsStore,
    private val aiUseCase: AiUseCase,
    private val elevatedHistory: ElevatedHistoryRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> =
        settings.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    /** Live provider status — reactive, updates on grant/connect/disconnect via the bus. */
    val providerStatuses: StateFlow<Map<ProviderKind, ProviderStatus>> = capabilityManager.statuses

    /** True while a grant/setup flow is in flight — gates the buttons. */
    private val _grantBusy = MutableStateFlow(false)
    val grantBusy: StateFlow<Boolean> = _grantBusy.asStateFlow()

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    val dynamicColor: StateFlow<Boolean> =
        settings.dynamicColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settings.setDynamicColor(enabled) }
    }

    // ---- Reports ----

    val technicianName: StateFlow<String> =
        settings.technicianName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun setTechnicianName(name: String) {
        viewModelScope.launch { settings.setTechnicianName(name) }
    }

    fun refreshProviders() {
        capabilityManager.refreshStatuses()
    }

    /** Runs the provider's own grant flow (Shizuku prompt, root-manager prompt). */
    fun requestGrant(kind: ProviderKind) {
        viewModelScope.launch {
            _grantBusy.value = true
            try {
                elevatedOps.requestGrant(kind)
            } finally {
                _grantBusy.value = false
                refreshProviders()
            }
        }
    }

    // ---- DroidKit Intelligence ----

    val aiEnabled: StateFlow<Boolean> =
        aiStore.enabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val aiModel: StateFlow<String> =
        aiStore.model.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AiSettingsStore.DEFAULT_MODEL)
    val aiHasKey: StateFlow<Boolean> =
        aiStore.hasKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val aiAutoSummarize: StateFlow<Boolean> =
        aiStore.autoSummarize.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _aiStatus = MutableStateFlow<String?>(null)
    val aiStatus: StateFlow<String?> = _aiStatus.asStateFlow()

    private val _aiTesting = MutableStateFlow(false)
    val aiTesting: StateFlow<Boolean> = _aiTesting.asStateFlow()

    fun setAiEnabled(value: Boolean) {
        viewModelScope.launch { aiStore.setEnabled(value) }
    }

    fun setAiAutoSummarize(value: Boolean) {
        viewModelScope.launch { aiStore.setAutoSummarize(value) }
    }

    fun setAiModel(value: String) {
        viewModelScope.launch { aiStore.setModel(value) }
    }

    /** Stores the BYOK key encrypted; never echoes or logs it. */
    fun saveAiKey(plain: String) {
        val trimmed = plain.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            aiStore.saveApiKey(trimmed)
            _aiStatus.value = "Key saved"
        }
    }

    fun clearAiKey() {
        viewModelScope.launch {
            aiStore.clearApiKey()
            _aiStatus.value = "Key removed"
        }
    }

    fun testAiConnection() {
        viewModelScope.launch {
            _aiTesting.value = true
            _aiStatus.value = aiUseCase.testConnection()?.describe() ?: "Connected"
            _aiTesting.value = false
        }
    }

    // ---- Elevated action audit ----

    val elevatedLog: StateFlow<List<ElevatedActionLogEntity>> =
        elevatedHistory.recent(50)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun clearElevatedLog() {
        viewModelScope.launch { elevatedHistory.clear() }
    }

    /**
     * Resets appearance, technician name, favorites, recents and use counts to
     * defaults. AI configuration/key and the onboarding flag are not touched.
     */
    fun resetSettings() {
        viewModelScope.launch { settings.resetAll() }
    }
}

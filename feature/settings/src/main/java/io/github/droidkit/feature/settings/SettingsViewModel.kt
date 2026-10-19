package io.github.droidkit.feature.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.droidkit.core.ai.AiError
import io.github.droidkit.core.ai.AiSettingsStore
import io.github.droidkit.core.ai.AiUseCase
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
    @ApplicationContext context: Context,
    private val settings: SettingsRepository,
    private val capabilityManager: CapabilityManager,
    private val elevatedOps: ElevatedOps,
    private val aiStore: AiSettingsStore,
    private val aiUseCase: AiUseCase,
    private val elevatedHistory: ElevatedHistoryRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> =
        settings.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    /** Recomputed on [refreshProviders]; provider status is read live each time. */
    private val _providerStatuses = MutableStateFlow(capabilityManager.providerStatuses())
    val providerStatuses: StateFlow<Map<ProviderKind, ProviderStatus>> = _providerStatuses.asStateFlow()

    /** True while a grant/setup flow is in flight — gates the buttons. */
    private val _grantBusy = MutableStateFlow(false)
    val grantBusy: StateFlow<Boolean> = _grantBusy.asStateFlow()

    val versionName: String = readVersionName(context)

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    // ---- Reports ----

    val technicianName: StateFlow<String> =
        settings.technicianName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun setTechnicianName(name: String) {
        viewModelScope.launch { settings.setTechnicianName(name) }
    }

    fun refreshProviders() {
        _providerStatuses.value = capabilityManager.providerStatuses()
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

    @Suppress("DEPRECATION")
    private fun readVersionName(context: Context): String = runCatching {
        val pm = context.packageManager
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            pm.getPackageInfo(context.packageName, 0)
        }
        info.versionName
    }.getOrNull() ?: "unknown"

    // ---- DroidKit Intelligence ----

    val aiEnabled: StateFlow<Boolean> =
        aiStore.enabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val aiModel: StateFlow<String> =
        aiStore.model.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AiSettingsStore.DEFAULT_MODEL)
    val aiHasKey: StateFlow<Boolean> =
        aiStore.hasKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _aiStatus = MutableStateFlow<String?>(null)
    val aiStatus: StateFlow<String?> = _aiStatus.asStateFlow()

    private val _aiTesting = MutableStateFlow(false)
    val aiTesting: StateFlow<Boolean> = _aiTesting.asStateFlow()

    fun setAiEnabled(value: Boolean) {
        viewModelScope.launch { aiStore.setEnabled(value) }
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
            _aiStatus.value = when (val err = aiUseCase.testConnection()) {
                null -> "Connected"
                AiError.NO_KEY -> "No API key — add one first"
                AiError.OFFLINE -> "Offline — check network"
                AiError.AUTH_ERROR -> "Authentication failed — check the key"
                AiError.RATE_LIMITED -> "Rate limited — try later"
                AiError.MALFORMED_RESPONSE -> "Unexpected response"
                AiError.PROVIDER_ERROR -> "Provider error"
                AiError.DISABLED -> "Enable AI first"
            }
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
}

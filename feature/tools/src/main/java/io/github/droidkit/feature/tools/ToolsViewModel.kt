package io.github.droidkit.feature.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolRegistry
import io.github.droidkit.core.datastore.SettingsRepository
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.ToolResolution
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ToolsViewModel @Inject constructor(
    private val registry: ToolRegistry,
    private val capabilityManager: CapabilityManager,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _category = MutableStateFlow<ToolCategory?>(null)
    val category: StateFlow<ToolCategory?> = _category

    val categories: List<ToolCategory> = ToolCategory.entries

    val favorites: Flow<Set<String>> = settings.favoriteToolIds

    fun setQuery(q: String) { _query.value = q }

    fun setCategory(c: ToolCategory?) { _category.value = c }

    fun visibleTools(): List<Tool> {
        val base = _category.value?.let { registry.byCategory(it) } ?: registry.search(_query.value)
        return if (_category.value != null && _query.value.isNotBlank()) {
            base.filter {
                it.title.contains(_query.value, true) || it.description.contains(_query.value, true)
            }
        } else base
    }

    fun resolve(tool: Tool): ToolResolution = capabilityManager.resolve(tool)

    fun toggleFavorite(tool: Tool) {
        viewModelScope.launch { settings.toggleFavorite(tool.id) }
    }

    fun recordOpen(tool: Tool) {
        viewModelScope.launch { settings.recordRecent(tool.id) }
    }
}

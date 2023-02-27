package org.cru.godtools.ui.dashboard.tools

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL_DETAILS
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.banner.BannerType
import org.greenrobot.eventbus.EventBus

private const val ATTR_SELECTED_CATEGORY = "selectedCategory"

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ToolsViewModel @Inject constructor(
    private val eventBus: EventBus,
    settings: Settings,
    toolsRepository: ToolsRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    val primaryLanguage = settings.primaryLanguageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), settings.primaryLanguage)

    val banner = settings.isFeatureDiscoveredFlow(Settings.FEATURE_TOOL_FAVORITE)
        .map { if (!it) BannerType.TOOL_LIST_FAVORITES else null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val spotlightTools = toolsRepository.getToolsFlow()
        .map { it.filter { !it.isHidden && it.isSpotlight }.sortedWith(Tool.COMPARATOR_DEFAULT_ORDER) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val tools = toolsRepository.getToolsFlow()
        .map { it.filterNot { it.isHidden } }
        .combine(
            toolsRepository.getMetaToolsFlow().map { it.associateBy({ it.code }, { it.defaultVariantCode }) }
        ) { tools, defaultVariants ->
            tools.filter { it.metatoolCode == null || it.code == defaultVariants[it.metatoolCode] }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val categories = tools.mapLatest { it.mapNotNull { it.category }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // region Filters
    val filterCategory = savedState.getStateFlow<String?>(ATTR_SELECTED_CATEGORY, null)
    fun setFilterCategory(category: String?) = savedState.set(ATTR_SELECTED_CATEGORY, category)

    val filteredTools = tools.combine(filterCategory) { tools, category ->
        tools.filter { category == null || it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
    // endregion Filters

    // region Analytics
    fun recordOpenToolDetailsInAnalytics(tool: String?, source: String) {
        eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL_DETAILS, tool, source))
    }
    // endregion Analytics
}

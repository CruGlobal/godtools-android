package org.cru.godtools.ui.dashboard.tools

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL_DETAILS
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.banner.BannerType
import org.greenrobot.eventbus.EventBus

private const val KEY_SELECTED_CATEGORY = "selectedCategory"
private const val KEY_SELECTED_LANGUAGE = "selectedLanguage"

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ToolsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val eventBus: EventBus,
    settings: Settings,
    toolsRepository: ToolsRepository,
    languagesRepository: LanguagesRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    val banner = settings.isFeatureDiscoveredFlow(Settings.FEATURE_TOOL_FAVORITE)
        .map { if (!it) BannerType.TOOL_LIST_FAVORITES else null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val spotlightTools = toolsRepository.getToolsFlow()
        .map { it.filter { !it.isHidden && it.isSpotlight }.sortedWith(Tool.COMPARATOR_DEFAULT_ORDER) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // region Tools
    val selectedCategory = savedState.getStateFlow<String?>(KEY_SELECTED_CATEGORY, null)
    fun setSelectedCategory(category: String?) = savedState.set(KEY_SELECTED_CATEGORY, category)

    internal val selectedLocale = savedState.getStateFlow<Locale?>(KEY_SELECTED_LANGUAGE, null)
    val selectedLanguage = selectedLocale
        .flatMapLatest { it?.let { languagesRepository.findLanguageFlow(it) } ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    fun setSelectedLanguage(language: Language?) = savedState.set(KEY_SELECTED_LANGUAGE, language?.code)

    private val toolsForLocale = selectedLocale
        .flatMapLatest {
            if (it != null) toolsRepository.getToolsFlowForLanguage(it) else toolsRepository.getToolsFlow()
        }
        .map { it.filterNot { it.isHidden }.sortedBy { it.defaultOrder } }
        .combine(
            toolsRepository.getMetaToolsFlow().map { it.associateBy({ it.code }, { it.defaultVariantCode }) }
        ) { tools, defaultVariants ->
            tools.filter { it.metatoolCode == null || it.code == defaultVariants[it.metatoolCode] }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories = toolsForLocale.mapLatest { it.mapNotNull { it.category }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val languages = selectedCategory
        .flatMapLatest {
            when {
                it != null -> languagesRepository.getLanguagesFlowForToolCategory(it)
                else -> languagesRepository.getLanguagesFlow()
            }
        }
        .combine(settings.appLanguageFlow) { langs, appLang ->
            langs.sortedWith(
                compareByDescending<Language> { it.code == appLang }
                    .then(compareByDescending { it.isAdded })
                    .then(Language.COMPARATOR_DISPLAY_NAME(context))
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tools = toolsForLocale
        .combine(selectedCategory) { tools, category -> tools.filter { category == null || it.category == category } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    // endregion Tools

    // region Analytics
    fun recordOpenToolDetailsInAnalytics(tool: String?, source: String) {
        eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL_DETAILS, tool, source))
    }
    // endregion Analytics
}

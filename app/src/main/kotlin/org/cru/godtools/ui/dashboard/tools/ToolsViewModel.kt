package org.cru.godtools.ui.dashboard.tools

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Language.Companion.filterByDisplayAndNativeName

private const val KEY_SELECTED_CATEGORY = "selectedCategory"
private const val KEY_SELECTED_LANGUAGE = "selectedLanguage"
private const val KEY_LANGUAGE_QUERY = "languageQuery"

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ToolsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    settings: Settings,
    toolsRepository: ToolsRepository,
    languagesRepository: LanguagesRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    // region Tools
    val selectedCategory = savedState.getStateFlow<String?>(KEY_SELECTED_CATEGORY, null)
    fun setSelectedCategory(category: String?) = savedState.set(KEY_SELECTED_CATEGORY, category)

    internal val selectedLocale = savedState.getStateFlow<Locale?>(KEY_SELECTED_LANGUAGE, null)
    fun setSelectedLocale(locale: Locale?) = savedState.set(KEY_SELECTED_LANGUAGE, locale)

    val languageQuery = savedState.getStateFlow(KEY_LANGUAGE_QUERY, "")
    fun setLanguageQuery(query: String) = savedState.set(KEY_LANGUAGE_QUERY, query)

    private val toolsForLocale = selectedLocale
        .flatMapLatest {
            if (it != null) toolsRepository.getNormalToolsFlowByLanguage(it) else toolsRepository.getNormalToolsFlow()
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
            langs.sortedWith(Language.displayNameComparator(context, appLang)) to appLang
        }
        .combine(languageQuery) { (langs, appLang), q -> langs.filterByDisplayAndNativeName(q, context, appLang) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tools = toolsForLocale
        .combine(selectedCategory) { tools, category -> tools.filter { category == null || it.category == category } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    // endregion Tools
}

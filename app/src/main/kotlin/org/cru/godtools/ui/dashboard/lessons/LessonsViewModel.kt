package org.cru.godtools.ui.dashboard.lessons

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_LESSON
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_LESSONS
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Language.Companion.filterByDisplayAndNativeName
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.greenrobot.eventbus.EventBus

const val KEY_SAVED_LESSON_LANGUAGE_LOCALE = "savedLessonLanguageLocale"

@HiltViewModel
class LessonsViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val eventBus: EventBus,
    toolsRepository: ToolsRepository,
    languagesRepository: LanguagesRepository,
    private val translationsRepository: TranslationsRepository,
    private val savedStateHandle: SavedStateHandle,
    val settings: Settings,
) : ViewModel() {
    init {
        settings.appLanguageFlow
            .onEach { updateSelectedLanguage(Language(it)) }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedLanguage = savedStateHandle.getStateFlow(KEY_SAVED_LESSON_LANGUAGE_LOCALE, settings.appLanguage)
        .flatMapLatest { locale ->
            languagesRepository.findLanguageFlow(locale).map { it ?: Language(locale) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Language(settings.appLanguage))

    fun updateSelectedLanguage(newLanguage: Language) {
        savedStateHandle.set(KEY_SAVED_LESSON_LANGUAGE_LOCALE, newLanguage.code)
    }

    var query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val lessons = selectedLanguage
        .flatMapLatest { toolsRepository.getLessonsFlowByLanguage(it.code) }
        .map {
            it.filterNot { it.isHidden }
                .sortedBy { it.defaultOrder }
                .mapNotNull { it.code }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val toolCountsFlow = toolsRepository.getLessonsFlow()
        .map { it.mapNotNullTo(mutableSetOf()) { it.code } }
        .distinctUntilChanged()
        .flatMapLatest { translationsRepository.getTranslationsFlowForTools(it) }
        .map { translations ->
            translations
                .groupBy { it.languageCode }
                .mapValues { it.value.distinctBy { it.toolCode }.count() }
        }

    val filteredLanguages = combine(
        languagesRepository.getLanguagesFlow(),
        settings.appLanguageFlow,
        query,
        toolCountsFlow
    ) { languages, appLanguage, query, toolCounts ->
        languages
            .filterByDisplayAndNativeName(query, context, appLanguage)
            .map { FilterMenu.UiState.Item(it, toolCounts[it.code] ?: 0) }
            .filter { it.count > 0 }
            .toImmutableList()
    }

    // region Analytics
    fun recordOpenLessonInAnalytics(tool: String?) {
        eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_LESSON, tool, SOURCE_LESSONS))
    }
    // endregion Analytics
}

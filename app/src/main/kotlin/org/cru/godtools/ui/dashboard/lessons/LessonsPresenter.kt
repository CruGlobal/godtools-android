package org.cru.godtools.ui.dashboard.lessons

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.android.IntentScreen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.dagger.coroutines.DispatcherType
import org.ccci.gto.android.common.dagger.coroutines.DispatcherType.Type.IO
import org.ccci.gto.android.common.sync.SyncTracker
import org.ccci.gto.android.common.sync.rememberSyncTask
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_LESSON
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_FEATURED
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_LESSONS
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.LessonsScreen
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.repository.rememberLanguage
import org.cru.godtools.model.Language
import org.cru.godtools.model.Language.Companion.filterByDisplayAndNativeName
import org.cru.godtools.model.Tool
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.cru.godtools.ui.dashboard.lessons.LessonsPresenter.UiState
import org.cru.godtools.ui.dashboard.personalization.LocalDashboardPersonalizationState
import org.cru.godtools.ui.settings.country.CountrySettingsScreen
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.cru.godtools.ui.tools.ToolCardPresenter.ToolCardEvent
import org.cru.godtools.util.createToolIntent
import org.greenrobot.eventbus.EventBus

class LessonsPresenter @AssistedInject internal constructor(
    @param:ApplicationContext
    private val context: Context,
    private val eventBus: EventBus,
    private val languagesRepository: LanguagesRepository,
    private val lessonsFlowProducer: LessonsFlowProducer,
    private val settings: Settings,
    private val syncService: GodToolsSyncService,
    private val toolCardPresenter: ToolCardPresenter,
    private val toolsRepository: ToolsRepository,
    private val translationsRepository: TranslationsRepository,
    @param:DispatcherType(IO) private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val circuitContext: CircuitContext,
    @Assisted private val navigator: Navigator,
) : Presenter<UiState> {
    // region UiState / UiEvent
    @ConsistentCopyVisibility
    data class UiState internal constructor(
        val mode: Mode = Mode.ALL_LESSONS,
        val dataLoaded: Boolean = true,
        val languageFilter: FilterMenu.UiState<Language> = FilterMenu.UiState(),
        val featuredLessons: List<ToolCardPresenter.UiState> = emptyList(),
        val lessons: List<ToolCardPresenter.UiState> = emptyList(),
        internal val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState {
        enum class Mode { PERSONALIZATION, ALL_LESSONS }
    }

    internal sealed interface UiEvent : CircuitUiEvent {
        data class ChangeMode(val mode: UiState.Mode) : UiEvent
        data object OpenLocalizationSettings : UiEvent
    }
    // endregion UiState / UiEvent

    @Composable
    override fun present(): UiState {
        val personalization = LocalDashboardPersonalizationState.current
        val mode = if (personalization.lessonsPersonalized) UiState.Mode.PERSONALIZATION else UiState.Mode.ALL_LESSONS

        val appLanguage by settings.appLanguageFlow.collectAsState()
        val languageFilter = rememberLanguagesFilter(mode)

        RegisterSyncTask(languageFilter.selectedItem?.code ?: appLanguage)

        // featured lessons follow the personalized language filter, independent of the current mode's filter
        val featuredLocale by remember {
            settings.getDashboardPersonalizedLessonsFilterLocaleFlow()
                .combine(settings.appLanguageFlow) { stored, appLanguage -> stored ?: appLanguage }
        }.collectAsState(null)
        // wait for the stored filter to load so we don't briefly sync/load the app language instead
        featuredLocale?.let { RegisterFeaturedLessonsSyncTask(it) }
        val featuredLessons = rememberFeaturedLessons(featuredLocale)
        val lessons = rememberLessons(mode, languageFilter.selectedItem?.code ?: appLanguage)

        return UiState(
            mode = mode,
            dataLoaded = featuredLessons != null && lessons != null,
            languageFilter = languageFilter,
            featuredLessons = if (mode == UiState.Mode.PERSONALIZATION) featuredLessons.orEmpty() else emptyList(),
            lessons = lessons.orEmpty(),
        ) {
            when (it) {
                is UiEvent.ChangeMode -> personalization.lessonsPersonalized = it.mode == UiState.Mode.PERSONALIZATION
                is UiEvent.OpenLocalizationSettings -> navigator.goTo(CountrySettingsScreen)
            }
        }
    }

    @Composable
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun rememberLanguagesFilter(mode: UiState.Mode): FilterMenu.UiState<Language> {
        val selectedLocaleFlow = remember(mode) {
            when (mode) {
                UiState.Mode.PERSONALIZATION -> settings.getDashboardPersonalizedLessonsFilterLocaleFlow()
                UiState.Mode.ALL_LESSONS -> settings.getDashboardLessonsFilterLocaleFlow()
            }.combine(settings.appLanguageFlow) { stored, appLanguage -> stored ?: appLanguage }
        }

        val scope = rememberCoroutineScope()
        val query = rememberSaveable { mutableStateOf("") }
        val languagesFlow = rememberLanguagesFlow()

        return FilterMenu.UiState(
            menuExpanded = rememberSaveable { mutableStateOf(false) },
            query = query,
            selectedItem = remember(selectedLocaleFlow) {
                selectedLocaleFlow.flatMapLatest { locale ->
                    languagesRepository.findLanguageFlow(locale).map { it ?: Language(locale) }
                }
            }.collectAsState(Language(settings.appLanguage)).value,
            items = remember {
                combine(
                    languagesFlow,
                    settings.appLanguageFlow,
                    snapshotFlow { query.value },
                ) { languages, appLang, query -> languages.filterByDisplayAndNativeName(query, context, appLang) }
                    .flowOn(ioDispatcher)
                    .combine(
                        toolsRepository.getLessonsFlow()
                            .map { it.filterNot { it.isHidden }.mapNotNullTo(mutableSetOf()) { it.code } }
                            .distinctUntilChanged()
                            .flatMapLatest { translationsRepository.getTranslationsFlowForTools(it) }
                            .map {
                                it.groupBy { it.languageCode }
                                    .mapValues { it.value.distinctBy { it.toolCode }.count() }
                            }
                    ) { languages, toolCounts ->
                        languages
                            .map { FilterMenu.UiState.Item(it, toolCounts[it.code] ?: 0) }
                            .filter { it.count > 0 }
                            .toImmutableList()
                    }
            }.collectAsState(persistentListOf()).value,
            eventSink = {
                when (it) {
                    is FilterMenu.Event.SelectItem -> scope.launch(start = CoroutineStart.UNDISPATCHED) {
                        withContext(NonCancellable) {
                            when (mode) {
                                UiState.Mode.PERSONALIZATION ->
                                    settings.updateDashboardPersonalizedLessonsFilterLocale(it.item.code)

                                UiState.Mode.ALL_LESSONS -> settings.updateDashboardLessonsFilterLocale(it.item.code)
                            }
                        }
                    }
                }
            }
        )
    }

    @Composable
    private fun rememberLanguagesFlow() = remember {
        languagesRepository.getLanguagesFlow()
            .combine(settings.appLanguageFlow) { languages, appLanguage ->
                languages.sortedWith(Language.displayNameComparator(context, appLanguage))
            }
            .flowOn(ioDispatcher)
    }

    @Composable
    private fun rememberFeaturedLessons(locale: Locale?): List<ToolCardPresenter.UiState>? {
        val lessons by remember(locale) { locale?.let { lessonsFlowProducer.getFeaturedFlow(it) } ?: emptyFlow() }
            .collectAsState(null)
        val language = languagesRepository.rememberLanguage(locale)
        return locale?.let { lessons?.toLessonCardStates(it, SOURCE_FEATURED, secondLanguage = language) }
    }

    @Composable
    private fun rememberLessons(mode: UiState.Mode, locale: Locale): List<ToolCardPresenter.UiState>? {
        val lessons by remember(mode, locale) { lessonsFlowProducer.getFlow(mode, locale) }.collectAsState(null)
        return lessons?.toLessonCardStates(locale, SOURCE_LESSONS)
    }

    @Composable
    @SuppressLint("FlowOperatorInvokedInComposition")
    private fun List<Tool>.toLessonCardStates(locale: Locale, source: String, secondLanguage: Language? = null) =
        map { tool ->
            key(tool.code) {
                lateinit var toolState: ToolCardPresenter.UiState
                toolState = toolCardPresenter.present(
                    tool = tool,
                    customLocale = locale,
                    secondLanguage = secondLanguage,
                    eventSink = {
                        when (it) {
                            ToolCardEvent.Click, ToolCardEvent.OpenTool -> {
                                eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_LESSON, tool.code, source))
                                navigator.goTo(
                                    IntentScreen(
                                        context.createToolIntent(
                                            tool = tool,
                                            languages = listOfNotNull(toolState.translation?.languageCode),
                                            resumeProgress = true
                                        ) ?: return@present
                                    )
                                )
                            }

                            ToolCardEvent.OpenToolDetails -> Unit
                        }
                    }
                )
                toolState
            }
        }

    @Composable
    private fun RegisterSyncTask(locale: Locale) {
        circuitContext.rememberSyncTask(locale) { syncData(locale, it) }
    }

    @Composable
    private fun RegisterFeaturedLessonsSyncTask(locale: Locale) {
        circuitContext.rememberSyncTask(locale) { syncFeaturedLessons(locale, it) }
    }

    private fun SyncTracker.syncData(locale: Locale, force: Boolean = false) = launchSync {
        val country = settings.getCountrySettingFlow().first()
        syncService.syncToolOrder(locale, country, force)
    }

    private fun SyncTracker.syncFeaturedLessons(locale: Locale, force: Boolean = false) = launchSync {
        val country = settings.getCountrySettingFlow().first() ?: return@launchSync
        syncService.syncFeaturedTools(locale, country, force)
    }

    @AssistedFactory
    @CircuitInject(LessonsScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(circuitContext: CircuitContext, navigator: Navigator): LessonsPresenter
    }
}

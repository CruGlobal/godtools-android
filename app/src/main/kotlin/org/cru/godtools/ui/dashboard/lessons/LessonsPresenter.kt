package org.cru.godtools.ui.dashboard.lessons

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.ccci.gto.android.common.dagger.coroutines.DispatcherType
import org.ccci.gto.android.common.dagger.coroutines.DispatcherType.Type.IO
import org.ccci.gto.android.common.sync.SyncTracker
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_LESSON
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_LESSONS
import org.cru.godtools.base.CONFIG_UI_DASHBOARD_PERSONALIZATION_ENABLED
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.LessonsScreen
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Language.Companion.filterByDisplayAndNativeName
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.ui.dashboard.SyncTaskRegistry.Companion.syncTaskRegistry
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.cru.godtools.ui.dashboard.lessons.LessonsPresenter.UiState
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter
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
    private val remoteConfig: FirebaseRemoteConfig,
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
        val isPersonalizationEnabled: Boolean = false,
        val languageFilter: FilterMenu.UiState<Language> = FilterMenu.UiState(),
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
        val isPersonalizationEnabled = rememberSaveable {
            remoteConfig.getBoolean(CONFIG_UI_DASHBOARD_PERSONALIZATION_ENABLED)
        }
        var mode by rememberSaveable {
            mutableStateOf(if (isPersonalizationEnabled) UiState.Mode.PERSONALIZATION else UiState.Mode.ALL_LESSONS)
        }

        val appLanguage by settings.appLanguageFlow.collectAsState()
        val languageFilter = rememberLanguagesFilter()

        RegisterSyncTask(languageFilter.selectedItem?.code ?: appLanguage)

        return UiState(
            mode = mode,
            isPersonalizationEnabled = isPersonalizationEnabled,
            languageFilter = languageFilter,
            lessons = rememberLessons(mode, languageFilter.selectedItem?.code ?: appLanguage),
        ) {
            when (it) {
                is UiEvent.ChangeMode -> mode = it.mode
                is UiEvent.OpenLocalizationSettings -> navigator.goTo(CountrySettingsScreen)
            }
        }
    }

    @Composable
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun rememberLanguagesFilter(): FilterMenu.UiState<Language> {
        val appLanguage by settings.appLanguageFlow.collectAsState()
        var locale by rememberSaveable { mutableStateOf(appLanguage to appLanguage) }
        LaunchedEffect(appLanguage) { if (locale.first != appLanguage) locale = appLanguage to appLanguage }

        val query = rememberSaveable { mutableStateOf("") }
        val languagesFlow = rememberLanguagesFlow()

        return FilterMenu.UiState(
            menuExpanded = rememberSaveable { mutableStateOf(false) },
            query = query,
            selectedItem = remember {
                snapshotFlow { locale.second }
                    .flatMapLatest { locale ->
                        languagesRepository.findLanguageFlow(locale).map { it ?: Language(locale) }
                    }
            }.collectAsState(Language(locale.second)).value,
            items = remember {
                combine(
                    languagesFlow,
                    settings.appLanguageFlow,
                    snapshotFlow { query.value },
                ) { languages, appLang, query -> languages.filterByDisplayAndNativeName(query, context, appLang) }
                    .flowOn(ioDispatcher)
                    .combine(
                        toolsRepository.getLessonsFlow()
                            .map { it.mapNotNullTo(mutableSetOf()) { it.code } }
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
                    is FilterMenu.Event.SelectItem -> locale = appLanguage to it.item.code
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
    private fun rememberLessons(mode: UiState.Mode, locale: Locale): List<ToolCardPresenter.UiState> {
        val lessons by remember(mode, locale) { lessonsFlowProducer.getFlow(mode, locale) }.collectAsState(emptyList())
        return lessons.map { tool ->
            key(tool.code) {
                lateinit var toolState: ToolCardPresenter.UiState
                toolState = toolCardPresenter.present(
                    tool = tool,
                    customLocale = locale,
                    eventSink = {
                        when (it) {
                            ToolCardEvent.Click, ToolCardEvent.OpenTool -> {
                                eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_LESSON, tool.code, SOURCE_LESSONS))
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
    }

    @Composable
    private fun RegisterSyncTask(locale: Locale) {
        val syncRegistry = circuitContext.syncTaskRegistry
        DisposableEffect(syncRegistry, locale) {
            if (syncRegistry == null) return@DisposableEffect onDispose { }
            val id = syncRegistry.registerSyncTask { force -> syncData(locale, force) }
            onDispose { syncRegistry.unregisterSyncTask(id) }
        }
    }

    private fun SyncTracker.syncData(locale: Locale, force: Boolean = false) = launchSync {
        val country = settings.getCountrySettingFlow().first()
        syncService.syncToolOrder(locale, country, force)
    }

    @AssistedFactory
    @CircuitInject(LessonsScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(circuitContext: CircuitContext, navigator: Navigator): LessonsPresenter
    }
}

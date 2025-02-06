package org.cru.godtools.ui.dashboard.lessons

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.android.IntentScreen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.cru.godtools.util.createToolIntent
import org.greenrobot.eventbus.EventBus

class LessonsPresenter @AssistedInject constructor(
    @ApplicationContext
    private val context: Context,
    private val eventBus: EventBus,
    private val languagesRepository: LanguagesRepository,
    private val settings: Settings,
    private val toolCardPresenter: ToolCardPresenter,
    private val toolsRepository: ToolsRepository,
    private val translationsRepository: TranslationsRepository,
    @Assisted
    private val navigator: Navigator,
) : Presenter<LessonsScreen.UiState> {
    @Composable
    override fun present(): LessonsScreen.UiState {
        val appLanguage by settings.appLanguageFlow.collectAsState()
        val languageFilter = rememberLanguagesFilter()

        return LessonsScreen.UiState(
            languageFilter = languageFilter,
            lessons = rememberLessons(languageFilter.selectedItem?.code ?: appLanguage),
        )
    }

    @Composable
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun rememberLanguagesFilter(): FilterMenu.UiState<Language> {
        val appLanguage by settings.appLanguageFlow.collectAsState()
        val locale = rememberSaveable(appLanguage) { mutableStateOf(appLanguage) }
        val query = rememberSaveable { mutableStateOf("") }

        return FilterMenu.UiState(
            menuExpanded = rememberSaveable { mutableStateOf(false) },
            query = query,
            selectedItem = remember {
                snapshotFlow { locale.value }
                    .flatMapLatest { locale ->
                        languagesRepository.findLanguageFlow(locale).map { it ?: Language(locale) }
                    }
            }.collectAsState(Language(locale.value)).value,
            items = remember {
                combine(
                    languagesRepository.getLanguagesFlow(),
                    snapshotFlow { query.value },
                    settings.appLanguageFlow,
                    toolsRepository.getLessonsFlow()
                        .map { it.mapNotNullTo(mutableSetOf()) { it.code } }
                        .distinctUntilChanged()
                        .flatMapLatest { translationsRepository.getTranslationsFlowForTools(it) }
                        .map {
                            it.groupBy { it.languageCode }
                                .mapValues { it.value.distinctBy { it.toolCode }.count() }
                        },
                ) { languages, query, appLanguage, toolCounts ->
                    languages
                        .filterByDisplayAndNativeName(query, context, appLanguage)
                        .map { FilterMenu.UiState.Item(it, toolCounts[it.code] ?: 0) }
                        .filter { it.count > 0 }
                        .toImmutableList()
                }
            }.collectAsState(persistentListOf()).value,
            eventSink = {
                when (it) {
                    is FilterMenu.Event.SelectItem -> locale.value = it.item.code
                }
            }
        )
    }

    @Composable
    private fun rememberLessons(locale: Locale): ImmutableList<ToolCard.State> {
        val lessons by remember(locale) {
            toolsRepository.getLessonsFlowByLanguage(locale)
                .map { it.filterNot { it.isHidden }.sortedBy { it.defaultOrder } }
        }.collectAsState(emptyList())

        return lessons.mapTo(persistentListOf<ToolCard.State>().builder()) { tool ->
            key(tool.code) {
                lateinit var toolState: ToolCard.State
                toolState = toolCardPresenter.present(
                    tool = tool,
                    customLocale = locale,
                    eventSink = {
                        when (it) {
                            ToolCard.Event.Click -> {
                                eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_LESSON, tool.code, SOURCE_LESSONS))
                                navigator.goTo(
                                    IntentScreen(
                                        tool.createToolIntent(
                                            context = context,
                                            languages = listOfNotNull(toolState.translation?.languageCode),
                                            saveLanguageSettings = true
                                        ) ?: return@present
                                    )
                                )
                            }
                            ToolCard.Event.OpenTool,
                            ToolCard.Event.OpenToolDetails,
                            ToolCard.Event.PinTool,
                            ToolCard.Event.UnpinTool -> Unit
                        }
                    }
                )
                toolState
            }
        }.build()
    }

    @AssistedFactory
    @CircuitInject(LessonsScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): LessonsPresenter
    }
}

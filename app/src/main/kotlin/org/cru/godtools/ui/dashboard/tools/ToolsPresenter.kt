package org.cru.godtools.ui.dashboard.tools

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.dagger.coroutines.DispatcherType
import org.ccci.gto.android.common.dagger.coroutines.DispatcherType.Type.IO
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL_DETAILS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_ALL_TOOLS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_SPOTLIGHT
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.repository.rememberLanguage
import org.cru.godtools.model.Language
import org.cru.godtools.model.Language.Companion.filterByDisplayAndNativeName
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.banner.BannerType
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.greenrobot.eventbus.EventBus

class ToolsPresenter @AssistedInject internal constructor(
    @param:ApplicationContext
    private val context: Context,
    private val eventBus: EventBus,
    private val settings: Settings,
    private val toolCardPresenter: ToolCardPresenter,
    private val languagesRepository: LanguagesRepository,
    private val toolsRepository: ToolsRepository,
    private val translationsRepository: TranslationsRepository,
    private val filteredToolsFlowProducer: FilteredToolsFlowProducer,
    @param:DispatcherType(IO) private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val navigator: Navigator,
) : Presenter<UiState> {
    // region UiState / UiEvent
    data class UiState(
        val banner: BannerType? = null,
        val dataLoaded: Boolean = true,
        val spotlightTools: List<ToolCard.State> = emptyList(),
        val filters: Filters = Filters(),
        val tools: List<ToolCard.State> = emptyList(),
        val eventSink: (ToolsPresenter.UiEvent) -> Unit = {},
    ) : CircuitUiState {
        data class Filters(
            val categoryFilter: FilterMenu.UiState<String?> = FilterMenu.UiState(),
            val languageFilter: FilterMenu.UiState<Language?> = FilterMenu.UiState(),
        ) : CircuitUiState
    }

    sealed interface UiEvent : CircuitUiEvent {
        data class OpenToolDetails(val tool: String, val source: String? = null) : UiEvent
    }
    // endregion UiState / UiEvent

    @Composable
    override fun present(): UiState {
        val filters = rememberFilters()
        val selectedLocale by rememberUpdatedState(filters.languageFilter.selectedItem?.code)

        val eventSink: (UiEvent) -> Unit = remember {
            {
                when (it) {
                    is UiEvent.OpenToolDetails -> {
                        if (it.source != null) {
                            eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL_DETAILS, it.tool, it.source))
                        }
                        navigator.goTo(ToolDetailsScreen(it.tool, selectedLocale))
                    }
                }
            }
        }

        val spotlightTools = rememberSpotlightTools(
            secondLanguage = filters.languageFilter.selectedItem,
            eventSink = eventSink
        )
        val tools = rememberTools(
            category = filters.categoryFilter.selectedItem,
            language = filters.languageFilter.selectedItem,
            eventSink = eventSink,
        )

        return UiState(
            banner = rememberBanner(),
            dataLoaded = spotlightTools != null && tools != null,
            spotlightTools = spotlightTools.orEmpty(),
            filters = filters,
            tools = tools.orEmpty(),
            eventSink = eventSink,
        )
    }

    @Composable
    private fun rememberBanner() = remember {
        settings.isFeatureDiscoveredFlow(Settings.FEATURE_TOOL_FAVORITE)
            .map { if (!it) BannerType.TOOL_LIST_FAVORITES else null }
    }.collectAsState(null).value

    @Composable
    private fun rememberFilters(): UiState.Filters {
        val scope = rememberCoroutineScope()

        val selectedCategory by remember { settings.getDashboardFilterCategoryFlow() }.collectAsState(null)
        val selectedLocale by remember { settings.getDashboardFilterLocaleFlow() }.collectAsState(null)

        val languageMenuExpanded = rememberSaveable { mutableStateOf(false) }
        val languageQuery = rememberSaveable { mutableStateOf("") }
        LaunchedEffect(languageMenuExpanded.value) {
            if (!languageMenuExpanded.value) languageQuery.value = ""
        }

        return UiState.Filters(
            categoryFilter = FilterMenu.UiState(
                menuExpanded = rememberSaveable { mutableStateOf(false) },
                items = rememberFilterCategories(selectedLocale),
                query = remember { mutableStateOf("") },
                selectedItem = selectedCategory,
                eventSink = {
                    when (it) {
                        is FilterMenu.Event.SelectItem -> scope.launch {
                            settings.updateDashboardFilterCategory(it.item)
                        }
                    }
                }
            ),
            languageFilter = FilterMenu.UiState(
                menuExpanded = languageMenuExpanded,
                items = rememberFilterLanguages(selectedCategory, languageQuery.value),
                selectedItem = languagesRepository.rememberLanguage(selectedLocale),
                query = languageQuery,
                eventSink = {
                    when (it) {
                        is FilterMenu.Event.SelectItem -> scope.launch {
                            settings.updateDashboardFilterLocale(it.item?.code)
                        }
                    }
                }
            ),
        )
    }

    @Composable
    private fun rememberFilterCategories(selectedLanguage: Locale?): ImmutableList<FilterMenu.UiState.Item<String?>> {
        return remember(selectedLanguage) {
            filteredToolsFlowProducer.getFlow(language = selectedLanguage).map {
                it.groupBy { it.category }
                    .map { (category, tools) -> FilterMenu.UiState.Item(category, tools.size) }
                    .toImmutableList()
            }
        }.collectAsState(persistentListOf()).value
    }

    @Composable
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun rememberFilterLanguages(
        category: String?,
        query: String,
    ): ImmutableList<FilterMenu.UiState.Item<Language?>> {
        val scope = rememberCoroutineScope()

        val categoryFlow = remember { MutableStateFlow(category) }.apply { value = category }
        val queryFlow = remember { MutableStateFlow(query) }.apply { value = query }

        val languagesFlow = remember {
            categoryFlow
                .flatMapLatest {
                    when (it) {
                        null -> languagesRepository.getLanguagesFlow()
                        else -> languagesRepository.getLanguagesFlowForToolCategory(it)
                    }
                }
                .combine(settings.appLanguageFlow) { languages, appLang ->
                    languages.sortedWith(Language.displayNameComparator(context, appLang))
                }
                .flowOn(ioDispatcher)
                .shareIn(scope, started = SharingStarted.WhileSubscribed(5_000), replay = 1)
        }

        return remember(category) {
            val toolCountsFlow = filteredToolsFlowProducer.getFlow(category = category)
                .map { it.mapNotNullTo(mutableSetOf()) { it.code } }
                .distinctUntilChanged()
                .flatMapLatest { translationsRepository.getTranslationsFlowForTools(it) }
                .map { translations ->
                    translations
                        .groupBy { it.languageCode }
                        .mapValues { it.value.distinctBy { it.toolCode }.count() }
                }

            combine(
                languagesFlow,
                settings.appLanguageFlow,
                queryFlow,
            ) { languages, appLang, query -> languages.filterByDisplayAndNativeName(query, context, appLang) }
                .flowOn(ioDispatcher)
                .combine(toolCountsFlow) { languages, toolCounts ->
                    languages
                        .let { listOf(null) + it }
                        .map { FilterMenu.UiState.Item(it, toolCounts[it?.code] ?: 0) }
                        .toImmutableList()
                }
        }.collectAsState(persistentListOf()).value
    }

    @Composable
    private fun rememberSpotlightTools(
        secondLanguage: Language?,
        eventSink: (UiEvent) -> Unit,
    ): List<ToolCard.State>? {
        val tools by remember {
            toolsRepository.getNormalToolsFlow()
                .map { it.filter { !it.isHidden && it.isSpotlight }.sortedWith(Tool.COMPARATOR_DEFAULT_ORDER) }
        }.collectAsState(null)
        val eventSink by rememberUpdatedState(eventSink)

        return tools?.map { tool ->
            val toolCode by rememberUpdatedState(tool.code)

            toolCardPresenter.present(
                tool = tool,
                secondLanguage = secondLanguage,
                eventSink = {
                    when (it) {
                        ToolCard.Event.Click,
                        ToolCard.Event.OpenTool,
                        ToolCard.Event.OpenToolDetails ->
                            toolCode?.let { eventSink(UiEvent.OpenToolDetails(it, SOURCE_SPOTLIGHT)) }

                        ToolCard.Event.PinTool,
                        ToolCard.Event.UnpinTool -> error("$it should be handled by the ToolCardPresenter")
                    }
                }
            )
        }
    }

    @Composable
    private fun rememberTools(
        category: String?,
        language: Language?,
        eventSink: (UiEvent) -> Unit,
    ): List<ToolCard.State>? {
        val locale = language?.code
        val tools by remember(category, locale) { filteredToolsFlowProducer.getFlow(category, locale) }
            .collectAsState(null)
        val eventSink by rememberUpdatedState(eventSink)

        return tools?.map { tool ->
            key(tool.code) {
                val toolCode by rememberUpdatedState(tool.code)
                toolCardPresenter.present(
                    tool = tool,
                    secondLanguage = language,
                    eventSink = {
                        when (it) {
                            ToolCard.Event.Click,
                            ToolCard.Event.OpenTool,
                            ToolCard.Event.OpenToolDetails ->
                                toolCode?.let { eventSink(UiEvent.OpenToolDetails(it, SOURCE_ALL_TOOLS)) }

                            ToolCard.Event.PinTool,
                            ToolCard.Event.UnpinTool -> error("$it should be handled by the ToolCardPresenter")
                        }
                    }
                )
            }
        }
    }

    @AssistedFactory
    @CircuitInject(ToolsScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): ToolsPresenter
    }
}

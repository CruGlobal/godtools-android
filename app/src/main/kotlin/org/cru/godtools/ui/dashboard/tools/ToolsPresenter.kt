package org.cru.godtools.ui.dashboard.tools

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import com.slack.circuit.codegen.annotations.CircuitInject
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL_DETAILS
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
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.greenrobot.eventbus.EventBus

class ToolsPresenter @AssistedInject constructor(
    @ApplicationContext
    private val context: Context,
    private val eventBus: EventBus,
    private val settings: Settings,
    private val toolCardPresenter: ToolCardPresenter,
    private val languagesRepository: LanguagesRepository,
    private val toolsRepository: ToolsRepository,
    private val translationsRepository: TranslationsRepository,
    @Assisted private val navigator: Navigator,
) : Presenter<ToolsScreen.State> {
    @Composable
    override fun present(): ToolsScreen.State {
        val filters = rememberFilters()
        val selectedLocale by rememberUpdatedState(filters.languageFilter.selectedItem?.code)

        val eventSink: (ToolsScreen.Event) -> Unit = remember {
            {
                when (it) {
                    is ToolsScreen.Event.OpenToolDetails -> {
                        if (it.source != null) {
                            eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL_DETAILS, it.tool, it.source))
                        }
                        navigator.goTo(ToolDetailsScreen(it.tool, selectedLocale))
                    }
                }
            }
        }

        return ToolsScreen.State(
            banner = rememberBanner(),
            spotlightTools = rememberSpotlightTools(
                secondLanguage = filters.languageFilter.selectedItem,
                eventSink = eventSink
            ),
            filters = filters,
            tools = rememberFilteredToolsFlow(filters.categoryFilter.selectedItem, selectedLocale)
                .collectAsState(emptyList()).value,
            eventSink = eventSink,
        )
    }

    @Composable
    private fun rememberBanner() = remember {
        settings.isFeatureDiscoveredFlow(Settings.FEATURE_TOOL_FAVORITE)
            .map { if (!it) BannerType.TOOL_LIST_FAVORITES else null }
    }.collectAsState(null).value

    @Composable
    private fun rememberFilters(): ToolsScreen.Filters {
        val scope = rememberCoroutineScope()

        val selectedCategory by remember { settings.getDashboardFilterCategoryFlow() }.collectAsState(null)
        val selectedLocale by remember { settings.getDashboardFilterLocaleFlow() }.collectAsState(null)

        val languageMenuExpanded = rememberSaveable { mutableStateOf(false) }
        val languageQuery = rememberSaveable { mutableStateOf("") }
        LaunchedEffect(languageMenuExpanded.value) {
            if (!languageMenuExpanded.value) languageQuery.value = ""
        }

        return ToolsScreen.Filters(
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
    private fun rememberFilterCategories(selectedLanguage: Locale?): ImmutableList<FilterMenu.UiState.Item<String>> {
        val filteredToolsFlow = rememberFilteredToolsFlow(language = selectedLanguage)

        return remember(filteredToolsFlow) {
            filteredToolsFlow.map {
                it.groupBy { it.category }
                    .mapNotNull { (category, tools) -> category?.let { FilterMenu.UiState.Item(category, tools.size) } }
                    .toImmutableList()
            }
        }.collectAsState(persistentListOf()).value
    }

    @Composable
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun rememberFilterLanguages(
        category: String?,
        query: String,
    ): ImmutableList<FilterMenu.UiState.Item<Language>> {
        val categoryFlow = remember { MutableStateFlow(category) }.apply { value = category }
        val queryFlow = remember { MutableStateFlow(query) }.apply { value = query }
        val toolsFlow = rememberFilteredToolsFlow(category = category)

        return remember {
            val languagesFlow = categoryFlow
                .flatMapLatest {
                    when (it) {
                        null -> languagesRepository.getLanguagesFlow()
                        else -> languagesRepository.getLanguagesFlowForToolCategory(it)
                    }
                }
                .combine(settings.appLanguageFlow) { languages, appLang ->
                    languages.sortedWith(Language.displayNameComparator(context, appLang))
                }

            val toolCountsFlow = toolsFlow
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
                toolCountsFlow,
            ) { languages, appLang, query, toolCounts ->
                languages
                    .filterByDisplayAndNativeName(query, context, appLang)
                    .map { FilterMenu.UiState.Item(it, toolCounts[it.code] ?: 0) }
                    .toImmutableList()
            }
        }.collectAsState(persistentListOf()).value
    }

    @Composable
    private fun rememberSpotlightTools(
        secondLanguage: Language?,
        eventSink: (ToolsScreen.Event) -> Unit,
    ): List<ToolCard.State> {
        val tools by remember {
            toolsRepository.getNormalToolsFlow()
                .map { it.filter { !it.isHidden && it.isSpotlight }.sortedWith(Tool.COMPARATOR_DEFAULT_ORDER) }
        }.collectAsState(emptyList())
        val eventSink by rememberUpdatedState(eventSink)

        return tools.map { tool ->
            val toolCode by rememberUpdatedState(tool.code)
            val toolEventSink: (ToolCard.Event) -> Unit = remember {
                {
                    when (it) {
                        ToolCard.Event.Click,
                        ToolCard.Event.OpenTool,
                        ToolCard.Event.OpenToolDetails ->
                            toolCode?.let { eventSink(ToolsScreen.Event.OpenToolDetails(it, SOURCE_SPOTLIGHT)) }
                        ToolCard.Event.PinTool,
                        ToolCard.Event.UnpinTool -> error("$it should be handled by the ToolCardPresenter")
                    }
                }
            }

            toolCardPresenter.present(
                tool = tool,
                secondLanguage = secondLanguage,
                eventSink = toolEventSink,
            )
        }
    }

    @Composable
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun rememberFilteredToolsFlow(category: String? = null, language: Locale? = null): Flow<List<Tool>> {
        val categoryFlow = remember { MutableStateFlow(category) }.apply { value = category }
        val languageFlow = remember { MutableStateFlow(language) }.apply { value = language }

        return remember {
            val defaultVariantsFlow = toolsRepository.getMetaToolsFlow()
                .map { it.associateBy({ it.code }, { it.defaultVariantCode }) }

            languageFlow
                .flatMapLatest {
                    when (it) {
                        null -> toolsRepository.getNormalToolsFlow()
                        else -> toolsRepository.getNormalToolsFlowByLanguage(it)
                    }
                }
                .map { it.filterNot { it.isHidden }.sortedBy { it.defaultOrder } }
                .combine(defaultVariantsFlow) { tools, defaultVariants ->
                    tools.filter { it.metatoolCode == null || it.code == defaultVariants[it.metatoolCode] }
                }
                .combine(categoryFlow) { tools, category ->
                    if (category == null) tools else tools.filter { it.category == category }
                }
        }
    }

    @AssistedFactory
    @CircuitInject(ToolsScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): ToolsPresenter
    }
}

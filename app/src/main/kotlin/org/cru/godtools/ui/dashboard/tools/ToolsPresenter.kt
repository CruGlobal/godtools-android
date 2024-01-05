package org.cru.godtools.ui.dashboard.tools

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL_DETAILS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_SPOTLIGHT
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Language.Companion.filterByDisplayAndNativeName
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.banner.BannerType
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
    @Assisted private val navigator: Navigator,
) : Presenter<ToolsScreen.State> {
    @Composable
    override fun present(): ToolsScreen.State {
        // selected category
        var selectedCategory: String? by remember { mutableStateOf(null) }

        // selected language
        var selectedLocale: Locale? by remember { mutableStateOf(null) }
        val selectedLanguage = rememberLanguage(selectedLocale)
        var languageQuery by remember { mutableStateOf("") }

        val eventSink: (ToolsScreen.Event) -> Unit = remember {
            {
                when (it) {
                    is ToolsScreen.Event.OpenToolDetails -> {
                        if (it.source != null) {
                            eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL_DETAILS, it.tool, it.source))
                        }
                        navigator.goTo(ToolDetailsScreen(it.tool, selectedLocale))
                    }
                    is ToolsScreen.Event.UpdateSelectedCategory -> selectedCategory = it.category
                    is ToolsScreen.Event.UpdateLanguageQuery -> languageQuery = it.query
                    is ToolsScreen.Event.UpdateSelectedLanguage -> selectedLocale = it.locale
                }
            }
        }

        val filters = ToolsScreen.Filters(
            categories = rememberFilterCategories(selectedLocale),
            selectedCategory = selectedCategory,
            languages = rememberFilterLanguages(selectedCategory, languageQuery),
            languageQuery = languageQuery,
            selectedLanguage = selectedLanguage,
        )

        return ToolsScreen.State(
            banner = rememberBanner(),
            spotlightTools = rememberSpotlightTools(secondLanguage = selectedLanguage, eventSink = eventSink),
            filters = filters,
            tools = rememberFilteredToolsFlow(filters.selectedCategory, filters.selectedLanguage?.code)
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
    private fun rememberFilterCategories(selectedLanguage: Locale?): List<String> {
        val filteredToolsFlow = rememberFilteredToolsFlow(language = selectedLanguage)

        return remember(filteredToolsFlow) {
            filteredToolsFlow.map { it.mapNotNull { it.category }.distinct() }
        }.collectAsState(emptyList()).value
    }

    @Composable
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun rememberFilterLanguages(category: String?, query: String): List<Language> {
        val categoryFlow = remember { MutableStateFlow(category) }.apply { value = category }
        val queryFlow = remember { MutableStateFlow(query) }.apply { value = query }

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

            combine(languagesFlow, settings.appLanguageFlow, queryFlow) { languages, appLang, query ->
                languages.filterByDisplayAndNativeName(query, context, appLang)
            }
        }.collectAsState(emptyList()).value
    }

    @Composable
    @VisibleForTesting
    internal fun rememberLanguage(locale: Locale?) = remember(locale) {
        locale?.let { languagesRepository.findLanguageFlow(it) } ?: flowOf(null)
    }.collectAsState(null).value

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

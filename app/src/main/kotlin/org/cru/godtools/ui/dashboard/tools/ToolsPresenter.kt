package org.cru.godtools.ui.dashboard.tools

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
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
        val viewModel: ToolsViewModel = viewModel()

        // selected category
        val selectedCategory by viewModel.selectedCategory.collectAsState()

        // selected language
        val selectedLocale by viewModel.selectedLocale.collectAsState()
        val selectedLanguage = rememberLanguage(selectedLocale)
        val languageQuery by viewModel.languageQuery.collectAsState()

        val eventSink: (ToolsScreen.Event) -> Unit = remember {
            {
                when (it) {
                    is ToolsScreen.Event.OpenToolDetails -> {
                        if (it.source != null) {
                            eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL_DETAILS, it.tool, it.source))
                        }
                        navigator.goTo(ToolDetailsScreen(it.tool, selectedLocale))
                    }
                    is ToolsScreen.Event.UpdateSelectedCategory -> viewModel.setSelectedCategory(it.category)
                    is ToolsScreen.Event.UpdateLanguageQuery -> viewModel.setLanguageQuery(it.query)
                    is ToolsScreen.Event.UpdateSelectedLanguage -> viewModel.setSelectedLocale(it.locale)
                }
            }
        }

        val filters = ToolsScreen.State.Filters(
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
    @VisibleForTesting
    internal fun rememberBanner() = remember {
        settings.isFeatureDiscoveredFlow(Settings.FEATURE_TOOL_FAVORITE)
            .map { if (!it) BannerType.TOOL_LIST_FAVORITES else null }
    }.collectAsState(null).value

    @Composable
    @VisibleForTesting
    internal fun rememberFilterCategories(selectedLanguage: Locale?): List<String> {
        val filteredToolsFlow = rememberFilteredToolsFlow(language = selectedLanguage)

        return remember(filteredToolsFlow) {
            filteredToolsFlow.map { it.mapNotNull { it.category }.distinct() }
        }.collectAsState(emptyList()).value
    }

    @Composable
    @VisibleForTesting
    internal fun rememberFilterLanguages(selectedCategory: String?, query: String): List<Language> {
        val appLanguage by settings.appLanguageFlow.collectAsState(settings.appLanguage)

        val rawLanguages by remember(context, selectedCategory) {
            combine(
                when (selectedCategory) {
                    null -> languagesRepository.getLanguagesFlow()
                    else -> languagesRepository.getLanguagesFlowForToolCategory(selectedCategory)
                },
                settings.appLanguageFlow,
            ) { langs, appLang -> langs.sortedWith(Language.displayNameComparator(context, appLang)) }
                .flowOn(Dispatchers.Default)
        }.collectAsState(emptyList())

        return remember(context, query) {
            derivedStateOf { rawLanguages.filterByDisplayAndNativeName(query, context, appLanguage) }
        }.value
    }

    @Composable
    @VisibleForTesting
    internal fun rememberLanguage(locale: Locale?) = remember(locale) {
        locale?.let { languagesRepository.findLanguageFlow(it) } ?: flowOf(null)
    }.collectAsState(null).value

    @Composable
    @VisibleForTesting
    internal fun rememberSpotlightTools(
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
    @VisibleForTesting
    internal fun rememberFilteredToolsFlow(category: String? = null, language: Locale? = null): Flow<List<Tool>> {
        val defaultVariantsFlow = remember {
            toolsRepository.getMetaToolsFlow()
                .map { it.associateBy({ it.code }, { it.defaultVariantCode }) }
        }

        return remember(category, language) {
            when (language) {
                null -> toolsRepository.getNormalToolsFlow()
                else -> toolsRepository.getNormalToolsFlowByLanguage(language)
            }.combine(defaultVariantsFlow) { tools, defaultVariants ->
                tools
                    .filter { it.metatoolCode == null || it.code == defaultVariants[it.metatoolCode] }
                    .filter { category == null || it.category == category }
                    .filterNot { it.isHidden }
                    .sortedBy { it.defaultOrder }
            }
        }
    }

    @AssistedFactory
    @CircuitInject(ToolsScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): ToolsPresenter
    }
}

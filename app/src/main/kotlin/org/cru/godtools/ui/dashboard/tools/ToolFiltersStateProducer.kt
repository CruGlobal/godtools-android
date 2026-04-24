package org.cru.godtools.ui.dashboard.tools

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.slack.circuit.runtime.CircuitUiState
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.compose.util.rememberStateFlow
import org.ccci.gto.android.common.dagger.coroutines.DispatcherType
import org.ccci.gto.android.common.dagger.coroutines.DispatcherType.Type.IO
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.repository.rememberLanguage
import org.cru.godtools.model.Language
import org.cru.godtools.model.Language.Companion.filterByDisplayAndNativeName
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.cru.godtools.ui.dashboard.tools.ToolFiltersStateProducer.Filters

interface ToolFiltersStateProducer {
    data class Filters(
        val categoryFilter: FilterMenu.UiState<String?> = FilterMenu.UiState(),
        val languageFilter: FilterMenu.UiState<Language?> = FilterMenu.UiState(),
    ) : CircuitUiState

    @Composable
    fun produce(): Filters
}

internal class DefaultToolFiltersStateProducer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val filteredToolsFlowProducer: FilteredToolsFlowProducer,
    private val languagesRepository: LanguagesRepository,
    private val settings: Settings,
    private val translationsRepository: TranslationsRepository,
    @param:DispatcherType(IO) private val ioDispatcher: CoroutineDispatcher,
) : ToolFiltersStateProducer {
    @Composable
    override fun produce(): Filters {
        val scope = rememberCoroutineScope()

        val selectedCategory by remember { settings.getDashboardFilterCategoryFlow() }.collectAsState(null)
        val selectedLocale by remember { settings.getDashboardFilterLocaleFlow() }.collectAsState(null)

        val languageMenuExpanded = rememberSaveable { mutableStateOf(false) }
        val languageQuery = rememberSaveable { mutableStateOf("") }
        LaunchedEffect(languageMenuExpanded.value) {
            if (!languageMenuExpanded.value) languageQuery.value = ""
        }

        return Filters(
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
    private fun rememberFilterCategories(selectedLanguage: Locale?) = remember(selectedLanguage) {
        filteredToolsFlowProducer.getFlow(language = selectedLanguage).map {
            it.groupBy { it.category }
                .map { (category, tools) -> FilterMenu.UiState.Item(category, tools.size) }
        }
    }.collectAsState(emptyList()).value

    @Composable
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun rememberFilterLanguages(category: String?, query: String): List<FilterMenu.UiState.Item<Language?>> {
        val scope = rememberCoroutineScope()

        val categoryFlow = rememberStateFlow(category)
        val queryFlow = rememberStateFlow(query)

        val languagesFlow = remember(categoryFlow, queryFlow) {
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
                }
        }.collectAsState(emptyList()).value
    }
}

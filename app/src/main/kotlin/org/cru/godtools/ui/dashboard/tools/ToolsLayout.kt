package org.cru.godtools.ui.dashboard.tools

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_ALL_TOOLS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_SPOTLIGHT
import org.cru.godtools.ui.banner.Banners
import org.cru.godtools.ui.tools.SquareToolCard
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardEvent
import org.cru.godtools.ui.tools.ToolViewModels

internal val MARGIN_TOOLS_LAYOUT_HORIZONTAL = 16.dp

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun ToolsLayout(onEvent: (ToolCardEvent) -> Unit) {
    val viewModel: ToolsViewModel = viewModel()
    val toolViewModels: ToolViewModels = viewModel()

    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    val filters = ToolsScreen.State.Filters(
        categories = viewModel.categories.collectAsState().value,
        selectedCategory = viewModel.selectedCategory.collectAsState().value,
        languages = viewModel.languages.collectAsState().value,
        languageQuery = viewModel.languageQuery.collectAsState().value,
        selectedLanguage = viewModel.selectedLanguage.collectAsState().value,
    )
    val eventSink: (ToolsScreen.Event) -> Unit = remember(onEvent) {
        {
            when (it) {
                is ToolsScreen.Event.OpenToolDetails -> {
                    if (it.source != null) viewModel.recordOpenToolDetailsInAnalytics(it.tool, it.source)
                    onEvent(ToolCardEvent.OpenToolDetails(it.tool, additionalLocale = selectedLanguage?.code))
                }
                is ToolsScreen.Event.UpdateSelectedCategory -> viewModel.setSelectedCategory(it.category)
                is ToolsScreen.Event.UpdateLanguageQuery -> viewModel.setLanguageQuery(it.query)
                is ToolsScreen.Event.UpdateSelectedLanguage -> viewModel.setSelectedLocale(it.locale)
            }
        }
    }

    val state = ToolsScreen.State(
        banner = viewModel.banner.collectAsState().value,
        spotlightTools = viewModel.spotlightTools.collectAsState().value,
        filters = filters,
        tools = viewModel.tools.collectAsState().value,
        eventSink = eventSink,
    )

    val banner by rememberUpdatedState(state.banner)
    val spotlightTools by rememberUpdatedState(state.spotlightTools)
    val tools by rememberUpdatedState(state.tools)

    val columnState = rememberLazyListState()
    LaunchedEffect(banner) { if (banner != null) columnState.animateScrollToItem(0) }

    LazyColumn(state = columnState) {
        item("banners", "banners") {
            Banners(
                { banner },
                modifier = Modifier
                    .animateItemPlacement()
                    .fillMaxWidth()
            )
        }

        if (spotlightTools.isNotEmpty()) {
            item("tool-spotlight", "tool-spotlight") {
                ToolSpotlight(
                    state,
                    toolViewModels,
                    modifier = Modifier
                        .animateItemPlacement()
                        .padding(top = 16.dp)
                )

                Divider(
                    modifier = Modifier
                        .animateItemPlacement()
                        .padding(horizontal = MARGIN_TOOLS_LAYOUT_HORIZONTAL, top = 16.dp)
                )
            }
        }

        item("tool-filters", "tool-filters") {
            ToolFilters(
                filters = filters,
                eventSink = eventSink,
                modifier = Modifier
                    .animateItemPlacement()
                    .padding(vertical = 16.dp)
            )
        }

        items(tools, { "tool:${it.code.orEmpty()}" }, { "tool" }) { tool ->
            val toolViewModel = toolViewModels[tool.code.orEmpty(), tool]
            val toolState = toolViewModel.toState(secondLanguage = selectedLanguage) {
                when (it) {
                    ToolCard.Event.Click, ToolCard.Event.OpenTool, ToolCard.Event.OpenToolDetails ->
                        tool.code?.let { eventSink(ToolsScreen.Event.OpenToolDetails(it, SOURCE_ALL_TOOLS)) }
                    ToolCard.Event.PinTool -> toolViewModel.pinTool()
                    ToolCard.Event.UnpinTool -> toolViewModel.unpinTool()
                }
            }

            ToolCard(
                state = toolState,
                showActions = false,
                modifier = Modifier
                    .animateItemPlacement()
                    .padding(bottom = 16.dp, horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun ToolSpotlight(state: ToolsScreen.State, toolViewModels: ToolViewModels, modifier: Modifier = Modifier) {
    val spotlightTools by rememberUpdatedState(state.spotlightTools)
    val selectedLanguage by rememberUpdatedState(state.filters.selectedLanguage)
    val eventSink by rememberUpdatedState(state.eventSink)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.dashboard_tools_section_spotlight_label),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )
        Text(
            stringResource(R.string.dashboard_tools_section_spotlight_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(top = 4.dp, horizontal = 16.dp)
                .fillMaxWidth()
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(spotlightTools, key = { it.code.orEmpty() }) { tool ->
                val toolViewModel = toolViewModels[tool.code.orEmpty()]
                val toolState = toolViewModel.toState(secondLanguage = selectedLanguage) {
                    when (it) {
                        ToolCard.Event.Click, ToolCard.Event.OpenTool, ToolCard.Event.OpenToolDetails ->
                            tool.code?.let { eventSink(ToolsScreen.Event.OpenToolDetails(it, SOURCE_SPOTLIGHT)) }
                        ToolCard.Event.PinTool -> toolViewModel.pinTool()
                        ToolCard.Event.UnpinTool -> toolViewModel.unpinTool()
                    }
                }

                SquareToolCard(
                    state = toolState,
                    showCategory = false,
                    showActions = false,
                    floatParallelLanguageUp = false,
                    confirmRemovalFromFavorites = false,
                )
            }
        }
    }
}

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_ALL_TOOLS
import org.cru.godtools.ui.banner.Banners
import org.cru.godtools.ui.tools.SquareToolCard
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolViewModels

internal val MARGIN_TOOLS_LAYOUT_HORIZONTAL = 16.dp

@Composable
@OptIn(ExperimentalFoundationApi::class)
@CircuitInject(ToolsScreen::class, SingletonComponent::class)
internal fun ToolsLayout(state: ToolsScreen.State, modifier: Modifier = Modifier) {
    val toolViewModels: ToolViewModels = viewModel()

    val banner by rememberUpdatedState(state.banner)
    val spotlightTools by rememberUpdatedState(state.spotlightTools)
    val filters by rememberUpdatedState(state.filters)
    val tools by rememberUpdatedState(state.tools)
    val selectedLanguage by rememberUpdatedState(state.filters.selectedLanguage)
    val eventSink by rememberUpdatedState(state.eventSink)

    val columnState = rememberLazyListState()
    LaunchedEffect(banner) { if (banner != null) columnState.animateScrollToItem(0) }

    LazyColumn(state = columnState, modifier = modifier) {
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
                    spotlightTools,
                    modifier = Modifier
                        .animateItemPlacement()
                        .padding(top = 16.dp)
                )

                HorizontalDivider(
                    modifier = Modifier
                        .animateItemPlacement()
                        .padding(horizontal = MARGIN_TOOLS_LAYOUT_HORIZONTAL, top = 16.dp)
                )
            }
        }

        item("tool-filters", "tool-filters") {
            ToolFilters(
                filters = filters,
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
private fun ToolSpotlight(tools: List<ToolCard.State>, modifier: Modifier = Modifier) {
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
            items(tools, key = { it.tool?.code.orEmpty() }) { tool ->
                SquareToolCard(
                    state = tool,
                    showCategory = false,
                    showSecondLanguage = true,
                    showActions = false,
                    floatParallelLanguageUp = false,
                    confirmRemovalFromFavorites = false,
                )
            }
        }
    }
}

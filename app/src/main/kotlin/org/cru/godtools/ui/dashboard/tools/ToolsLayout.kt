package org.cru.godtools.ui.dashboard.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import org.ccci.gto.android.common.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.ToolsScreen
import org.cru.godtools.ui.banner.Banners
import org.cru.godtools.ui.dashboard.LocalizationSettingsBox
import org.cru.godtools.ui.dashboard.rememberPinLastItemBottomArrangement
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiEvent
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState
import org.cru.godtools.ui.tools.SquareToolCard
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardPresenter

internal val MARGIN_TOOLS_LAYOUT_HORIZONTAL = 16.dp

@Composable
@CircuitInject(ToolsScreen::class, SingletonComponent::class)
internal fun ToolsLayout(state: UiState, modifier: Modifier = Modifier) {
    val filters by rememberUpdatedState(state.filters)
    val tools by rememberUpdatedState(state.tools)

    val columnState = rememberLazyListState()
    LaunchedEffect(state.banner?.type) { if (state.banner != null) columnState.animateScrollToItem(0) }
    val pinLastItem = rememberPinLastItemBottomArrangement()
    val verticalArrangement = if (state.mode == UiState.Mode.PERSONALIZATION) {
        pinLastItem
    } else {
        Arrangement.Top
    }

    LazyColumn(
        state = columnState,
        verticalArrangement = verticalArrangement,
        modifier = modifier.fillMaxHeight()
    ) {
        if (!state.dataLoaded) return@LazyColumn

        item("banners", "banners") {
            Banners(
                state.banner,
                modifier = Modifier
                    .animateItem()
                    .fillMaxWidth()
            )
        }

        if (state.isPersonalizationEnabled) {
            item("mode-toggle", "mode-toggle") {
                PersonalizationToggle(
                    state,
                    modifier = Modifier
                        .padding(horizontal = MARGIN_TOOLS_LAYOUT_HORIZONTAL)
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }

        item("header", "header") {
            ToolsHeader(
                state.mode,
                modifier = Modifier
                    .padding(horizontal = MARGIN_TOOLS_LAYOUT_HORIZONTAL, top = 16.dp)
                    .fillMaxWidth()
            )
        }

        if (state.spotlightTools.isNotEmpty()) {
            item("featured-tools", "featured-tools") {
                HorizontalDivider(
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = MARGIN_TOOLS_LAYOUT_HORIZONTAL, top = 16.dp)
                )

                ToolSpotlight(
                    state.spotlightTools,
                    modifier = Modifier
                        .animateItem()
                        .padding(top = 16.dp)
                )
            }
        }

        item("tool-filters", "tool-filters") {
            HorizontalDivider(
                modifier = Modifier
                    .animateItem()
                    .padding(horizontal = MARGIN_TOOLS_LAYOUT_HORIZONTAL, top = 16.dp)
            )

            ToolFilters(
                filters = filters,
                modifier = Modifier
                    .animateItem()
                    .padding(vertical = 16.dp)
            )
        }

        items(tools, { "tool:${it.toolCode.orEmpty()}" }, { "tool" }) { toolState ->
            ToolCard(
                state = toolState,
                showActions = false,
                modifier = Modifier
                    .animateItem()
                    .padding(bottom = 16.dp, horizontal = MARGIN_TOOLS_LAYOUT_HORIZONTAL)
            )
        }

        if (state.mode == UiState.Mode.PERSONALIZATION) {
            item("localization-settings-box", "localization-settings-box") {
                LocalizationSettingsBox(
                    title = R.string.dashboard_tools_section_personalized_localization_title,
                    description = R.string.dashboard_tools_section_personalized_localization_text,
                    onClickSettings = { state.eventSink(UiEvent.OpenLocalizationSettings) }
                )
            }
        }
    }
}

@Composable
private fun PersonalizationToggle(state: UiState, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        SegmentedButton(
            selected = state.mode == UiState.Mode.PERSONALIZATION,
            onClick = { state.eventSink(UiEvent.ChangeMode(UiState.Mode.PERSONALIZATION)) },
            shape = SegmentedButtonDefaults.itemShape(0, 2),
        ) {
            Text(stringResource(R.string.dashboard_tools_toggle_personalized))
        }

        SegmentedButton(
            selected = state.mode == UiState.Mode.ALL_TOOLS,
            onClick = { state.eventSink(UiEvent.ChangeMode(UiState.Mode.ALL_TOOLS)) },
            shape = SegmentedButtonDefaults.itemShape(1, 2),
        ) {
            Text(stringResource(R.string.dashboard_tools_toggle_all))
        }
    }
}

@Composable
private fun ToolsHeader(mode: UiState.Mode, modifier: Modifier = Modifier) = Column(modifier) {
    Text(
        stringResource(
            when (mode) {
                UiState.Mode.PERSONALIZATION -> R.string.dashboard_tools_header_title_personalized
                UiState.Mode.ALL_TOOLS -> R.string.dashboard_tools_header_title_all
            }
        ),
        style = MaterialTheme.typography.headlineMedium,
    )
    Text(
        stringResource(R.string.dashboard_tools_header_description),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun ToolSpotlight(tools: List<ToolCardPresenter.UiState>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.dashboard_tools_section_spotlight_label),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(horizontal = MARGIN_TOOLS_LAYOUT_HORIZONTAL)
                .fillMaxWidth()
        )
        Text(
            stringResource(R.string.dashboard_tools_section_spotlight_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(top = 4.dp, horizontal = MARGIN_TOOLS_LAYOUT_HORIZONTAL)
                .fillMaxWidth()
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = MARGIN_TOOLS_LAYOUT_HORIZONTAL),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(tools, key = { it.toolCode.orEmpty() }) { tool ->
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

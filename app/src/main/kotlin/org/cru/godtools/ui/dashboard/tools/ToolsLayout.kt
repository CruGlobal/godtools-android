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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_ALL_TOOLS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_SPOTLIGHT
import org.cru.godtools.ui.banner.Banners
import org.cru.godtools.ui.tools.PreloadTool
import org.cru.godtools.ui.tools.SquareToolCard
import org.cru.godtools.ui.tools.ToolCard

internal val MARGIN_TOOLS_LAYOUT_HORIZONTAL = 16.dp

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun ToolsLayout(
    viewModel: ToolsViewModel = viewModel(),
    onToolClicked: (String) -> Unit = {}
) {
    val banner by viewModel.banner.collectAsState()
    val spotlightTools by viewModel.spotlightTools.collectAsState()
    val tools by viewModel.tools.collectAsState()

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
                    viewModel,
                    onToolClicked = onToolClicked,
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
                viewModel = viewModel,
                modifier = Modifier
                    .animateItemPlacement()
                    .padding(vertical = 16.dp)
            )
        }

        items(tools, { "tool:${it.id}" }, { "tool" }) { tool ->
            PreloadTool(tool)
            ToolCard(
                tool.code.orEmpty(),
                showActions = false,
                onClick = { it, _, _ ->
                    viewModel.recordOpenToolDetailsInAnalytics(it?.code, SOURCE_ALL_TOOLS)
                    it?.code?.let(onToolClicked)
                },
                modifier = Modifier
                    .animateItemPlacement()
                    .padding(bottom = 16.dp, horizontal = 16.dp)
            )
        }
    }
}

@Composable
internal fun ToolSpotlight(
    viewModel: ToolsViewModel,
    modifier: Modifier = Modifier,
    onToolClicked: (String) -> Unit = {}
) = Column(modifier = modifier.fillMaxWidth()) {
    val spotlightTools by viewModel.spotlightTools.collectAsState()

    Text(
        stringResource(R.string.dashboard_tools_section_spotlight_label),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        items(spotlightTools, key = { it.code.orEmpty() }) {
            PreloadTool(it)

            SquareToolCard(
                toolCode = it.code.orEmpty(),
                showCategory = false,
                showActions = false,
                floatParallelLanguageUp = false,
                confirmRemovalFromFavorites = false,
                onClick = { tool, _, _ ->
                    viewModel.recordOpenToolDetailsInAnalytics(tool?.code, SOURCE_SPOTLIGHT)
                    tool?.code?.let(onToolClicked)
                }
            )
        }
    }
}

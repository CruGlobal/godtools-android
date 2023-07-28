package org.cru.godtools.ui.dashboard.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import org.cru.godtools.R
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL_DETAILS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_FAVORITE
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.tools.PreloadTool
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardEvent

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun AllFavoritesList(
    onEvent: (ToolCardEvent) -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val favoriteTools by viewModel.reorderableFavoriteTools.collectAsState()

    val reorderableState = rememberReorderableLazyListState(
        // only support reordering tool items
        canDragOver = { (_, k), _ -> (k as? String)?.startsWith("tool:") == true },
        onMove = { from, to ->
            val fromPos = favoriteTools.indexOfFirst { it.code == (from.key as? String)?.removePrefix("tool:") }
            val toPos = favoriteTools.indexOfFirst { it.code == (to.key as? String)?.removePrefix("tool:") }
            if (fromPos != -1 && toPos != -1) viewModel.moveFavoriteTool(fromPos, toPos)
        },
        onDragEnd = { _, _ -> viewModel.commitFavoriteToolOrder() }
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        state = reorderableState.listState,
        modifier = Modifier
            .reorderable(reorderableState)
            .detectReorderAfterLongPress(reorderableState)
    ) {
        item("header", "header") {
            Text(
                stringResource(R.string.dashboard_home_section_favorites_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement()
            )
        }

        items(favoriteTools, key = { "tool:${it.code}" }) { tool ->
            ReorderableItem(reorderableState, "tool:${tool.code}") { isDragging ->
                val interactionSource = remember { MutableInteractionSource() }
                interactionSource.reorderableDragInteractions(isDragging)

                PreloadTool(tool)
                ToolCard(
                    toolCode = tool.code.orEmpty(),
                    confirmRemovalFromFavorites = true,
                    interactionSource = interactionSource,
                    onEvent = {
                        when (it) {
                            is ToolCardEvent.Click,
                            is ToolCardEvent.OpenTool -> viewModel.recordOpenClickInAnalytics(
                                ACTION_OPEN_TOOL,
                                it.tool?.code,
                                SOURCE_FAVORITE
                            )
                            is ToolCardEvent.OpenToolDetails -> viewModel.recordOpenClickInAnalytics(
                                ACTION_OPEN_TOOL_DETAILS,
                                it.tool?.code,
                                SOURCE_FAVORITE
                            )
                        }
                        onEvent(it)
                    },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MutableInteractionSource.reorderableDragInteractions(isDragging: Boolean) {
    val dragState = remember { object { var start: DragInteraction.Start? = null } }
    LaunchedEffect(isDragging) {
        when (val start = dragState.start) {
            null -> if (isDragging) dragState.start = DragInteraction.Start().also { emit(it) }
            else -> if (!isDragging) {
                dragState.start = null
                emit(DragInteraction.Stop(start))
            }
        }
    }
}

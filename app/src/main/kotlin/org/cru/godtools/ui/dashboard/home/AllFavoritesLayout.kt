package org.cru.godtools.ui.dashboard.home

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import org.cru.godtools.R
import org.cru.godtools.ui.dashboard.home.AllFavoritesScreen.UiEvent
import org.cru.godtools.ui.dashboard.home.AllFavoritesScreen.UiState
import org.cru.godtools.ui.tools.ToolCard

@Composable
@CircuitInject(AllFavoritesScreen::class, SingletonComponent::class)
internal fun AllFavoritesLayout(state: UiState, modifier: Modifier = Modifier) {
    val tools by rememberUpdatedState(state.tools)
    val eventSink by rememberUpdatedState(state.eventSink)

    val reorderableState = rememberReorderableLazyListState(
        // only support reordering tool items
        canDragOver = { (_, k), _ -> (k as? String)?.startsWith("tool:") == true },
        onMove = { from, to ->
            val fromPos = tools.indexOfFirst { it.toolCode == (from.key as? String)?.removePrefix("tool:") }
            val toPos = tools.indexOfFirst { it.toolCode == (to.key as? String)?.removePrefix("tool:") }
            if (fromPos != -1 && toPos != -1) eventSink(UiEvent.MoveTool(fromPos, toPos))
        },
        onDragEnd = { _, _ -> eventSink(UiEvent.CommitToolOrder) }
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        state = reorderableState.listState,
        modifier = modifier
            .reorderable(reorderableState)
            .detectReorderAfterLongPress(reorderableState)
    ) {
        item("header", "header") {
            Text(
                stringResource(R.string.dashboard_home_section_favorites_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem()
            )
        }

        items(tools, key = { "tool:${it.toolCode}" }) { toolState ->
            ReorderableItem(
                state = reorderableState,
                key = "tool:${toolState.toolCode}",
                defaultDraggingModifier = Modifier.animateItem()
            ) { isDragging ->
                val interactionSource = remember { MutableInteractionSource() }
                interactionSource.reorderableDragInteractions(isDragging)

                ToolCard(
                    toolState,
                    confirmRemovalFromFavorites = true,
                    interactionSource = interactionSource,
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
    val dragState = remember {
        object {
            var start: DragInteraction.Start? = null
        }
    }
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

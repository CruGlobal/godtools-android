package org.cru.godtools.ui.dashboard.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import org.ccci.gto.android.common.androidx.lifecycle.compose.OnResume
import org.cru.godtools.R
import org.cru.godtools.base.ui.dashboard.Page
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.banner.TutorialFeaturesBanner
import org.cru.godtools.ui.tools.LessonToolCard
import org.cru.godtools.ui.tools.PreloadTool
import org.cru.godtools.ui.tools.SquareToolCard
import org.cru.godtools.ui.tools.ToolCard

private val PADDING_HORIZONTAL = 16.dp

@Preview(showBackground = true)
@Composable
internal fun HomeLayout(
    viewModel: HomeViewModel = viewModel(),
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit = { _, _, _ -> },
    onOpenToolDetails: (String) -> Unit = {},
    onShowDashboardPage: (Page) -> Unit = {},
    onUpdateCurrentPage: (Page) -> Unit = {}
) {
    val pageStack = rememberSaveable(
        saver = listSaver(save = { it }, restore = { it.toMutableStateList() })
    ) { mutableStateListOf(Page.HOME) }
    BackHandler(pageStack.size > 1) { pageStack.removeLast() }
    val currentPage by remember { derivedStateOf { pageStack.last() } }
    LaunchedEffect(currentPage) { onUpdateCurrentPage(currentPage) }

    SwipeRefresh(
        rememberSwipeRefreshState(viewModel.isSyncRunning.collectAsState().value),
        onRefresh = { viewModel.triggerSync(true) }
    ) {
        when (currentPage) {
            Page.HOME -> {
                HomeContent(
                    viewModel,
                    onOpenTool = onOpenTool,
                    onOpenToolDetails = onOpenToolDetails,
                    onViewAllFavorites = { pageStack.add(Page.FAVORITE_TOOLS) },
                    onViewAllTools = { onShowDashboardPage(Page.ALL_TOOLS) }
                )
            }
            Page.FAVORITE_TOOLS -> {
                AllFavoritesList(
                    viewModel,
                    onOpenTool = onOpenTool,
                    onOpenToolDetails = onOpenToolDetails
                )
            }
            else -> Unit
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun HomeContent(
    viewModel: HomeViewModel,
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit,
    onOpenToolDetails: (String) -> Unit,
    onViewAllFavorites: () -> Unit,
    onViewAllTools: () -> Unit
) {
    OnResume { viewModel.trackPageInAnalytics(Page.HOME) }

    val favoriteTools by viewModel.favoriteTools.collectAsState()
    val spotlightLessons by viewModel.spotlightLessons.collectAsState()
    val favoriteToolsLoaded by remember { derivedStateOf { favoriteTools != null } }
    val hasFavoriteTools by remember { derivedStateOf { !favoriteTools.isNullOrEmpty() } }

    val columnState = rememberLazyListState()
    val showTutorialFeaturesBanner by viewModel.showTutorialFeaturesBanner.collectAsState()
    LaunchedEffect(showTutorialFeaturesBanner) {
        if (showTutorialFeaturesBanner) columnState.animateScrollToItem(0)
    }

    LazyColumn(state = columnState, contentPadding = PaddingValues(bottom = 16.dp)) {
        item("banners", "banners") {
            Banners(
                viewModel,
                modifier = Modifier
                    .animateItemPlacement()
                    .fillMaxWidth()
            )
        }

        item("welcome") {
            WelcomeMessage(
                modifier = Modifier
                    .animateItemPlacement()
                    .padding(horizontal = PADDING_HORIZONTAL)
                    .padding(top = 16.dp)
            )
        }

        // featured lessons
        if (spotlightLessons.isNotEmpty()) {
            item("lesson-header", "lesson-header") {
                FeaturedLessonsHeader(
                    modifier = Modifier
                        .animateItemPlacement()
                        .padding(horizontal = PADDING_HORIZONTAL)
                        .padding(top = 32.dp, bottom = 16.dp)
                )
            }

            items(spotlightLessons, key = { it }, contentType = { "lesson-tool-card" }) {
                LessonToolCard(
                    it,
                    onClick = { tool, translation -> onOpenTool(tool, translation, null) },
                    modifier = Modifier
                        .animateItemPlacement()
                        .padding(horizontal = PADDING_HORIZONTAL)
                        .padding(bottom = 16.dp)
                )
            }
        }

        // favorite tools
        if (favoriteToolsLoaded) {
            item("favorites-header") {
                FavoritesHeader(
                    showViewAll = { hasFavoriteTools },
                    onViewAllFavorites = onViewAllFavorites,
                    modifier = Modifier
                        .animateItemPlacement()
                        .padding(horizontal = PADDING_HORIZONTAL)
                        .padding(top = 32.dp, bottom = 16.dp),
                )
            }

            if (hasFavoriteTools) {
                item("favorites", "favorites") {
                    HorizontalFavoriteTools(
                        { favoriteTools.orEmpty().take(5) },
                        onOpenTool = onOpenTool,
                        onOpenToolDetails = onOpenToolDetails,
                        modifier = Modifier
                            .animateItemPlacement()
                            .fillMaxWidth()
                    )
                }
            } else {
                item("favorites-empty", "favorites-empty") {
                    NoFavoriteTools(
                        onViewAllTools = onViewAllTools,
                        modifier = Modifier
                            .animateItemPlacement()
                            .padding(horizontal = PADDING_HORIZONTAL)
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun Banners(viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    val showTutorialFeaturesBanner by viewModel.showTutorialFeaturesBanner.collectAsState()

    Box(modifier = modifier.heightIn(min = 1.dp)) {
        AnimatedContent(
            targetState = showTutorialFeaturesBanner,
            transitionSpec = {
                slideInVertically(initialOffsetY = { -it }) with slideOutVertically(targetOffsetY = { -it })
            }
        ) {
            if (it) TutorialFeaturesBanner()
        }
    }
}

@Composable
private fun WelcomeMessage(modifier: Modifier = Modifier) = Text(
    stringResource(R.string.dashboard_home_header_title),
    style = MaterialTheme.typography.headlineMedium,
    modifier = modifier.fillMaxWidth()
)

@Composable
private fun FeaturedLessonsHeader(modifier: Modifier = Modifier) = Text(
    stringResource(R.string.dashboard_home_section_featured_lesson),
    style = MaterialTheme.typography.titleLarge,
    modifier = modifier.fillMaxWidth()
)

@Composable
private fun FavoritesHeader(
    showViewAll: () -> Boolean,
    modifier: Modifier = Modifier,
    onViewAllFavorites: () -> Unit = {}
) = Row(modifier = modifier.fillMaxWidth()) {
    Text(
        stringResource(R.string.dashboard_home_section_favorites_title),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .weight(1f)
            .alignByBaseline()
    )

    AnimatedVisibility(
        showViewAll(),
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.alignByBaseline()
    ) {
        Text(
            stringResource(R.string.dashboard_home_section_favorites_action_view_all),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onViewAllFavorites)
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun HorizontalFavoriteTools(
    tools: () -> List<Tool>,
    modifier: Modifier = Modifier,
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit,
    onOpenToolDetails: (String) -> Unit,
) = LazyRow(
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    modifier = modifier
) {
    items(tools(), key = { it.code.orEmpty() }) {
        PreloadTool(it)

        SquareToolCard(
            toolCode = it.code.orEmpty(),
            confirmRemovalFromFavorites = true,
            onOpenTool = { tool, trans1, trans2 -> onOpenTool(tool, trans1, trans2) },
            onOpenToolDetails = onOpenToolDetails,
            modifier = Modifier.animateItemPlacement()
        )
    }
}

@Preview
@Composable
private fun NoFavoriteTools(
    modifier: Modifier = Modifier,
    onViewAllTools: () -> Unit = {}
) = Surface(
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = Shapes.None,
    modifier = modifier
        .fillMaxWidth()
        .heightIn(min = 215.dp)
) {
    Column(verticalArrangement = Arrangement.Center, modifier = Modifier.padding(16.dp)) {
        Text(
            stringResource(R.string.dashboard_home_section_favorites_no_tools_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            stringResource(R.string.dashboard_home_section_favorites_no_tools_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onViewAllTools,
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.dashboard_home_section_favorites_action_all_tools))
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun AllFavoritesList(
    viewModel: HomeViewModel,
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit,
    onOpenToolDetails: (String) -> Unit,
) {
    OnResume { viewModel.trackPageInAnalytics(Page.FAVORITE_TOOLS) }

    val favoriteTools by viewModel.reorderableFavoriteTools.collectAsState()

    val reorderableState = rememberReorderableLazyListState(
        // only support reordering tool items
        canDragOver = { (_, k) -> (k as? String)?.startsWith("tool:") == true },
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
                    onOpenTool = { tool, trans1, trans2 -> onOpenTool(tool, trans1, trans2) },
                    onOpenToolDetails = onOpenToolDetails,
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

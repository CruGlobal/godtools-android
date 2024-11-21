package org.cru.godtools.ui.dashboard.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import org.cru.godtools.BuildConfig
import org.cru.godtools.R
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_LESSON
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL_DETAILS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_FAVORITE
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_FEATURED
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.banner.Banners
import org.cru.godtools.ui.dashboard.home.HomeScreen.UiState
import org.cru.godtools.ui.tools.LessonToolCard
import org.cru.godtools.ui.tools.SquareToolCard
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardEvent
import org.cru.godtools.ui.tools.toolViewModels

private val PADDING_HORIZONTAL = 16.dp

internal sealed interface DashboardHomeEvent {
    open class OpenTool(val tool: String?, val type: Tool.Type?, val lang1: Locale?, val lang2: Locale? = null) :
        DashboardHomeEvent {
        constructor(event: ToolCardEvent) : this(event.tool, event.toolType, event.lang1, event.lang2)
    }
    open class OpenToolDetails(val tool: String?) : DashboardHomeEvent {
        constructor(event: ToolCardEvent.OpenToolDetails) : this(event.tool)
    }
    class OpenLesson(event: ToolCardEvent) : OpenTool(event.tool, Tool.Type.LESSON, event.lang1)
    data object ViewAllFavorites : DashboardHomeEvent
    data object ViewAllTools : DashboardHomeEvent
}

@Composable
internal fun HomeContent(onEvent: (DashboardHomeEvent) -> Unit, viewModel: HomeViewModel = viewModel()) {
    val favoriteTools by viewModel.favoriteTools.collectAsState()

    val state = UiState(
        banner = viewModel.banner.collectAsState().value,
        spotlightLessons = viewModel.spotlightLessons.collectAsState().value,
        favoriteTools = favoriteTools.orEmpty().take(5).mapNotNull { tool ->
            key(tool.code) {
                val toolViewModel = toolViewModels[tool.code ?: return@mapNotNull null, tool]
                lateinit var toolState: ToolCard.State
                toolState = toolViewModel.toState {
                    when (it) {
                        ToolCard.Event.Click, ToolCard.Event.OpenTool -> {
                            viewModel.recordOpenClickInAnalytics(ACTION_OPEN_TOOL, tool.code, SOURCE_FAVORITE)
                            onEvent(
                                DashboardHomeEvent.OpenTool(
                                    ToolCardEvent.Click(
                                        tool = tool.code,
                                        type = tool.type,
                                        lang1 = toolState.translation?.languageCode,
                                    )
                                )
                            )
                        }
                        ToolCard.Event.OpenToolDetails -> {
                            viewModel.recordOpenClickInAnalytics(ACTION_OPEN_TOOL_DETAILS, tool.code, SOURCE_FAVORITE)
                            onEvent(DashboardHomeEvent.OpenToolDetails(tool.code))
                        }
                        ToolCard.Event.PinTool -> toolViewModel.pinTool()
                        ToolCard.Event.UnpinTool -> toolViewModel.unpinTool()
                    }
                }
                toolState
            }
        },
        favoriteToolsLoaded = favoriteTools != null,
    )

    val banner by rememberUpdatedState(state.banner)
    val spotlightLessons by rememberUpdatedState(state.spotlightLessons)
    val favoriteToolsLoaded by rememberUpdatedState(state.favoriteToolsLoaded)

    val hasFavoriteTools by rememberUpdatedState(state.favoriteTools.isNotEmpty())

    val columnState = rememberLazyListState()
    LaunchedEffect(banner) { if (banner != null) columnState.animateScrollToItem(0) }

    LazyColumn(state = columnState, contentPadding = PaddingValues(bottom = 16.dp)) {
        item("banners", "banners") {
            Banners(
                { banner },
                modifier = Modifier
                    .animateItem()
                    .fillMaxWidth()
            )
        }

        item("welcome") {
            WelcomeMessage(
                modifier = Modifier
                    .animateItem()
                    .padding(horizontal = PADDING_HORIZONTAL)
                    .padding(top = 16.dp)
            )
        }

        // featured lessons
        if (spotlightLessons.isNotEmpty()) {
            item("lesson-header", "lesson-header") {
                FeaturedLessonsHeader(
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = PADDING_HORIZONTAL)
                        .padding(top = 32.dp, bottom = 16.dp)
                )
            }

            items(spotlightLessons, key = { it }, contentType = { "lesson-tool-card" }) { lesson ->
                LessonToolCard(
                    lesson,
                    onEvent = {
                        when (it) {
                            is ToolCardEvent.Click, is ToolCardEvent.OpenTool -> {
                                viewModel.recordOpenClickInAnalytics(ACTION_OPEN_LESSON, it.tool, SOURCE_FEATURED)
                                onEvent(DashboardHomeEvent.OpenLesson(it))
                            }
                            is ToolCardEvent.OpenToolDetails -> {
                                if (BuildConfig.DEBUG) error("$it is currently unsupported for Lesson Cards")
                            }
                        }
                    },
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = PADDING_HORIZONTAL)
                        .padding(bottom = 16.dp)
                )
            }
        }

        // favorite tools
        if (favoriteToolsLoaded) {
            item("favorites-header") {
                FavoritesHeader(
                    showViewAll = hasFavoriteTools,
                    onEvent = onEvent,
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = PADDING_HORIZONTAL)
                        .padding(top = 32.dp, bottom = 16.dp),
                )
            }

            if (hasFavoriteTools) {
                item("favorites", "favorites") {
                    HorizontalFavoriteTools(
                        state,
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                    )
                }
            } else {
                item("favorites-empty", "favorites-empty") {
                    NoFavoriteTools(
                        onEvent = onEvent,
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = PADDING_HORIZONTAL)
                    )
                }
            }
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
    showViewAll: Boolean,
    onEvent: (DashboardHomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) = Row(modifier = modifier.fillMaxWidth()) {
    Text(
        stringResource(R.string.dashboard_home_section_favorites_title),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .weight(1f)
            .alignByBaseline()
    )

    AnimatedVisibility(
        showViewAll,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.alignByBaseline()
    ) {
        Text(
            stringResource(R.string.dashboard_home_section_favorites_action_view_all),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onEvent(DashboardHomeEvent.ViewAllFavorites) }
        )
    }
}

@Composable
private fun HorizontalFavoriteTools(state: UiState, modifier: Modifier = Modifier) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(state.favoriteTools, key = { it.toolCode.orEmpty() }) { toolState ->
            SquareToolCard(
                state = toolState,
                confirmRemovalFromFavorites = true,
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Preview
@Composable
private fun NoFavoriteTools(modifier: Modifier = Modifier, onEvent: (DashboardHomeEvent) -> Unit = {}) = Surface(
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = RectangleShape,
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
            onClick = { onEvent(DashboardHomeEvent.ViewAllTools) },
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.dashboard_home_section_favorites_action_all_tools))
        }
    }
}

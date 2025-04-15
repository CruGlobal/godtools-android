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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.OverlayEffect
import dagger.hilt.components.SingletonComponent
import org.cru.godtools.R
import org.cru.godtools.ui.banner.Banners
import org.cru.godtools.ui.dashboard.OptInNotificationModalOverlay
import org.cru.godtools.ui.dashboard.home.HomeScreen.UiEvent
import org.cru.godtools.ui.dashboard.home.HomeScreen.UiState
import org.cru.godtools.ui.tools.LessonToolCard
import org.cru.godtools.ui.tools.SquareToolCard

private val PADDING_HORIZONTAL = 16.dp

@Composable
@CircuitInject(HomeScreen::class, SingletonComponent::class)
internal fun HomeLayout(state: UiState, modifier: Modifier = Modifier) {
    val banner by rememberUpdatedState(state.banner)
    val favoriteToolsLoaded by rememberUpdatedState(state.favoriteToolsLoaded)

    val hasFavoriteTools by rememberUpdatedState(state.favoriteTools.isNotEmpty())

    val columnState = rememberLazyListState()
    LaunchedEffect(banner) { if (banner != null) columnState.animateScrollToItem(0) }

    var testShouldShow by remember { mutableStateOf(true) }

    ShowTheDialog(testShouldShow)

    LazyColumn(state = columnState, contentPadding = PaddingValues(bottom = 16.dp), modifier = modifier) {
        item {
            Button(
                onClick = {
                    testShouldShow = !testShouldShow
                },

            ) {
                Text("Change Bool")
            }
        }



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
        if (state.spotlightLessons.isNotEmpty()) {
            item("lesson-header", "lesson-header") {
                FeaturedLessonsHeader(
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = PADDING_HORIZONTAL)
                        .padding(top = 32.dp, bottom = 16.dp)
                )
            }

            items(
                state.spotlightLessons,
                key = { it.toolCode.orEmpty() },
                contentType = { "lesson-tool-card" }
            ) { lessonState ->
                LessonToolCard(
                    lessonState,
                    showLanguage = false,
                    showProgress = true,
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
                    state = state,
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
                        state = state,
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = PADDING_HORIZONTAL)
                    )
                }
            }
        }
    }
}
//here
@Composable
private fun ShowTheDialog(shouldShow: Boolean) {
    OverlayEffect {
        if (shouldShow) {
            println("showOverlay is TRUE")
            val result = show(OptInNotificationModalOverlay())
            println("Result: $result")
        } else {
            println("showOverlay is FALSE")
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
private fun FavoritesHeader(state: UiState, modifier: Modifier = Modifier) = Row(modifier = modifier.fillMaxWidth()) {
    val eventSink by rememberUpdatedState(state.eventSink)

    Text(
        stringResource(R.string.dashboard_home_section_favorites_title),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .weight(1f)
            .alignByBaseline()
    )

    AnimatedVisibility(
        state.favoriteTools.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.alignByBaseline()
    ) {
        Text(
            stringResource(R.string.dashboard_home_section_favorites_action_view_all),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { eventSink(UiEvent.ViewAllFavorites) }
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

@Composable
private fun NoFavoriteTools(state: UiState, modifier: Modifier = Modifier) = Surface(
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = RectangleShape,
    modifier = modifier
        .fillMaxWidth()
        .heightIn(min = 215.dp)
) {
    val eventSink by rememberUpdatedState(state.eventSink)

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
            onClick = { eventSink(UiEvent.ViewAllTools) },
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.dashboard_home_section_favorites_action_all_tools))
        }
    }
}

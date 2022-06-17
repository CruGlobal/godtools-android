package org.cru.godtools.ui.dashboard.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.tools.LessonToolCard
import org.cru.godtools.ui.tools.SquareToolCard

private val PADDING_HORIZONTAL = 16.dp

@Preview(showBackground = true)
@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun HomeLayout(
    viewModel: HomeViewModel = viewModel(),
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit = { _, _, _ -> },
    onOpenToolDetails: (String) -> Unit = {},
    onViewAllFavorites: () -> Unit = {},
    onViewAllTools: () -> Unit = {}
) = GodToolsTheme {
    val favoriteTools by viewModel.favoriteTools.collectAsState()
    val spotlightLessons by viewModel.spotlightLessons.collectAsState()
    val favoriteToolsLoaded by remember { derivedStateOf { favoriteTools != null } }
    val hasFavoriteTools by remember { derivedStateOf { !favoriteTools.isNullOrEmpty() } }

    LazyColumn(contentPadding = PaddingValues(vertical = 16.dp)) {
        item("welcome") { WelcomeMessage(modifier = Modifier.padding(horizontal = PADDING_HORIZONTAL)) }

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
                    FavoriteTools(
                        { favoriteTools },
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
private fun FavoriteTools(
    tools: () -> List<String>?,
    modifier: Modifier = Modifier,
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit = { _, _, _ -> },
    onOpenToolDetails: (String) -> Unit = {},
) = LazyRow(
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    modifier = modifier
) {
    items(tools().orEmpty(), key = { it }) {
        SquareToolCard(
            toolCode = it,
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

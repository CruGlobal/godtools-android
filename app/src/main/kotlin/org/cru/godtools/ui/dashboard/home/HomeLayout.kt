package org.cru.godtools.ui.dashboard.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.tools.LessonToolCard
import org.cru.godtools.ui.tools.SquareToolCard

@Preview(showBackground = true)
@Composable
internal fun HomeLayout(
    viewModel: HomeViewModel = viewModel(),
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit = { _, _, _ -> },
    onOpenToolDetails: (String) -> Unit = {},
    onViewAllFavorites: () -> Unit = {}
) = GodToolsTheme {
    val spotlightLessons by viewModel.spotlightLessons.collectAsState()

    LazyColumn(contentPadding = PaddingValues(vertical = 16.dp)) {
        item("welcome") { WelcomeMessage(modifier = Modifier.padding(horizontal = 16.dp)) }

        // featured lessons
        if (spotlightLessons.isNotEmpty()) {
            item("lesson-header") {
                FeaturedLessonsHeader(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 32.dp, bottom = 16.dp)
                )
            }

            items(spotlightLessons, key = { it }, contentType = { "lesson-tool-card" }) {
                LessonToolCard(
                    it,
                    onClick = { tool, translation -> onOpenTool(tool, translation, null) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                )
            }
        }

        // favorite tools
        item("favorites") {
            val favoriteTools by viewModel.favoriteTools.collectAsState()
            val hasFavoriteTools by remember { derivedStateOf { favoriteTools.isNotEmpty() } }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(top = 32.dp, bottom = 16.dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.dashboard_home_section_favorites_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .weight(1f)
                        .alignByBaseline()
                )

                if (hasFavoriteTools) {
                    Text(
                        stringResource(R.string.dashboard_home_section_favorites_action_view_all),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable(onClick = onViewAllFavorites)
                            .alignByBaseline()
                    )
                }
            }

            if (hasFavoriteTools) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(favoriteTools, key = { it }) {
                        SquareToolCard(
                            toolCode = it,
                            onOpenTool = { tool, trans1, trans2 -> onOpenTool(tool, trans1, trans2) },
                            onOpenToolDetails = onOpenToolDetails
                        )
                    }
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

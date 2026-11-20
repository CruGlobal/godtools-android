package org.cru.godtools.ui.dashboard.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import org.ccci.gto.android.common.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.LessonsScreen
import org.cru.godtools.ui.dashboard.LocalizationSettingsBox
import org.cru.godtools.ui.dashboard.PersonalizationUnavailable
import org.cru.godtools.ui.dashboard.lessons.LessonsPresenter.UiEvent
import org.cru.godtools.ui.dashboard.lessons.LessonsPresenter.UiState
import org.cru.godtools.ui.dashboard.personalization.rememberFloatLastItemsToBottomArrangement
import org.cru.godtools.ui.tools.LessonToolCard
import org.cru.godtools.ui.tools.SquareToolCard
import org.cru.godtools.ui.tools.ToolCardPresenter

internal val MARGIN_LESSONS_LAYOUT_HORIZONTAL = 16.dp

@Composable
@CircuitInject(LessonsScreen::class, SingletonComponent::class)
internal fun LessonsLayout(state: UiState, modifier: Modifier = Modifier) {
    val isPersonalization = state.mode == UiState.Mode.PERSONALIZATION
    val showNoPersonalizedLessons = isPersonalization && state.dataLoaded && state.lessons.isEmpty()
    // the unavailable panel already offers the localization settings action
    val showLocalizationSettingsBox = isPersonalization && !showNoPersonalizedLessons

    LazyColumn(
        verticalArrangement = rememberFloatLastItemsToBottomArrangement(
            numToFloat = if (showLocalizationSettingsBox) 1 else 0
        ),
        modifier = modifier.fillMaxHeight()
    ) {
        item("header", "header") {
            LessonsHeader(state.mode, Modifier.padding(top = 16.dp, horizontal = MARGIN_LESSONS_LAYOUT_HORIZONTAL))
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = MARGIN_LESSONS_LAYOUT_HORIZONTAL)
            )
        }

        if (state.featuredLessons.isNotEmpty()) {
            item("featured-lessons", "featured-lessons") {
                FeaturedLessons(state.featuredLessons, modifier = Modifier.animateItem())
                HorizontalDivider(
                    modifier = Modifier
                        .animateItem()
                        .padding(vertical = 16.dp, horizontal = MARGIN_LESSONS_LAYOUT_HORIZONTAL)
                )
            }
        }

        item("filters", "filters") {
            LessonFilters(state, modifier = Modifier.padding(horizontal = MARGIN_LESSONS_LAYOUT_HORIZONTAL))
        }

        items(state.lessons, { it.toolCode.orEmpty() }, { "lesson" }) { toolState ->
            LessonToolCard(
                toolState,
                showLanguage = true,
                showProgress = true,
                modifier = Modifier
                    .animateItem()
                    .padding(top = 16.dp, horizontal = MARGIN_LESSONS_LAYOUT_HORIZONTAL)
            )
        }

        if (showNoPersonalizedLessons) {
            item("no-personalized-lessons", "no-personalized-lessons") {
                PersonalizationUnavailable(
                    title = R.string.dashboard_lessons_section_personalized_no_lessons_title,
                    description = R.string.dashboard_lessons_section_personalized_no_lessons_description,
                    goToAllAction = R.string.dashboard_lessons_section_personalized_no_lessons_action_all_lessons,
                    onChangeLocalizationSettings = { state.eventSink(UiEvent.OpenLocalizationSettings) },
                    onGoToAll = { state.eventSink(UiEvent.ChangeMode(UiState.Mode.ALL_LESSONS)) },
                    modifier = Modifier.padding(top = 16.dp, horizontal = MARGIN_LESSONS_LAYOUT_HORIZONTAL)
                )
            }
        }

        item("spacer", "spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showLocalizationSettingsBox) {
            item("localization-settings-box", "localization-settings-box") {
                LocalizationSettingsBox(
                    title = R.string.dashboard_lessons_section_personalized_localization_title,
                    description = R.string.dashboard_lessons_section_personalized_localization_text,
                    onClickSettings = { state.eventSink(UiEvent.OpenLocalizationSettings) }
                )
            }
        }
    }
}

@Composable
private fun FeaturedLessons(lessons: List<ToolCardPresenter.UiState>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.dashboard_lessons_section_featured_label),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(horizontal = MARGIN_LESSONS_LAYOUT_HORIZONTAL)
                .fillMaxWidth()
        )
        Text(
            stringResource(R.string.dashboard_lessons_section_featured_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(top = 4.dp, horizontal = MARGIN_LESSONS_LAYOUT_HORIZONTAL)
                .fillMaxWidth()
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = MARGIN_LESSONS_LAYOUT_HORIZONTAL),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(lessons, key = { it.toolCode.orEmpty() }) { toolState ->
                SquareToolCard(
                    state = toolState,
                    showCategory = false,
                    // shows the featured language availability, like featured tools
                    showSecondLanguage = true,
                    showActions = false,
                    showFavoriteAction = false,
                    floatParallelLanguageUp = false,
                )
            }
        }
    }
}

@Composable
private fun LessonsHeader(mode: UiState.Mode, modifier: Modifier = Modifier) = Column(modifier = modifier) {
    Text(
        stringResource(
            when (mode) {
                UiState.Mode.PERSONALIZATION -> R.string.dashboard_lessons_header_title_personalized
                UiState.Mode.ALL_LESSONS -> R.string.dashboard_lessons_header_title_all
            }
        ),
        style = MaterialTheme.typography.headlineMedium
    )
    Text(
        stringResource(R.string.dashboard_lessons_header_description),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 4.dp)
    )
}

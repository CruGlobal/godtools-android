package org.cru.godtools.ui.dashboard.lessons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import org.ccci.gto.android.common.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.LessonsScreen
import org.cru.godtools.ui.dashboard.LocalizationSettingsBox
import org.cru.godtools.ui.dashboard.lessons.LessonsPresenter.UiEvent
import org.cru.godtools.ui.dashboard.lessons.LessonsPresenter.UiState
import org.cru.godtools.ui.dashboard.personalization.rememberFloatLastItemsToBottomArrangement
import org.cru.godtools.ui.tools.LessonToolCard

internal val MARGIN_LESSONS_LAYOUT_HORIZONTAL = 16.dp

@Composable
@CircuitInject(LessonsScreen::class, SingletonComponent::class)
internal fun LessonsLayout(state: UiState, modifier: Modifier = Modifier) {
    LazyColumn(
        verticalArrangement = rememberFloatLastItemsToBottomArrangement(
            numToFloat = if (state.mode == UiState.Mode.PERSONALIZATION) 1 else 0
        ),
        modifier = modifier.fillMaxHeight()
    ) {
        if (state.isPersonalizationEnabled) {
            item("mode-toggle", "mode-toggle") {
                PersonalizationToggle(
                    state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }

        item("header", "header") {
            LessonsHeader(state.mode, Modifier.padding(top = 16.dp, horizontal = MARGIN_LESSONS_LAYOUT_HORIZONTAL))
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = MARGIN_LESSONS_LAYOUT_HORIZONTAL)
            )
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

        if (state.mode == UiState.Mode.PERSONALIZATION && state.dataLoaded && state.lessons.isEmpty()) {
            item("no-personalized-lessons", "no-personalized-lessons") {
                NoPersonalizedLessons(
                    onGoToAllLessons = { state.eventSink(UiEvent.ChangeMode(UiState.Mode.ALL_LESSONS)) },
                    modifier = Modifier.padding(top = 16.dp, horizontal = MARGIN_LESSONS_LAYOUT_HORIZONTAL)
                )
            }
        }

        item("spacer", "spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.mode == UiState.Mode.PERSONALIZATION) {
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
private fun PersonalizationToggle(state: UiState, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        SegmentedButton(
            selected = state.mode == UiState.Mode.PERSONALIZATION,
            onClick = { state.eventSink(UiEvent.ChangeMode(UiState.Mode.PERSONALIZATION)) },
            shape = SegmentedButtonDefaults.itemShape(0, 2),
        ) {
            Text(stringResource(R.string.dashboard_lessons_toggle_personalized))
        }

        SegmentedButton(
            selected = state.mode == UiState.Mode.ALL_LESSONS,
            onClick = { state.eventSink(UiEvent.ChangeMode(UiState.Mode.ALL_LESSONS)) },
            shape = SegmentedButtonDefaults.itemShape(1, 2),
        ) {
            Text(stringResource(R.string.dashboard_lessons_toggle_all))
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
        style = MaterialTheme.typography.titleLarge
    )
    Text(
        stringResource(R.string.dashboard_lessons_header_description),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 4.dp)
    )
}

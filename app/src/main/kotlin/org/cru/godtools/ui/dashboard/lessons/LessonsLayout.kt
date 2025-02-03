package org.cru.godtools.ui.dashboard.lessons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.cru.godtools.R
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.cru.godtools.ui.tools.LessonToolCard
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.toolViewModels

internal sealed interface DashboardLessonsEvent {
    class OpenLesson(val lesson: String?, val lang: Locale?) : DashboardLessonsEvent
}

@Composable
internal fun LessonsLayout(viewModel: LessonsViewModel = viewModel(), onEvent: (DashboardLessonsEvent) -> Unit = {}) {
    val languageFilter = FilterMenu.UiState(
        menuExpanded = rememberSaveable { mutableStateOf(false) },
        query = rememberSaveable { mutableStateOf("") },
        items = viewModel.filteredLanguages.collectAsState(persistentListOf()).value,
        selectedItem = viewModel.selectedLanguage.collectAsState().value,
        eventSink = {
            when (it) {
                is FilterMenu.Event.SelectItem -> it.item?.let { viewModel.updateSelectedLanguage(it) }
            }
        }
    )
    LaunchedEffect(languageFilter.query.value) { viewModel.query.value = languageFilter.query.value }

    val state = LessonsScreen.UiState(
        languageFilter = languageFilter,
        lessons = viewModel.lessons.collectAsState().value
            .map { lesson ->
                key(lesson) {
                    lateinit var state: ToolCard.State
                    state = toolViewModels[lesson].toState(language = languageFilter.selectedItem) {
                        when (it) {
                            ToolCard.Event.Click -> {
                                viewModel.recordOpenLessonInAnalytics(lesson)
                                onEvent(DashboardLessonsEvent.OpenLesson(lesson, state.translation?.languageCode))
                            }

                            else -> TODO()
                        }
                    }
                    state
                }
            }
            .toImmutableList()
    )

    LessonsLayout(state)
}

@Composable
internal fun LessonsLayout(state: LessonsScreen.UiState, modifier: Modifier = Modifier) {
    LazyColumn(contentPadding = PaddingValues(16.dp), modifier = modifier) {
        item("header", "header") {
            LessonsHeader()
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            LessonFilters(state)
        }

        items(state.lessons, { it.toolCode.orEmpty() }, { "lesson" }) { toolState ->
            LessonToolCard(
                toolState,
                modifier = Modifier
                    .animateItem()
                    .padding(top = 16.dp)
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun LessonsHeader() = Column {
    Text(
        stringResource(R.string.dashboard_lessons_header_title),
        style = MaterialTheme.typography.titleLarge
    )
    Text(
        stringResource(R.string.dashboard_lessons_header_description),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 4.dp)
    )
}

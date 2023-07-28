package org.cru.godtools.ui.dashboard.lessons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import org.cru.godtools.BuildConfig
import org.cru.godtools.R
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.tools.LessonToolCard
import org.cru.godtools.ui.tools.ToolCardEvent

internal sealed interface DashboardLessonsEvent {
    class OpenLesson(val tool: Tool?, val lang: Locale?) : DashboardLessonsEvent
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun LessonsLayout(
    viewModel: LessonsViewModel = viewModel(),
    onEvent: (DashboardLessonsEvent) -> Unit = {},
) {
    val lessons by viewModel.lessons.collectAsState()
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item("header", "header") { LessonsHeader() }

        items(lessons, { it }, { "lesson" }) { lesson ->
            LessonToolCard(
                lesson,
                onEvent = {
                    when (it) {
                        is ToolCardEvent.OpenTool, is ToolCardEvent.Click -> {
                            viewModel.recordOpenLessonInAnalytics(it.tool?.code)
                            onEvent(DashboardLessonsEvent.OpenLesson(it.tool, it.lang1))
                        }
                        is ToolCardEvent.OpenToolDetails -> {
                            if (BuildConfig.DEBUG) error("$it is currently unsupported for Lessons")
                        }
                    }
                },
                modifier = Modifier
                    .animateItemPlacement()
                    .padding(top = 16.dp)
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun LessonsHeader() = Column {
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

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import org.cru.godtools.R
import org.cru.godtools.ui.tools.LessonToolCard

@Composable
@CircuitInject(LessonsScreen::class, SingletonComponent::class)
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
                showLanguage = true,
                showProgress = true,
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

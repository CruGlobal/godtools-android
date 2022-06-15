package org.cru.godtools.ui.dashboard.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.tools.LessonToolCard

@Preview(showBackground = true)
@Composable
internal fun HomeLayout(
    viewModel: HomeViewModel = viewModel(),
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit = { _, _, _ -> },
) = GodToolsTheme {
    val spotlightLessons by viewModel.spotlightLessons.collectAsState()

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        WelcomeMessage()
        if (spotlightLessons.isNotEmpty()) {
            FeaturedLessons(
                spotlightLessons,
                onOpenLesson = { tool, translation -> onOpenTool(tool, translation, null) }
            )
        }
    }
}

private fun LazyListScope.WelcomeMessage() = item("welcome") {
    Text(
        stringResource(R.string.dashboard_home_header_title),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun LazyListScope.FeaturedLessons(
    lessons: List<String>,
    onOpenLesson: (Tool?, Translation?) -> Unit = { _, _ -> }
) {
    // featured lessons
    item("lesson-header") {
        Text(
            stringResource(R.string.dashboard_home_section_featured_lesson),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(top = 32.dp, bottom = 16.dp)
                .fillMaxWidth()
        )
    }

    items(lessons, key = { it }, contentType = { "lesson-tool-card" }) {
        LessonToolCard(
            it,
            onClick = { tool, translation -> onOpenLesson(tool, translation) },
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

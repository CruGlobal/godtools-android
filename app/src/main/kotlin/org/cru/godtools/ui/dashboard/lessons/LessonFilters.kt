package org.cru.godtools.ui.dashboard.lessons

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.cru.godtools.R
import org.cru.godtools.base.LocalAppLanguage
import org.cru.godtools.ui.dashboard.filters.LazyFilterMenu
import org.cru.godtools.ui.languages.LanguageName

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun LessonFilters(state: LessonsScreen.UiState, modifier: Modifier = Modifier) {
    FlowRow(modifier = modifier) {
        Text(
            stringResource(R.string.dashboard_lessons_section_filter_label),
            modifier = Modifier
                .alignByBaseline()
                .padding(end = 8.dp)
        )
        LazyFilterMenu(
            state.languageFilter,
            buttonLabelText = state.languageFilter.selectedItem
                ?.getDisplayName(LocalContext.current, LocalAppLanguage.current).orEmpty(),
            itemKey = { (it) -> it.code },
            itemLabel = { LanguageName(it.item.code) },
            itemSupportingText = { (_, count) ->
                pluralStringResource(R.plurals.dashboard_lessons_section_filter_available_lessons, count, count)
            },
            modifier = Modifier
                .weight(1f)
                .widthIn(min = 175.dp)
                .alignByBaseline()
        )
    }
}

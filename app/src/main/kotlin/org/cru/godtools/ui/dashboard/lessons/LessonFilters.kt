package org.cru.godtools.ui.dashboard.lessons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import org.ccci.gto.android.common.androidx.compose.material3.ui.list.ListItemStartPadding
import org.ccci.gto.android.common.androidx.compose.material3.ui.menu.LazyDropdownMenu
import org.cru.godtools.R
import org.cru.godtools.base.LocalAppLanguage
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.model.Language
import org.cru.godtools.ui.languages.LanguageName

private val DROPDOWN_LESSON_MAX_HEIGHT = 700.dp
private val DROPDOWN_LESSON_MAX_WIDTH = 350.dp

@Composable
fun LessonFilters(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Text(
            stringResource(R.string.dashboard_lessons_section_filter_label),
            modifier = Modifier
                .alignByBaseline()
                .padding(end = 36.dp)
        )
        LessonLanguageFilter(
            modifier = Modifier
                .weight(1f)
                .alignByBaseline()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun LessonLanguageFilter(modifier: Modifier = Modifier, viewModel: LessonsViewModel = viewModel()) {
    val context = LocalContext.current
    val selectedLanguage by viewModel.selectedLanguage.collectAsState(initial = Language(Locale.getDefault()))

    val lessonLanguages by viewModel.filteredLanguages.collectAsState(emptyList())
    var expanded by remember { mutableStateOf(false) }

    ElevatedButton(
        onClick = { expanded = true },
        modifier = modifier.semantics { role = Role.DropdownList }
    ) {
        Text(
            selectedLanguage.getDisplayName(context, LocalAppLanguage.current),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        LazyDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.sizeIn(maxHeight = DROPDOWN_LESSON_MAX_HEIGHT, maxWidth = DROPDOWN_LESSON_MAX_WIDTH)
        ) {
            stickyHeader {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    val query by viewModel.query.collectAsState("")
                    SearchBar(
                        query = query,
                        onQueryChange = { change ->
                            viewModel.query.value = change
                        },
                        onSearch = {},
                        active = false,
                        onActiveChange = {},
                        colors = GodToolsTheme.searchBarColors,
                        placeholder = {
                            Text(stringResource(R.string.language_settings_downloadable_languages_search))
                        },
                        content = {},
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                            .wrapContentWidth()
                    )
                }
            }

            itemsIndexed(lessonLanguages, key = { _, (language) -> language.code }) { index, (lang, count) ->
                if (index > 0) {
                    HorizontalDivider(Modifier.padding(start = ListItemStartPadding, end = ListItemStartPadding))
                }
                FilterMenuItem(
                    label = { LanguageName(lang) },
                    supportingText = pluralStringResource(
                        R.plurals.dashboard_lessons_section_filter_available_lessons,
                        count,
                        count,
                    ),
                    onClick = {
                        expanded = false
                        viewModel.updateSelectedLanguage(lang)
                        viewModel.query.value = ""
                    },
                    // TODO: Animate item placement - Add back when Compose version 1.7 is released
//                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

@Composable
private fun FilterMenuItem(
    label: @Composable () -> Unit,
    supportingText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = ListItem(
    headlineContent = label,
    supportingContent = supportingText?.let { { Text(it) } },
    modifier = modifier.clickable(onClick = onClick)
)

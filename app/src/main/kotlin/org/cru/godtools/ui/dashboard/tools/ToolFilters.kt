package org.cru.godtools.ui.dashboard.tools

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.compose.material3.ui.menu.LazyDropdownMenu
import org.cru.godtools.R
import org.cru.godtools.base.LocalAppLanguage
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.base.ui.util.getToolCategoryName
import org.cru.godtools.ui.languages.LanguageName
import org.jetbrains.annotations.VisibleForTesting

private val DROPDOWN_MAX_HEIGHT = 700.dp
private val DROPDOWN_MAX_WIDTH = 400.dp

internal const val TEST_TAG_FILTER_DROPDOWN = "filter_dropdown"

@Composable
internal fun ToolFilters(viewModel: ToolsViewModel, modifier: Modifier = Modifier) {
    val filters = ToolsScreen.State.Filters(
        categories = viewModel.categories.collectAsState().value,
        selectedCategory = viewModel.selectedCategory.collectAsState().value,
        languages = viewModel.languages.collectAsState().value,
        languageQuery = viewModel.languageQuery.collectAsState().value,
        selectedLanguage = viewModel.selectedLanguage.collectAsState().value,
    )
    val eventSink: (ToolsScreen.Event) -> Unit = remember {
        {
            when (it) {
                is ToolsScreen.Event.UpdateSelectedCategory -> viewModel.setSelectedCategory(it.category)
                is ToolsScreen.Event.UpdateLanguageQuery -> viewModel.setLanguageQuery(it.query)
                is ToolsScreen.Event.UpdateSelectedLanguage -> viewModel.setSelectedLocale(it.locale)
            }
        }
    }

    ToolFilters(filters, modifier = modifier, eventSink = eventSink)
}

@Composable
internal fun ToolFilters(
    filters: ToolsScreen.State.Filters,
    modifier: Modifier = Modifier,
    eventSink: (ToolsScreen.Event) -> Unit = {},
) = Column(modifier.fillMaxWidth()) {
    Text(
        stringResource(R.string.dashboard_tools_section_filter_label),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        CategoryFilter(filters, modifier = Modifier.weight(1f), eventSink = eventSink)
        LanguageFilter(filters, modifier = Modifier.weight(1f), eventSink = eventSink)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CategoryFilter(
    filters: ToolsScreen.State.Filters,
    modifier: Modifier = Modifier,
    eventSink: (ToolsScreen.Event) -> Unit = {},
) {
    val categories by rememberUpdatedState(filters.categories)
    val selectedCategory by rememberUpdatedState(filters.selectedCategory)
    val eventSink by rememberUpdatedState(eventSink)

    var expanded by rememberSaveable { mutableStateOf(false) }

    ElevatedButton(
        onClick = { expanded = !expanded },
        modifier = modifier
    ) {
        Text(
            selectedCategory?.let { getToolCategoryName(it, LocalContext.current) }
                ?: stringResource(R.string.dashboard_tools_section_filter_category_any),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = DROPDOWN_MAX_HEIGHT),
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.dashboard_tools_section_filter_category_any)) },
                onClick = {
                    eventSink(ToolsScreen.Event.UpdateSelectedCategory(null))
                    expanded = false
                }
            )
            categories.forEach {
                DropdownMenuItem(
                    text = { Text(getToolCategoryName(it, LocalContext.current)) },
                    onClick = {
                        eventSink(ToolsScreen.Event.UpdateSelectedCategory(it))
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
@VisibleForTesting
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
internal fun LanguageFilter(
    filters: ToolsScreen.State.Filters,
    modifier: Modifier = Modifier,
    eventSink: (ToolsScreen.Event) -> Unit = {},
) {
    val context = LocalContext.current
    val languages by rememberUpdatedState(filters.languages)
    val query by rememberUpdatedState(filters.languageQuery)
    val selectedLanguage by rememberUpdatedState(filters.selectedLanguage)
    val eventSink by rememberUpdatedState(eventSink)

    var expanded by rememberSaveable { mutableStateOf(false) }

    ElevatedButton(
        onClick = {
            if (!expanded) eventSink(ToolsScreen.Event.UpdateLanguageQuery(""))
            expanded = !expanded
        },
        modifier = modifier
    ) {
        Text(
            text = selectedLanguage?.getDisplayName(context, LocalAppLanguage.current)
                ?: stringResource(R.string.dashboard_tools_section_filter_language_any),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)

        LazyDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .sizeIn(maxHeight = DROPDOWN_MAX_HEIGHT, maxWidth = DROPDOWN_MAX_WIDTH)
                .testTag(TEST_TAG_FILTER_DROPDOWN)
        ) {
            item {
                SearchBar(
                    query,
                    onQueryChange = { eventSink(ToolsScreen.Event.UpdateLanguageQuery(it)) },
                    onSearch = { eventSink(ToolsScreen.Event.UpdateLanguageQuery(it)) },
                    active = false,
                    onActiveChange = {},
                    colors = GodToolsTheme.searchBarColors,
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    placeholder = { Text(stringResource(R.string.language_settings_downloadable_languages_search)) },
                    content = {},
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.dashboard_tools_section_filter_language_any)) },
                    onClick = {
                        eventSink(ToolsScreen.Event.UpdateSelectedLanguage(null))
                        expanded = false
                    }
                )
            }

            items(languages, key = { it.code }) {
                DropdownMenuItem(
                    text = { LanguageName(it) },
                    onClick = {
                        eventSink(ToolsScreen.Event.UpdateSelectedLanguage(it.code))
                        expanded = false
                    },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

package org.cru.godtools.ui.dashboard.tools

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.compose.material3.ui.list.ListItemEndPadding
import org.ccci.gto.android.common.androidx.compose.material3.ui.list.ListItemStartPadding
import org.ccci.gto.android.common.androidx.compose.material3.ui.menu.LazyDropdownMenu
import org.cru.godtools.R
import org.cru.godtools.base.LocalAppLanguage
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.base.ui.util.getToolCategoryName
import org.cru.godtools.model.Language
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.cru.godtools.ui.dashboard.filters.FilterMenuItem
import org.cru.godtools.ui.languages.LanguageName
import org.jetbrains.annotations.VisibleForTesting

private val DROPDOWN_MAX_HEIGHT = 700.dp
private val DROPDOWN_MAX_WIDTH = 350.dp

internal const val TEST_TAG_FILTER_DROPDOWN = "filter_dropdown"

@Composable
internal fun ToolFilters(filters: ToolsScreen.Filters, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
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
            CategoryFilter(filters.categoryFilter, modifier = Modifier.weight(1f))
            LanguageFilter(filters.languageFilter, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
@VisibleForTesting
@OptIn(ExperimentalMaterial3Api::class)
internal fun CategoryFilter(state: FilterMenu.UiState<String>, modifier: Modifier = Modifier) {
    val categories by rememberUpdatedState(state.items)
    val selectedCategory by rememberUpdatedState(state.selectedItem)
    val eventSink by rememberUpdatedState(state.eventSink)

    var expanded by state.menuExpanded

    ElevatedButton(
        onClick = { expanded = !expanded },
        modifier = modifier.semantics { role = Role.DropdownList }
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
            modifier = Modifier
                .heightIn(max = DROPDOWN_MAX_HEIGHT)
                .testTag(TEST_TAG_FILTER_DROPDOWN)
        ) {
            FilterMenuItem(
                label = stringResource(R.string.dashboard_tools_section_filter_category_any),
                supportingText = stringResource(R.string.dashboard_tools_section_filter_available_tools_all),
                onClick = {
                    eventSink(FilterMenu.Event.SelectItem(null))
                    expanded = false
                }
            )
            categories.forEach { (category, count) ->
                FilterMenuItem(
                    label = getToolCategoryName(category, LocalContext.current),
                    supportingText = pluralStringResource(
                        R.plurals.dashboard_tools_section_filter_available_tools,
                        count,
                        count,
                    ),
                    onClick = {
                        eventSink(FilterMenu.Event.SelectItem(category))
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
internal fun LanguageFilter(state: FilterMenu.UiState<Language>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val languages by rememberUpdatedState(state.items)
    val selectedLanguage by rememberUpdatedState(state.selectedItem)
    val eventSink by rememberUpdatedState(state.eventSink)

    var expanded by state.menuExpanded
    var query by state.query

    ElevatedButton(
        onClick = { expanded = !expanded },
        modifier = modifier.semantics { role = Role.DropdownList }
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
            modifier = Modifier.sizeIn(maxHeight = DROPDOWN_MAX_HEIGHT, maxWidth = DROPDOWN_MAX_WIDTH)
        ) {
            stickyHeader {
                Surface(color = MenuDefaults.containerColor) {
                    SearchBar(
                        query,
                        onQueryChange = { query = it },
                        onSearch = { query = it },
                        active = false,
                        onActiveChange = {},
                        colors = GodToolsTheme.searchBarColors,
                        leadingIcon = { Icon(Icons.Filled.Search, null) },
                        placeholder = {
                            Text(stringResource(R.string.language_settings_downloadable_languages_search))
                        },
                        content = {},
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth()
                            .wrapContentWidth()
                    )
                }
            }
            item {
                FilterMenuItem(
                    label = stringResource(R.string.dashboard_tools_section_filter_language_any),
                    supportingText = stringResource(R.string.dashboard_tools_section_filter_available_tools_all),
                    onClick = {
                        eventSink(FilterMenu.Event.SelectItem(null))
                        expanded = false
                    }
                )
            }

            items(languages, key = { (it) -> it.code }) { (it, count) ->
                HorizontalDivider(Modifier.padding(start = ListItemStartPadding, end = ListItemEndPadding))
                FilterMenuItem(
                    label = { LanguageName(it) },
                    supportingText = pluralStringResource(
                        R.plurals.dashboard_tools_section_filter_available_tools,
                        count,
                        count,
                    ),
                    onClick = {
                        eventSink(FilterMenu.Event.SelectItem(it))
                        expanded = false
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

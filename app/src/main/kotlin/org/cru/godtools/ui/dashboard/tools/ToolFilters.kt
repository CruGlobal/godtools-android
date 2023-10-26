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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.compose.material3.ui.menu.LazyDropdownMenu
import org.cru.godtools.R
import org.cru.godtools.base.LocalAppLanguage
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.base.ui.util.getToolCategoryName
import org.cru.godtools.ui.languages.LanguageName

private val POPUP_MAX_HEIGHT = 600.dp
private val POPUP_MAX_WIDTH = 300.dp

@Composable
internal fun ToolFilters(viewModel: ToolsViewModel, modifier: Modifier = Modifier) = Column(modifier.fillMaxWidth()) {
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
        CategoryFilter(viewModel, modifier = Modifier.weight(1f))
        LanguageFilter(viewModel, modifier = Modifier.weight(1f))
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CategoryFilter(viewModel: ToolsViewModel, modifier: Modifier = Modifier) {
    val categories by viewModel.categories.collectAsState()
    var expanded by rememberSaveable { mutableStateOf(false) }

    ElevatedButton(
        onClick = { expanded = !expanded },
        modifier = modifier
    ) {
        val category by viewModel.selectedCategory.collectAsState()
        Text(
            category?.let { getToolCategoryName(it, LocalContext.current) }
                ?: stringResource(R.string.dashboard_tools_section_filter_category_any),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = POPUP_MAX_HEIGHT),
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.dashboard_tools_section_filter_category_any)) },
                onClick = {
                    viewModel.setSelectedCategory(null)
                    expanded = false
                }
            )
            categories.forEach {
                DropdownMenuItem(
                    text = { Text(getToolCategoryName(it, LocalContext.current)) },
                    onClick = {
                        viewModel.setSelectedCategory(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
private fun LanguageFilter(viewModel: ToolsViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var expanded by rememberSaveable { mutableStateOf(false) }

    ElevatedButton(
        onClick = {
            if (!expanded) viewModel.setLanguageQuery("")
            expanded = !expanded
        },
        modifier = modifier
    ) {
        val language by viewModel.selectedLanguage.collectAsState()
        Text(
            text = language?.getDisplayName(context, LocalAppLanguage.current)
                ?: stringResource(R.string.dashboard_tools_section_filter_language_any),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)

        val query by viewModel.languageQuery.collectAsState()
        val languages by viewModel.languages.collectAsState()
        LazyDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.sizeIn(maxHeight = POPUP_MAX_HEIGHT, maxWidth = POPUP_MAX_WIDTH),
        ) {
            item {
                SearchBar(
                    query,
                    onQueryChange = { viewModel.setLanguageQuery(it) },
                    onSearch = { viewModel.setLanguageQuery(it) },
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
                        viewModel.setSelectedLanguage(null)
                        expanded = false
                    }
                )
            }

            items(languages, key = { it.code }) {
                DropdownMenuItem(
                    text = { LanguageName(it) },
                    onClick = {
                        viewModel.setSelectedLanguage(it)
                        expanded = false
                    },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

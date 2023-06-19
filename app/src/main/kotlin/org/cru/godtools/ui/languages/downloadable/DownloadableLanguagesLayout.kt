package org.cru.godtools.ui.languages.downloadable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.ui.languages.LanguageName

sealed interface DownloadableLanguagesEvent {
    object NavigateBack : DownloadableLanguagesEvent
}

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
fun DownloadableLanguagesLayout(
    viewModel: DownloadableLanguagesViewModel = viewModel(),
    languageViewModels: LanguageViewModels = viewModel(),
    onEvent: (DownloadableLanguagesEvent) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val updateSearchQuery: (String) -> Unit = remember {
        {
            viewModel.updateSearchQuery(it)
            coroutineScope.launch { lazyListState.animateScrollToItem(0) }
        }
    }

    Scaffold(
        topBar = {
            SearchBar(
                query = searchQuery,
                onQueryChange = updateSearchQuery,
                onSearch = updateSearchQuery,
                active = false,
                onActiveChange = {},
                colors = when {
                    GodToolsTheme.isLightColorSchemeActive -> SearchBarDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    else -> SearchBarDefaults.colors()
                },
                leadingIcon = {
                    IconButton(
                        onClick = {
                            when {
                                searchQuery.isNotEmpty() -> updateSearchQuery("")
                                else -> onEvent(DownloadableLanguagesEvent.NavigateBack)
                            }
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                placeholder = { Text(stringResource(R.string.language_settings_downloadable_languages_search)) },
                content = {},
                modifier = Modifier
                    .padding(horizontal = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
    ) { contentPadding ->
        val languages by viewModel.languages.collectAsState(emptyList())

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.padding(contentPadding)
        ) {
            itemsIndexed(languages, key = { _, it -> it.code }) { i, it ->
                LanguageListItem(languageViewModels.get(it), Modifier.animateItemPlacement())
                if (i + 1 < languages.size) Divider()
            }
        }
    }
}

@Composable
private fun LanguageListItem(
    viewModel: LanguageViewModels.LanguageViewModel,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val language by viewModel.language.collectAsState()
    val toolsAvailable by viewModel.numberOfTools.collectAsState()

    ListItem(
        headlineContent = { LanguageName(language) },
        supportingContent = {
            val tools = toolsAvailable
            Text(pluralStringResource(R.plurals.language_settings_downloadable_languages_available_tools, tools, tools))
        },
        trailingContent = {
            Switch(language.isAdded, onCheckedChange = {
                scope.launch(NonCancellable) {
                    if (it) viewModel.pin() else viewModel.unpin()
                }
            })
        },
        modifier = modifier
    )
}

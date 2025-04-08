package org.cru.godtools.ui.languages.downloadable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.ui.languages.LanguageName
import org.cru.godtools.ui.languages.downloadable.DownloadableLanguagesScreen.UiState

internal const val TEST_TAG_NAVIGATE_UP = "navigateUp"
internal const val TEST_TAG_CANCEL_SEARCH = "cancelSearch"

@Composable
@CircuitInject(DownloadableLanguagesScreen::class, SingletonComponent::class)
@OptIn(ExperimentalMaterial3Api::class)
fun DownloadableLanguagesLayout(state: UiState, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    var searchQuery by state.query
    val updateSearchQuery: (String) -> Unit = remember {
        {
            searchQuery = it
            coroutineScope.launch { lazyListState.animateScrollToItem(0) }
        }
    }

    BackHandler(enabled = searchQuery.isNotEmpty()) { updateSearchQuery("") }

    Scaffold(
        topBar = {
            SearchBar(
                query = searchQuery,
                onQueryChange = updateSearchQuery,
                onSearch = updateSearchQuery,
                active = false,
                onActiveChange = {},
                colors = GodToolsTheme.searchBarColors,
                leadingIcon = {
                    IconButton(
                        onClick = { state.eventSink(UiState.UiEvent.NavigateUp) },
                        modifier = Modifier.testTag(TEST_TAG_NAVIGATE_UP),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                trailingIcon = searchQuery.takeIf { it.isNotEmpty() }?.let {
                    {
                        IconButton(
                            onClick = { updateSearchQuery("") },
                            modifier = Modifier.testTag(TEST_TAG_CANCEL_SEARCH),
                        ) {
                            Icon(Icons.Filled.Close, null)
                        }
                    }
                },
                placeholder = { Text(stringResource(R.string.language_settings_downloadable_languages_search)) },
                content = {},
                modifier = Modifier
                    .padding(horizontal = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        },
        modifier = modifier
    ) { contentPadding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.padding(contentPadding)
        ) {
            itemsIndexed(state.languages, key = { _, it -> it.language.code }) { i, it ->
                LanguageListItem(it, Modifier.animateItem())
                if (i < state.languages.lastIndex) HorizontalDivider(Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun LanguageListItem(state: UiState.UiLanguage, modifier: Modifier = Modifier) = ListItem(
    headlineContent = { LanguageName(state.language) },
    supportingContent = {
        Text(
            pluralStringResource(
                R.plurals.language_settings_downloadable_languages_available_tools,
                state.totalTools,
                state.totalTools
            )
        )
    },
    trailingContent = {
        var confirmRemoval by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(confirmRemoval) {
            delay(3_000)
            confirmRemoval = false
        }

        LanguageDownloadStatusIndicator(
            isPinned = state.language.isAdded,
            downloadedTools = state.downloadedTools,
            totalTools = state.totalTools,
            isConfirmRemoval = confirmRemoval,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false),
                ) {
                    when {
                        !state.language.isAdded -> state.eventSink(UiState.UiLanguage.UiEvent.PinLanguage)
                        !confirmRemoval -> confirmRemoval = true
                        else -> state.eventSink(UiState.UiLanguage.UiEvent.UnpinLanguage)
                    }
                }
                .padding(8.dp)
        )
    },
    modifier = modifier
)

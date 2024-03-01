package org.cru.godtools.ui.languages.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.ccci.gto.android.common.androidx.compose.ui.text.res.annotatedStringResource
import org.ccci.gto.android.common.util.content.localize
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.ui.languages.LanguageName
import org.cru.godtools.ui.languages.app.AppLanguageScreen.Event

internal const val TEST_TAG_ACTION_BACK = "action_navigate_back"
internal const val TEST_TAG_CANCEL_SEARCH = "action_cancel_search"

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(AppLanguageScreen::class, SingletonComponent::class)
internal fun AppLanguageLayout(state: AppLanguageScreen.State, modifier: Modifier = Modifier) {
    val eventSink by rememberUpdatedState(state.eventSink)
    val languageQuery by rememberUpdatedState(state.languageQuery)

    Scaffold(
        topBar = {
            SearchBar(
                query = state.languageQuery,
                onQueryChange = { eventSink(Event.UpdateLanguageQuery(it)) },
                onSearch = { eventSink(Event.UpdateLanguageQuery(it)) },
                active = false,
                onActiveChange = {},
                colors = GodToolsTheme.searchBarColors,
                leadingIcon = {
                    IconButton(
                        onClick = { eventSink(Event.NavigateBack) },
                        modifier = Modifier.testTag(TEST_TAG_ACTION_BACK),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                trailingIcon = {
                    if (languageQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { eventSink(Event.UpdateLanguageQuery("")) },
                            modifier = Modifier.testTag(TEST_TAG_CANCEL_SEARCH),
                        ) {
                            Icon(Icons.Filled.Close, null)
                        }
                    }
                },
                placeholder = { Text(stringResource(R.string.language_settings_app_language_title)) },
                content = {},
                modifier = Modifier
                    .padding(horizontal = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .background(ListItemDefaults.containerColor)
        ) {
            itemsIndexed(state.languages, { _, it -> it }) { i, lang ->
                if (i > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))

                ListItem(
                    headlineContent = { LanguageName(lang) },
                    modifier = Modifier.clickable { eventSink(Event.SelectLanguage(lang)) }
                )
            }
        }
    }

    ConfirmAppLanguageDialog(state)
}

@Composable
private fun ConfirmAppLanguageDialog(state: AppLanguageScreen.State) {
    val language = state.selectedLanguage
    val eventSink by rememberUpdatedState(state.eventSink)

    if (language != null) {
        AlertDialog(
            iconContentColor = MaterialTheme.colorScheme.primary,
            icon = { Icon(Icons.Outlined.Translate, null) },
            text = {
                Column {
                    ConfirmAppLanguageMessage(language.displayName)

                    val context = LocalContext.current
                    val localizedContext = remember(context, language) { context.localize(language) }
                    CompositionLocalProvider(LocalContext provides localizedContext) {
                        ConfirmAppLanguageMessage(
                            language.getDisplayName(language),
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { eventSink(Event.ConfirmLanguage(language)) }
                ) { Text(stringResource(R.string.language_settings_app_language_dialog_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { eventSink(Event.DismissConfirmDialog) }) {
                    Text(stringResource(R.string.language_settings_app_language_dialog_dismiss))
                }
            },
            onDismissRequest = { eventSink(Event.DismissConfirmDialog) },
        )
    }
}

@Composable
private fun ConfirmAppLanguageMessage(displayName: String, modifier: Modifier = Modifier) = Text(
    annotatedStringResource(
        R.string.language_settings_app_language_dialog_message,
        buildAnnotatedString {
            withStyle(SpanStyle(color = GodToolsTheme.GT_BLUE)) {
                append(displayName)
            }
        }
    ),
    modifier = modifier,
)

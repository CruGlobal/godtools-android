package org.cru.godtools.ui.languages.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import org.ccci.gto.android.common.androidx.compose.ui.draw.autoMirror
import org.ccci.gto.android.common.androidx.compose.ui.text.res.annotatedStringResource
import org.ccci.gto.android.common.util.content.localize
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.ui.languages.LanguageName

internal const val TEST_TAG_ACTION_BACK = "action_navigate_back"

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(AppLanguageScreen::class, SingletonComponent::class)
internal fun AppLanguageLayout(state: AppLanguageScreen.State, modifier: Modifier = Modifier) {
    val eventSink by rememberUpdatedState(state.eventSink)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { eventSink(AppLanguageScreen.Event.NavigateBack) },
                        modifier = Modifier.testTag(TEST_TAG_ACTION_BACK)
                    ) {
                        Icon(Icons.Filled.ArrowBack, null, Modifier.autoMirror())
                    }
                },
                title = { Text(stringResource(R.string.language_settings_app_language_title)) },
                colors = GodToolsTheme.topAppBarColors,
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
                if (i > 0) Divider(Modifier.padding(horizontal = 16.dp))

                ListItem(
                    headlineContent = { LanguageName(lang) },
                    modifier = Modifier.clickable { eventSink(AppLanguageScreen.Event.SelectLanguage(lang)) }
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
                    onClick = { eventSink(AppLanguageScreen.Event.ConfirmLanguage(language)) }
                ) { Text(stringResource(R.string.language_settings_app_language_dialog_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { eventSink(AppLanguageScreen.Event.DismissConfirmDialog) }) {
                    Text(stringResource(R.string.language_settings_app_language_dialog_dismiss))
                }
            },
            onDismissRequest = { eventSink(AppLanguageScreen.Event.DismissConfirmDialog) },
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

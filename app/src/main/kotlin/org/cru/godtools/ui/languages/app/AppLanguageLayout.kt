package org.cru.godtools.ui.languages.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import org.ccci.gto.android.common.androidx.compose.ui.text.res.annotatedStringResource
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsAppBarColors
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.ui.languages.LanguageName

internal sealed interface AppLanguageEvent {
    object NavigateBack : AppLanguageEvent
    class LanguageSelected(val language: Locale) : AppLanguageEvent
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun AppLanguageLayout(
    viewModel: AppLanguageViewModel = viewModel(),
    onEvent: (AppLanguageEvent) -> Unit = {},
) {
    val languages by viewModel.languages.collectAsState(emptyList())
    var confirmLanguage by rememberSaveable { mutableStateOf<Locale?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onEvent(AppLanguageEvent.NavigateBack) }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                title = { Text(stringResource(R.string.language_settings_app_language_title)) },
                colors = GodToolsAppBarColors,
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .background(ListItemDefaults.containerColor)
        ) {
            itemsIndexed(languages, { _, it -> it }) { i, lang ->
                ListItem(
                    headlineContent = { LanguageName(lang) },
                    modifier = Modifier.clickable { confirmLanguage = lang }
                )
                if (i != languages.lastIndex) Divider(Modifier.padding(horizontal = 16.dp))
            }
        }
    }

    ConfirmAppLanguageDialog(
        language = confirmLanguage,
        onEvent = {
            @Suppress("LiftReturnOrAssignment")
            when (it) {
                is AppLanguageDialogEvent.Confirm -> {
                    onEvent(AppLanguageEvent.LanguageSelected(it.language))
                    confirmLanguage = null
                }
                AppLanguageDialogEvent.Dismiss -> confirmLanguage = null
            }
        }
    )
}

private sealed interface AppLanguageDialogEvent {
    object Dismiss : AppLanguageDialogEvent
    class Confirm(val language: Locale) : AppLanguageDialogEvent
}

@Composable
private fun ConfirmAppLanguageDialog(language: Locale?, onEvent: (AppLanguageDialogEvent) -> Unit) {
    if (language != null) {
        AlertDialog(
            iconContentColor = MaterialTheme.colorScheme.primary,
            icon = { Icon(Icons.Outlined.Translate, null) },
            text = {
                Text(
                    annotatedStringResource(
                        R.string.language_settings_app_language_dialog_message,
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = GodToolsTheme.GT_BLUE)) {
                                append(language.displayName)
                            }
                        }
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(AppLanguageDialogEvent.Confirm(language)) }
                ) { Text(stringResource(R.string.language_settings_app_language_dialog_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(AppLanguageDialogEvent.Dismiss) }) {
                    Text(stringResource(R.string.language_settings_app_language_dialog_dismiss))
                }
            },
            onDismissRequest = { onEvent(AppLanguageDialogEvent.Dismiss) },
        )
    }
}

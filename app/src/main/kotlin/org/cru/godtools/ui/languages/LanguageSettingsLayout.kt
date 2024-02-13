package org.cru.godtools.ui.languages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.cru.godtools.R
import org.cru.godtools.analytics.compose.RecordAnalyticsScreen
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.shared.analytics.AnalyticsScreenNames

internal sealed interface LanguageSettingsEvent {
    data object NavigateUp : LanguageSettingsEvent
    data object AppLanguage : LanguageSettingsEvent
    data object DownloadableLanguages : LanguageSettingsEvent
}

private const val SECTION_APP_LANGUAGE = "app_language"
private const val SECTION_OFFLINE_LANGUAGES_TOP = "offline_languages_top"
private const val SECTION_OFFLINE_LANGUAGES_BOTTOM = "offline_languages_bottom"

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
internal fun LanguageSettingsLayout(
    viewModel: LanguageSettingsViewModel = viewModel(),
    onEvent: (LanguageSettingsEvent) -> Unit = {},
) {
    RecordAnalyticsScreen(AnalyticsScreenEvent(AnalyticsScreenNames.SETTINGS_LANGUAGES))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_language_settings)) },
                colors = GodToolsTheme.topAppBarColors,
                navigationIcon = {
                    IconButton(onClick = { onEvent(LanguageSettingsEvent.NavigateUp) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
            )
        }
    ) {
        val pinnedLanguages by viewModel.pinnedLanguages.collectAsState()

        LazyColumn(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 32.dp)
                .fillMaxHeight()
        ) {
            // App interface language
            item(SECTION_APP_LANGUAGE) {
                val appLanguage by viewModel.appLanguage.collectAsState()
                val appLanguages by viewModel.appLanguages.collectAsState()

                Text(
                    stringResource(R.string.language_settings_section_app_language_heading),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 32.dp),
                )
                Text(
                    pluralStringResource(
                        R.plurals.language_settings_section_app_language_available,
                        appLanguages,
                        appLanguages
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    stringResource(R.string.language_settings_section_app_language_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
                FilledTonalButton(
                    onClick = { onEvent(LanguageSettingsEvent.AppLanguage) },
                    colors = when {
                        GodToolsTheme.isLightColorSchemeActive -> ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        else -> ButtonDefaults.filledTonalButtonColors()
                    },
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                ) {
                    Icon(
                        Icons.Outlined.Translate,
                        null,
                        tint = when {
                            GodToolsTheme.isLightColorSchemeActive -> MaterialTheme.colorScheme.primary
                            else -> LocalContentColor.current
                        },
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(12.dp)
                    )
                    Text(appLanguage.getDisplayName(appLanguage))
                    Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(24.dp))
                }
            }

            // Offline Languages
            item(SECTION_OFFLINE_LANGUAGES_TOP) {
                Text(
                    stringResource(R.string.language_settings_section_offline_heading),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 32.dp),
                )
                Text(
                    stringResource(R.string.language_settings_section_offline_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                )
            }

            itemsIndexed(pinnedLanguages.orEmpty(), key = { _, it -> it.code }) { i, it ->
                if (i == 0) Divider(modifier = Modifier.animateItemPlacement())
                LanguageName(
                    it,
                    modifier = Modifier
                        .animateItemPlacement()
                        .heightIn(min = 56.dp)
                        .padding(vertical = 4.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
                Divider(modifier = Modifier.animateItemPlacement())
            }
            item(SECTION_OFFLINE_LANGUAGES_BOTTOM) {
                if (pinnedLanguages != null) {
                    Button(
                        onClick = { onEvent(LanguageSettingsEvent.DownloadableLanguages) },
                        modifier = Modifier
                            .animateItemPlacement()
                            .padding(top = 24.dp)
                            .fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.language_settings_section_offline_action_edit))
                    }
                }
            }
        }
    }
}

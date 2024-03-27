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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import org.cru.godtools.R
import org.cru.godtools.analytics.compose.RecordAnalyticsScreen
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.shared.analytics.AnalyticsScreenNames
import org.cru.godtools.ui.drawer.DrawerMenuLayout
import org.cru.godtools.ui.languages.LanguageSettingsScreen.Event
import org.cru.godtools.ui.languages.LanguageSettingsScreen.State

internal sealed interface LanguageSettingsEvent {
    data object NavigateUp : LanguageSettingsEvent
    data object AppLanguage : LanguageSettingsEvent
    data object DownloadableLanguages : LanguageSettingsEvent
}

internal const val TEST_TAG_ACTION_BACK = "action_navigate_back"

private const val SECTION_APP_LANGUAGE = "app_language"
private const val SECTION_OFFLINE_LANGUAGES_TOP = "offline_languages_top"
private const val SECTION_OFFLINE_LANGUAGES_BOTTOM = "offline_languages_bottom"

@Composable
internal fun LanguageSettingsLayout(
    viewModel: LanguageSettingsViewModel = viewModel(),
    onEvent: (LanguageSettingsEvent) -> Unit = {},
) {
    val state = State(
        appLanguage = viewModel.appLanguage.collectAsState().value,
        appLanguages = viewModel.appLanguages.collectAsState().value,
        downloadedLanguages = viewModel.pinnedLanguages.collectAsState().value,
        eventSink = {
            when (it) {
                Event.NavigateUp -> onEvent(LanguageSettingsEvent.NavigateUp)
                Event.AppLanguage -> onEvent(LanguageSettingsEvent.AppLanguage)
                Event.DownloadableLanguages -> onEvent(LanguageSettingsEvent.DownloadableLanguages)
            }
        }
    )

    LanguageSettingsLayout(state)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@CircuitInject(LanguageSettingsScreen::class, SingletonComponent::class)
internal fun LanguageSettingsLayout(state: State, modifier: Modifier = Modifier) {
    RecordAnalyticsScreen(AnalyticsScreenEvent(AnalyticsScreenNames.SETTINGS_LANGUAGES))

    val appLanguage by rememberUpdatedState(state.appLanguage)
    val appLanguages by rememberUpdatedState(state.appLanguages)
    val eventSink by rememberUpdatedState(state.eventSink)
    val pinnedLanguages by rememberUpdatedState(state.downloadedLanguages)

    DrawerMenuLayout {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.title_language_settings)) },
                    colors = GodToolsTheme.topAppBarColors,
                    navigationIcon = {
                        IconButton(
                            onClick = { eventSink(Event.NavigateUp) },
                            modifier = Modifier.testTag(TEST_TAG_ACTION_BACK)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    },
                )
            },
            modifier = modifier,
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(it)
                    .padding(horizontal = 32.dp)
                    .fillMaxHeight()
            ) {
                // App interface language
                item(SECTION_APP_LANGUAGE) {
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
                        onClick = { eventSink(Event.AppLanguage) },
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
                        Text(remember { derivedStateOf { appLanguage.getDisplayName(appLanguage) } }.value)
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

                itemsIndexed(pinnedLanguages, key = { _, it -> it.code }) { i, it ->
                    if (i == 0) HorizontalDivider(modifier = Modifier.animateItemPlacement())
                    LanguageName(
                        it,
                        modifier = Modifier
                            .animateItemPlacement()
                            .heightIn(min = 56.dp)
                            .padding(vertical = 4.dp)
                            .wrapContentHeight(Alignment.CenterVertically)
                    )
                    HorizontalDivider(modifier = Modifier.animateItemPlacement())
                }
                item(SECTION_OFFLINE_LANGUAGES_BOTTOM) {
                    Button(
                        onClick = { eventSink(Event.DownloadableLanguages) },
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

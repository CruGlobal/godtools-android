package org.cru.godtools.ui.languages.localization

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.ui.languages.localization.LocalizationSettingsPresenter.CountryItem
import org.cru.godtools.ui.languages.localization.LocalizationSettingsPresenter.UiEvent
import org.cru.godtools.ui.languages.localization.LocalizationSettingsPresenter.UiState

internal const val TEST_TAG_ACTION_BACK = "action_navigate_back"
internal const val TEST_TAG_CANCEL_SEARCH = "action_cancel_search"

@Composable
private fun CountryName(country: CountryItem, modifier: Modifier = Modifier) {
    val color = LocalTextStyle.current.color.takeOrElse { LocalContentColor.current }
    Text(
        remember(country, color) {
            buildAnnotatedString {
                withStyle(SpanStyle(color = color)) { append(country.nativeName) }
                withStyle(SpanStyle(color = color.copy(alpha = color.alpha * 0.60f))) {
                    append("  ${country.displayName}")
                }
            }
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(LocalizationSettingsScreen::class, SingletonComponent::class)
internal fun LocalizationSettingsLayout(state: UiState, modifier: Modifier = Modifier) {
    val eventSink by rememberUpdatedState(state.eventSink)
    var query by state.query

    Scaffold(
        topBar = {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = { query = it },
                        expanded = false,
                        onExpandedChange = {},
                        leadingIcon = {
                            IconButton(
                                onClick = { eventSink(UiEvent.NavigateBack) },
                                modifier = Modifier.testTag(TEST_TAG_ACTION_BACK),
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(
                                    onClick = { query = "" },
                                    modifier = Modifier.testTag(TEST_TAG_CANCEL_SEARCH),
                                ) {
                                    Icon(Icons.Filled.Close, null)
                                }
                            } else {
                                Icon(Icons.Filled.Search, null)
                            }
                        },
                        placeholder = { Text(stringResource(R.string.localization_settings_title)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                expanded = false,
                onExpandedChange = {},
                colors = GodToolsTheme.searchBarColors,
                content = {},
                modifier = Modifier
                    .padding(horizontal = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally),
            )
        },
        modifier = modifier,
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .background(ListItemDefaults.containerColor),
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(32.dp),
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.scrim) {
                        Column {
                            Text(
                                text = stringResource(R.string.localization_settings_subtitle_header),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = stringResource(R.string.localization_settings_subtitle_body),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            itemsIndexed(state.countries, { _, it -> it.isoCode }) { i, country ->
                if (i > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))

                ListItem(
                    headlineContent = { CountryName(country) },
                    trailingContent = {
                        if (country.isoCode == state.localizationCountryCode) {
                            Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.clickable { eventSink(UiEvent.SelectCountry(country.isoCode)) },
                )
            }
        }
    }
}

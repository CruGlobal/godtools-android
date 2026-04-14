package org.cru.godtools.ui.languages.country

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.LocalOverlayHost
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.ui.languages.LocalizedName
import org.cru.godtools.ui.languages.country.CountrySettingsPresenter.UiEvent
import org.cru.godtools.ui.languages.country.CountrySettingsPresenter.UiState

internal const val TEST_TAG_ACTION_BACK = "action_navigate_back"
internal const val TEST_TAG_CANCEL_SEARCH = "action_cancel_search"

@Composable
private fun CountryName(country: CountrySettingsPresenter.CountryItem, modifier: Modifier = Modifier) {
    LocalizedName(name = country.nativeName, secondName = country.displayName, modifier = modifier)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(CountrySettingsScreen::class, SingletonComponent::class)
internal fun CountrySettingsLayout(state: UiState, modifier: Modifier = Modifier) {
    val eventSink by rememberUpdatedState(state.eventSink)
    val overlayHost = LocalOverlayHost.current
    val coroutineScope = rememberCoroutineScope()
    var query by state.query
    val selectedCountry = state.countries.firstOrNull {
        it.isoCode == state.countryCode
    }

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
                        placeholder = { Text(stringResource(R.string.country_settings_title)) },
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
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.country_settings_subtitle_header),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(R.string.country_settings_subtitle_body),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            item {
                ListItem(
                    headlineContent = {
                        Text(text = stringResource(R.string.country_settings_option_none))
                    },
                    trailingContent = {
                        if (state.countryCode == null) {
                            Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.clickable {
                        eventSink(UiEvent.SelectCountry(null))
                    },
                )
            }
            if (selectedCountry != null) {
                item {
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = {
                            CountryName(selectedCountry)
                        },
                        trailingContent = {
                            Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.clickable {
                            eventSink(UiEvent.NavigateBack)
                        },
                    )
                }
            }
            items(state.countries, { it.isoCode }) { country ->
                if (state.countryCode != country.isoCode) {
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { CountryName(country) },
                        trailingContent = {
                            if (country.isoCode == state.countryCode) {
                                Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.clickable {
                            coroutineScope.launch {
                                val confirmCountrySelection = overlayHost.show(
                                    CountrySettingsConfirmationOverlay(
                                        country.displayName ?: country.isoCode,
                                    ),
                                )
                                if (confirmCountrySelection) {
                                    eventSink(UiEvent.SelectCountry(country.isoCode))
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

package org.cru.godtools.ui.languages.localization

import android.content.Context
import android.icu.text.LocaleDisplayNames
import android.icu.util.ULocale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.cru.godtools.base.Settings
import org.cru.godtools.base.util.getPrimaryCollator
import org.cru.godtools.ui.languages.localization.LocalizationSettingsPresenter.UiState

class LocalizationSettingsPresenter @AssistedInject constructor(
    @param:ApplicationContext private val context: Context,
    private val settings: Settings,
    @Assisted private val navigator: Navigator,
) : Presenter<UiState> {
    // what the screen needs to know
    data class UiState(
        val countries: ImmutableList<CountryItem> = persistentListOf(),
        val query: MutableState<String> = mutableStateOf(""),
        val localizationCountryCode: String? = null,
        val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState

    data class CountryItem(val isoCode: String, val displayName: String, val nativeName: String)

    sealed interface UiEvent : CircuitUiEvent {
        data object NavigateBack : UiEvent
        data class SelectCountry(val isoCode: String) : UiEvent
    }

    @Composable
    override fun present(): UiState {
        val scope = rememberCoroutineScope()
        val appLocale by settings.produceAppLocaleState()
        val query = rememberSaveable { mutableStateOf("") }
        val localizationCountryCode by settings.getLocalizationCountryFlow().collectAsState(initial = null)
        val sortedCountries = rememberSortedCountries(appLocale)
        val filteredCountries = remember(sortedCountries, query.value) {
            sortedCountries.filter { country ->
                query.value.isEmpty() ||
                    country.displayName.contains(query.value, ignoreCase = true) ||
                    country.nativeName.contains(query.value, ignoreCase = true)
            }.toImmutableList()
        }

        return UiState(
            countries = filteredCountries,
            query = query,
            localizationCountryCode = localizationCountryCode,
            eventSink = { event ->
                when (event) {
                    UiEvent.NavigateBack -> navigator.pop()

                    is UiEvent.SelectCountry -> {
                        scope.launch {
                            settings.updateLocalizationCountry(isoCode = event.isoCode)
                        }
                        navigator.pop()
                    }
                }
            }
        )
    }

    @Composable
    private fun rememberSortedCountries(appLocale: Locale): ImmutableList<CountryItem> {
        val isoCodes = remember { Locale.getISOCountries() }

        return remember(appLocale) {
            val appDisplayNames = LocaleDisplayNames.getInstance(ULocale.forLocale(appLocale))
            isoCodes.map { isoCode ->
                val displayName = appDisplayNames.regionDisplayName(isoCode)
                val resourceId = context.resources.getIdentifier(
                    "country_native_name_$isoCode",
                    "string",
                    context.packageName,
                )
                val nativeName = if (resourceId != 0) context.getString(resourceId) else displayName
                CountryItem(isoCode, displayName, nativeName)
            }.sortedWith(
                compareBy(appLocale.getPrimaryCollator()) { it.displayName }
            ).toImmutableList()
        }
    }

    @AssistedFactory
    @CircuitInject(LocalizationSettingsScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): LocalizationSettingsPresenter
    }
}

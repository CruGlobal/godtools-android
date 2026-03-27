package org.cru.godtools.ui.languages.country

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import org.cru.godtools.base.ui.BasePaparazziTest
import org.cru.godtools.ui.languages.country.CountrySettingsPresenter.CountryItem
import org.cru.godtools.ui.languages.country.CountrySettingsPresenter.UiState
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class CountrySettingsLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    private val countries = persistentListOf(
        CountryItem("BR", "Brazil", "Brasil"),
        CountryItem("EC", "Ecuador", "Ecuador"),
        CountryItem("FR", "France", "France"),
        CountryItem("DE", "Germany", "Deutschland"),
        CountryItem("IN", "India", "भारत"),
        CountryItem("JP", "Japan", "日本"),
        CountryItem("MX", "Mexico", "México"),
        CountryItem("ES", "Spain", "España"),
        CountryItem("GB", "United Kingdom", "United Kingdom"),
        CountryItem("US", "United States", "United States"),
    )
    private val state = UiState(countries = countries)

    @Test
    fun `CountrySettingsLayout()`() {
        snapshot { CountrySettingsLayout(state) }
    }

    @Test
    fun `CountrySettingsLayout() - Selected Country`() {
        snapshot { CountrySettingsLayout(state.copy(countryCode = "EC")) }
    }

    @Test
    fun `CountrySettingsLayout() - Searching`() {
        snapshot {
            CountrySettingsLayout(
                state.copy(
                    query = remember { mutableStateOf("india") },
                    countries = persistentListOf(
                        CountryItem("IN", "India", "भारत"),
                    ),
                )
            )
        }
    }
}

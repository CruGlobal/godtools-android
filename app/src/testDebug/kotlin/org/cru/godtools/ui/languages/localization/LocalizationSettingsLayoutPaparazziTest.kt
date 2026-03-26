package org.cru.godtools.ui.languages.localization

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import org.cru.godtools.base.ui.BasePaparazziTest
import org.cru.godtools.ui.languages.localization.LocalizationSettingsPresenter.CountryItem
import org.cru.godtools.ui.languages.localization.LocalizationSettingsPresenter.UiState
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class LocalizationSettingsLayoutPaparazziTest(
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
    fun `LocalizationSettingsLayout()`() {
        snapshot { LocalizationSettingsLayout(state) }
    }

    @Test
    fun `LocalizationSettingsLayout() - Selected Country`() {
        snapshot { LocalizationSettingsLayout(state.copy(localizationCountryCode = "EC")) }
    }

    @Test
    fun `LocalizationSettingsLayout() - Searching`() {
        snapshot {
            LocalizationSettingsLayout(
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

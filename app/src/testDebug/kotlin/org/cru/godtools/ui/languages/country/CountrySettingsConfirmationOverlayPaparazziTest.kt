package org.cru.godtools.ui.languages.country

import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.slack.circuit.overlay.OverlayEffect
import kotlin.test.Test
import org.cru.godtools.base.ui.BasePaparazziTest
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class CountrySettingsConfirmationOverlayPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    enum class CountryNameVariants(val countryName: String) {
        VIETNAM("Vietnam"),
        FRANCE("France"),
        UAE("United Arab Emirates"),
        USA("United States of America"),
        BOSNIA("Bosnia and Herzegovina"),
        CUBA("Cuba"),
        CHAD("Chad"),
    }

    @Test
    fun `CountrySettingsConfirmationOverlay()`(
        @TestParameter countryVariant: CountryNameVariants,
    ) {
        snapshot {
            OverlayEffect(CountrySettingsConfirmationOverlay(countryVariant.countryName)) { }
        }
    }
}

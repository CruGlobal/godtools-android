package org.cru.godtools.ui.languages

import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.util.Locale
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import org.cru.godtools.model.Language
import org.cru.godtools.ui.BasePaparazziTest
import org.cru.godtools.ui.languages.LanguageSettingsScreen.State
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class LanguageSettingsLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    private val state = State(
        appLanguage = Locale.FRENCH,
        appLanguages = 1234,
        downloadedLanguages = persistentListOf(
            Language(Locale.ENGLISH),
            Language(Locale.FRENCH),
            Language(Locale.GERMAN),
            Language(Locale("es"))
        )
    )

    @Test
    fun `LanguageSettingsLayout()`() {
        snapshot { LanguageSettingsLayout(state) }
    }
}

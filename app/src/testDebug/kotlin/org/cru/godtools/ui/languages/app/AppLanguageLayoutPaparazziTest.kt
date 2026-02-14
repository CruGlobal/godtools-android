package org.cru.godtools.ui.languages.app

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.util.Locale
import kotlin.test.Ignore
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import org.cru.godtools.base.ui.BasePaparazziTest
import org.cru.godtools.ui.languages.app.AppLanguagePresenter.UiState
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class AppLanguageLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    private val state = UiState(
        languages = persistentListOf(Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN, Locale("es"))
    )

    @Test
    fun `AppLanguageLayout()`() {
        snapshot { AppLanguageLayout(state) }
    }

    @Test
    fun `AppLanguageLayout() - Searching`() {
        snapshot {
            AppLanguageLayout(
                state.copy(
                    languageQuery = remember { mutableStateOf("en") },
                    languages = persistentListOf(Locale.ENGLISH, Locale.FRENCH)
                )
            )
        }
    }

    @Test
    @Ignore("TODO: Disabled due to https://github.com/cashapp/paparazzi/issues/1025")
    fun `AppLanguageLayout() - Confirm Dialog`() {
        snapshot { AppLanguageLayout(state.copy(selectedLanguage = Locale.GERMAN)) }
    }
}

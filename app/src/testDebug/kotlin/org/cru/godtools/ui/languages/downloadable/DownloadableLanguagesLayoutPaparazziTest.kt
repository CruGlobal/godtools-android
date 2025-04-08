package org.cru.godtools.ui.languages.downloadable

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.util.Locale
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import org.cru.godtools.model.Language
import org.cru.godtools.ui.BasePaparazziTest
import org.cru.godtools.ui.languages.downloadable.DownloadableLanguagesScreen.UiState
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class DownloadableLanguagesLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    private val state = UiState(
        languages = persistentListOf(
            UiState.UiLanguage(
                Language(Locale.ENGLISH, isAdded = true),
                downloadedTools = 2,
                totalTools = 2
            ),
            UiState.UiLanguage(
                Language(Locale.FRENCH, isAdded = true),
                downloadedTools = 1,
                totalTools = 2
            ),
            UiState.UiLanguage(
                Language(Locale.GERMAN, isAdded = false),
                downloadedTools = 1,
                totalTools = 2
            ),
        )
    )

    @Test
    fun `DownloadableLanguagesLayout()`() {
        snapshot { DownloadableLanguagesLayout(state) }
    }

    @Test
    fun `DownloadableLanguagesLayout() - Searching`() {
        snapshot {
            DownloadableLanguagesLayout(
                state.copy(query = remember { mutableStateOf("en") }),
            )
        }
    }
}

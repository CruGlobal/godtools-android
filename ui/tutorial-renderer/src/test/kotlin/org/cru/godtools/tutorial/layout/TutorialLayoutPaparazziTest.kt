package org.cru.godtools.tutorial.layout

import androidx.compose.runtime.CompositionLocalProvider
import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.util.Locale
import kotlin.test.Test
import org.cru.godtools.base.LocalAppLanguage
import org.cru.godtools.base.ui.BasePaparazziTest
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.layout.TutorialPresenter.UiState
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class TutorialLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    @Test
    fun `TutorialLayout() - FEATURES`() = snapshot {
        CompositionLocalProvider(LocalAppLanguage provides Locale.ENGLISH) {
            TutorialLayout(UiState(PageSet.FEATURES))
        }
    }
}

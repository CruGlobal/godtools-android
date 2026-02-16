package org.cru.godtools.ui.account.delete

import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import org.cru.godtools.base.ui.BasePaparazziTest
import org.cru.godtools.ui.account.delete.DeleteAccountPresenter.UiState
import org.junit.Assume.assumeFalse
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class DeleteAccountLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    @Test
    fun `DeleteAccountLayout() - Display`() {
        snapshot { DeleteAccountLayout(UiState.Display()) }
    }

    @Test
    fun `DeleteAccountLayout() - Deleting`() {
        snapshot { DeleteAccountLayout(UiState.Deleting()) }
    }

    @Test
    fun `DeleteAccountLayout() - Error`() {
        // TODO: Accessibility Tests don't currently handle dialogs
        assumeFalse(accessibilityMode == AccessibilityMode.ACCESSIBILITY)

        snapshot { DeleteAccountLayout(UiState.Error()) }
    }
}

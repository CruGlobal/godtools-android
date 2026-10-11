package org.cru.godtools.ui.login

import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import org.cru.godtools.account.LoginResponse
import org.cru.godtools.base.ui.BasePaparazziTest
import org.cru.godtools.ui.login.LoginPresenter.UiState
import org.junit.Assume.assumeFalse
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class LoginLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    @Test
    fun `LoginLayout() - Login`() = snapshot { LoginLayout(UiState(createAccount = false)) }

    @Test
    fun `LoginLayout() - Create Account`() = snapshot { LoginLayout(UiState(createAccount = true)) }

    @Test
    fun `LoginLayout() - Error`() {
        // TODO: Accessibility Tests don't currently handle dialogs
        assumeFalse(accessibilityMode == AccessibilityMode.ACCESSIBILITY)

        snapshot { LoginLayout(UiState(loginError = LoginResponse.Error.NotConnected)) }
    }
}

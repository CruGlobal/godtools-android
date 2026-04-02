package org.cru.godtools.ui.drawer

import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import org.cru.godtools.base.ui.BasePaparazziTest
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class DrawerMenuLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    @Test
    fun `DrawerMenuLayout() - Open`() {
        snapshot { DrawerMenuLayout(state = DrawerMenuScreenStateTestData.open) {} }
    }
}

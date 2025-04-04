package org.cru.godtools.ui.account.globalactivity

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import org.cru.godtools.model.GlobalActivityAnalytics
import org.cru.godtools.ui.BasePaparazziTest
import org.cru.godtools.ui.account.ACCOUNT_PAGE_MARGIN_HORIZONTAL
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class GlobalActivityLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    val state = GlobalActivityScreen.UiState(
        activity = GlobalActivityAnalytics(
            users = 1234,
            gospelPresentations = 4321,
            countries = 123,
            launches = 54321,
        ),
    )

    @Test
    fun `GlobalActivityLayout()`() = snapshot {
        GlobalActivityLayout(state, modifier = Modifier.padding(horizontal = ACCOUNT_PAGE_MARGIN_HORIZONTAL))
    }
}

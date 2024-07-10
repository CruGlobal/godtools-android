package org.cru.godtools.ui.account.delete

import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.slack.circuit.test.TestEventSink
import kotlin.test.Test
import org.cru.godtools.ui.BasePaparazziTest
import org.junit.Assume.assumeFalse
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class DeleteAccountLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    private val events = TestEventSink<DeleteAccountScreen.Event>()

    @Test
    fun `DeleteAccountLayout() - Display`() {
        snapshot { DeleteAccountLayout(DeleteAccountScreen.State.Display(events)) }
    }

    @Test
    fun `DeleteAccountLayout() - Deleting`() {
        snapshot { DeleteAccountLayout(DeleteAccountScreen.State.Deleting(events)) }
    }

    @Test
    fun `DeleteAccountLayout() - Error`() {
        // TODO: Accessibility Tests don't currently handle dialogs
        assumeFalse(accessibilityMode == AccessibilityMode.ACCESSIBILITY)

        snapshot { DeleteAccountLayout(DeleteAccountScreen.State.Error(events)) }
    }
}

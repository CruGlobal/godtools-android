package org.cru.godtools.ui

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.accessibility.AccessibilityRenderExtension
import com.android.resources.NightMode
import org.cru.godtools.base.ui.compose.LocalEventBus
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.greenrobot.eventbus.EventBus
import org.junit.Rule

abstract class BasePaparazziTest(
    accessibilityRenderExtension: Boolean = false,
    nightMode: NightMode = NightMode.NOTNIGHT,
    private val eventBus: EventBus = EventBus(),
) {
    // HACK: workaround a "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner" error
    private val backPressDispatcher: OnBackPressedDispatcherOwner = object : OnBackPressedDispatcherOwner {
        override val lifecycle: Lifecycle get() = TODO("Not yet implemented")
        override val onBackPressedDispatcher = OnBackPressedDispatcher { /* Swallow all back-presses. */ }
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.NEXUS_5.copy(nightMode = nightMode),
        renderExtensions = buildSet {
            if (accessibilityRenderExtension) add(AccessibilityRenderExtension())
        }
    )

    protected fun centerInSnapshot(content: @Composable BoxScope.() -> Unit) {
        paparazzi.snapshot {
            CompositionLocalProvider(
                LocalEventBus provides eventBus,
                LocalOnBackPressedDispatcherOwner provides backPressDispatcher
            ) {
                GodToolsTheme {
                    Box(contentAlignment = Alignment.Center, content = content)
                }
            }
        }
    }
}

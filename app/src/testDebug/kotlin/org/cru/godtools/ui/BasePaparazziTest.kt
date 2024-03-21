package org.cru.godtools.ui

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.accessibility.AccessibilityRenderExtension
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import com.android.resources.NightMode
import kotlin.test.BeforeTest
import org.cru.godtools.base.ui.compose.LocalEventBus
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.greenrobot.eventbus.EventBus
import org.junit.Assume.assumeFalse
import org.junit.Rule

abstract class BasePaparazziTest(
    private val nightMode: NightMode = NightMode.NOTNIGHT,
    private val accessibilityMode: AccessibilityMode = AccessibilityMode.NO_ACCESSIBILITY,
    renderingMode: RenderingMode = RenderingMode.NORMAL,
    private val excludeRedundantTests: Boolean = true,
    private val eventBus: EventBus = EventBus(),
) {
    enum class AccessibilityMode { ACCESSIBILITY, NO_ACCESSIBILITY }

    // HACK: workaround a "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner" error
    private val backPressDispatcher: OnBackPressedDispatcherOwner = object : OnBackPressedDispatcherOwner {
        override val lifecycle: Lifecycle get() = TODO("Not yet implemented")
        override val onBackPressedDispatcher = OnBackPressedDispatcher { /* Swallow all back-presses. */ }
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.NEXUS_5.copy(nightMode = nightMode),
        renderingMode = when (accessibilityMode) {
            AccessibilityMode.ACCESSIBILITY -> when (renderingMode) {
                RenderingMode.SHRINK -> RenderingMode.NORMAL
                else -> renderingMode
            }
            else -> renderingMode
        },
        maxPercentDifference = 0.001,
        renderExtensions = buildSet {
            if (accessibilityMode == AccessibilityMode.ACCESSIBILITY) add(AccessibilityRenderExtension())
        },
    )

    @BeforeTest
    fun excludeRedundantTests() {
        if (excludeRedundantTests) {
            // don't run accessibility mode for night mode, we already test it for Not Night
            assumeFalse(accessibilityMode == AccessibilityMode.ACCESSIBILITY && nightMode == NightMode.NIGHT)
        }
    }

    protected fun centerInSnapshot(content: @Composable BoxScope.() -> Unit) {
        paparazzi.snapshot {
            CompositionLocalProvider(
                LocalEventBus provides eventBus,
                LocalOnBackPressedDispatcherOwner provides backPressDispatcher
            ) {
                GodToolsTheme {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.background(MaterialTheme.colorScheme.background),
                        content = content,
                    )
                }
            }
        }
    }
}

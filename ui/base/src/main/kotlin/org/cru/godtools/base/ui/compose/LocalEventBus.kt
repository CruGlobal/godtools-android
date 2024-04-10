@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package org.cru.godtools.base.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.greenrobot.eventbus.EventBus

object LocalEventBus {
    private val LocalComposition = staticCompositionLocalOf<EventBus?> { null }

    /**
     * Returns current EventBus value
     */
    val current: EventBus
        @Composable
        get() = LocalComposition.current
            ?: when {
                LocalInspectionMode.current -> EventBus()
                else -> LocalContext.current.let {
                    remember(it) { EntryPointAccessors.fromApplication<DaggerEntryPoint>(it).eventBus }
                }
            }

    /**
     * Associates a [LocalEventBus] key to a value in a call to [CompositionLocalProvider].
     */
    infix fun provides(eventBus: EventBus) = LocalComposition.provides(eventBus)

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DaggerEntryPoint {
        val eventBus: EventBus
    }
}

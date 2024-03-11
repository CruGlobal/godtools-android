@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package org.cru.godtools.base.ui.compose

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.cru.godtools.base.ui.util.openUrl
import org.greenrobot.eventbus.EventBus

@Deprecated("EventBus should be injected and used from Circuit Presenters only")
val LocalEventBus = staticCompositionLocalOf { EventBus() }

@Composable
internal fun CompositionLocals(disableDagger: Boolean = false, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val daggerComponents = when {
        LocalInspectionMode.current || disableDagger -> remember {
            object : ComposeEntryPoint {
                override val eventBus = EventBus()
            }
        }
        else -> remember { EntryPointAccessors.fromApplication<ComposeEntryPoint>(context) }
    }

    val uriHandler = remember(context) {
        object : UriHandler {
            override fun openUri(uri: String) {
                context.openUrl(Uri.parse(uri))
            }
        }
    }

    CompositionLocalProvider(
        LocalEventBus provides daggerComponents.eventBus,
        LocalUriHandler provides uriHandler,
        content = content
    )
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ComposeEntryPoint {
    val eventBus: EventBus
}

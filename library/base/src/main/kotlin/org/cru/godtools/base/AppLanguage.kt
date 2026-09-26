package org.cru.godtools.base

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.core.content.localizeIfPossible

private val appLanguageChanged = MutableSharedFlow<Unit>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
)

val Context.appLanguage: Locale
    get() = localizeIfPossible(AppCompatDelegate.getApplicationLocales())
        .getString(R.string.normalized_app_language)
        .let { Locale.forLanguageTag(it) }

fun Context.getAppLanguageFlow() = callbackFlow {
    // system and Android 13+ per-app language changes trigger a configuration change
    val callbacks = object : ComponentCallbacks {
        override fun onConfigurationChanged(newConfig: Configuration) {
            trySend(Unit)
        }

        @Deprecated("Deprecated in Java")
        override fun onLowMemory() = Unit
    }
    applicationContext.registerComponentCallbacks(callbacks)

    // in-app language changes on older Android versions only recreate activities, so they are signaled explicitly
    launch(start = CoroutineStart.UNDISPATCHED) {
        appLanguageChanged.onSubscription { emit(Unit) }.collect { send(Unit) }
    }

    awaitClose { applicationContext.unregisterComponentCallbacks(callbacks) }
}.map { appLanguage }.distinctUntilChanged().flowOn(Dispatchers.Default)

internal fun notifyAppLanguageChanged() {
    appLanguageChanged.tryEmit(Unit)
}

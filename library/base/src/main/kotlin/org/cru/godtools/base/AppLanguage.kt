package org.cru.godtools.base

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
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
    val application = applicationContext as? Application
    val callbacks = object : ComponentCallbacks, Application.ActivityLifecycleCallbacks {
        // system and Android 13+ per-app language changes trigger a configuration change
        override fun onConfigurationChanged(newConfig: Configuration) {
            trySend(Unit)
        }

        // before Android 13, AppCompat only loads the stored app language when the first activity is created
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            trySend(Unit)
        }

        @Deprecated("Deprecated in Java")
        override fun onLowMemory() = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityResumed(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }
    applicationContext.registerComponentCallbacks(callbacks)
    application?.registerActivityLifecycleCallbacks(callbacks)

    // in-app language changes on older Android versions only recreate activities, so they are signaled explicitly
    launch(start = CoroutineStart.UNDISPATCHED) {
        appLanguageChanged.onSubscription { emit(Unit) }.collect { send(Unit) }
    }

    awaitClose {
        applicationContext.unregisterComponentCallbacks(callbacks)
        application?.unregisterActivityLifecycleCallbacks(callbacks)
    }
}.conflate().map { appLanguage }.distinctUntilChanged().flowOn(Dispatchers.Default)

internal fun notifyAppLanguageChanged() {
    appLanguageChanged.tryEmit(Unit)
}

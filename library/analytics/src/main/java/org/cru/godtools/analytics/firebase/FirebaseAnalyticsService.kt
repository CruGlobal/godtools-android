package org.cru.godtools.analytics.firebase

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import com.google.android.gms.common.wrappers.InstantApps
import com.google.firebase.analytics.FirebaseAnalytics
import com.karumi.weak.weak
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.ccci.gto.android.common.okta.oidc.OktaUserProfileProvider
import org.ccci.gto.android.common.okta.oidc.net.response.ssoGuid
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsBaseEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

private const val USER_PROP_APP_TYPE = "godtools_app_type"
private const val VALUE_APP_TYPE_INSTANT = "instant"
private const val VALUE_APP_TYPE_INSTALLED = "installed"

@Singleton
class FirebaseAnalyticsService @VisibleForTesting internal constructor(
    app: Application,
    eventBus: EventBus,
    oktaUserProfileProvider: OktaUserProfileProvider,
    private val firebase: FirebaseAnalytics,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : Application.ActivityLifecycleCallbacks {
    @Inject
    @MainThread
    internal constructor(
        app: Application,
        eventBus: EventBus,
        oktaUserProfileProvider: OktaUserProfileProvider
    ) : this(app, eventBus, oktaUserProfileProvider, FirebaseAnalytics.getInstance(app))

    // region Tracking Events
    init {
        eventBus.register(this)
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onAnalyticsEvent(event: AnalyticsBaseEvent) {
        if (event.isForSystem(AnalyticsSystem.FIREBASE)) when (event) {
            is AnalyticsScreenEvent -> handleScreenEvent(event)
            is AnalyticsActionEvent -> handleActionEvent(event)
        }
    }

    // region ActivityLifecycleCallbacks
    init {
        app.registerActivityLifecycleCallbacks(this)
    }

    private var currentActivity: Activity? by weak()

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        currentActivity = currentActivity?.takeUnless { it === activity }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
    // endregion ActivityLifecycleCallbacks
    // endregion Tracking Events

    @MainThread
    private fun handleScreenEvent(event: AnalyticsScreenEvent) {
        currentActivity?.let { firebase.setCurrentScreen(it, event.screen, null) }
    }

    @MainThread
    private fun handleActionEvent(event: AnalyticsActionEvent) {
        firebase.logEvent(event.firebaseEventName, null)
    }

    init {
        oktaUserProfileProvider.userInfoFlow()
            .map { it?.ssoGuid }
            .distinctUntilChanged()
            .onEach { firebase.setUserId(it) }
            .launchIn(coroutineScope)

        firebase.setUserProperty(
            USER_PROP_APP_TYPE, if (InstantApps.isInstantApp(app)) VALUE_APP_TYPE_INSTANT else VALUE_APP_TYPE_INSTALLED
        )
    }
}

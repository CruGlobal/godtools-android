package org.cru.godtools.analytics.firebase

import android.app.Application
import android.os.Bundle
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import com.google.android.gms.common.wrappers.InstantApps
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton
import me.thekey.android.TheKey
import me.thekey.android.eventbus.event.TheKeyEvent
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
class FirebaseAnalyticsService @MainThread @Inject internal constructor(
    app: Application,
    eventBus: EventBus,
    private val theKey: TheKey
) {
    private val firebase = FirebaseAnalytics.getInstance(app)

    // region Tracking Events
    init {
        eventBus.register(this)
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onAnalyticsEvent(event: AnalyticsBaseEvent) {
        if (event.isForSystem(AnalyticsSystem.FIREBASE) || event.isForSystem(AnalyticsSystem.ADOBE)) when (event) {
            is AnalyticsScreenEvent -> handleScreenEvent(event)
            is AnalyticsActionEvent -> handleActionEvent(event)
        }
    }

    @AnyThread
    @Subscribe
    fun onTheKeyEvent(@Suppress("UNUSED_PARAMETER") event: TheKeyEvent) = updateUser()

    @AnyThread
    private fun updateUser() {
        firebase.setUserId(theKey.defaultSessionGuid)
    }

    @MainThread
    private fun handleScreenEvent(event: AnalyticsScreenEvent) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, event.screen)
        firebase.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    @MainThread
    private fun handleActionEvent(event: AnalyticsActionEvent) {
        firebase.logEvent(event.firebaseEventName, null)
    }

    init {
        updateUser()
        firebase.setUserProperty(
            USER_PROP_APP_TYPE, if (InstantApps.isInstantApp(app)) VALUE_APP_TYPE_INSTANT else VALUE_APP_TYPE_INSTALLED
        )
    }
}

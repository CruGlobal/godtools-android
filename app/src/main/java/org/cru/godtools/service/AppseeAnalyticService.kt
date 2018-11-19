package org.cru.godtools.service

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.appsee.Appsee
import com.appsee.AppseeListener
import com.appsee.AppseeScreenDetectedInfo
import com.appsee.AppseeSessionEndedInfo
import com.appsee.AppseeSessionEndingInfo
import com.appsee.AppseeSessionStartedInfo
import com.appsee.AppseeSessionStartingInfo
import com.crashlytics.android.Crashlytics
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.base.util.SingletonHolder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AppseeAnalyticService private constructor(application: Application) :
    Application.ActivityLifecycleCallbacks, AppseeListener {
    init {
        Appsee.addAppseeListener(this)
        EventBus.getDefault().register(this)
        application.registerActivityLifecycleCallbacks(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onAnalyticScreenEvent(event: AnalyticsScreenEvent) {
        if (event.isForSystem(AnalyticsSystem.APPSEE)) {
            Appsee.startScreen(event.screen)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onAnalyticActionEvent(event: AnalyticsActionEvent) {
        if (!event.isForSystem(AnalyticsSystem.APPSEE)) {
            Appsee.addEvent(event.action, event.attributes)
        }
    }

    // region Activity Lifecycle Callbacks

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle) = Appsee.start()

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    // endregion Activity Lifecycle Callbacks

    // region AppSee LifeCycle Callbacks

    override fun onAppseeSessionStarting(appseeSessionStartingInfo: AppseeSessionStartingInfo) {
        val appseeId = Appsee.generate3rdPartyId("Crashlytics", false)
        Crashlytics.setString("AppseeSessionUrl", "https://dashboard.appsee.com/3rdparty/crashlytics/$appseeId")
    }

    override fun onAppseeSessionStarted(appseeSessionStartedInfo: AppseeSessionStartedInfo) {}

    override fun onAppseeSessionEnding(appseeSessionEndingInfo: AppseeSessionEndingInfo) {}

    override fun onAppseeSessionEnded(appseeSessionEndedInfo: AppseeSessionEndedInfo) {}

    override fun onAppseeScreenDetected(appseeScreenDetectedInfo: AppseeScreenDetectedInfo) {}

    // endregion AppSee LifeCycle Callbacks

    companion object : SingletonHolder<AppseeAnalyticService, Application>(::AppseeAnalyticService)
}

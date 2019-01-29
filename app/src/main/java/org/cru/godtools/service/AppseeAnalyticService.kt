package org.cru.godtools.service

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.appsee.Appsee
import com.appsee.AppseeListener
import com.appsee.AppseeScreenDetectedInfo
import com.appsee.AppseeSessionEndedInfo
import com.appsee.AppseeSessionEndingInfo
import com.appsee.AppseeSessionStartedInfo
import com.appsee.AppseeSessionStartingInfo
import com.crashlytics.android.Crashlytics
import me.thekey.android.view.dialog.LoginDialogFragment
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.util.SingletonHolder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class AppseeAnalyticService private constructor(application: Application) :
    Application.ActivityLifecycleCallbacks, AppseeListener {
    private val fragmentLifecycleCallbacks = AppseeFragmentLifecycleCallbacks()
    private val suppressedAppseeComponents = mutableSetOf<Reference<Any?>>()

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

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is FragmentActivity)
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)

        if (activity !is BaseActivity || activity.enableAppsee()) Appsee.start()
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        if (activity is BaseActivity && !activity.enableAppsee()) suppressComponent(activity)
    }

    override fun onActivityPaused(activity: Activity) = removeSuppressedComponent(activity)

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

    override fun onAppseeScreenDetected(appseeScreenDetectedInfo: AppseeScreenDetectedInfo) {
        // we don't want to use automatic screen detection, we will manually track screen names using analytics events
        appseeScreenDetectedInfo.screenName = null
    }

    // endregion AppSee LifeCycle Callbacks

    // region Suppressed Components Tracking

    internal fun suppressComponent(obj: Any) {
        suppressedAppseeComponents.add(WeakReference(obj))
        updateAppseeRecordingState()
    }

    internal fun removeSuppressedComponent(obj: Any) {
        suppressedAppseeComponents.iterator().run {
            while (hasNext()) {
                when (next().get()) {
                    null, obj -> remove()
                }
            }
        }
        updateAppseeRecordingState()
    }

    private fun updateAppseeRecordingState() {
        suppressedAppseeComponents.iterator().run {
            while (hasNext()) {
                when (next().get()) {
                    null -> remove()
                    else -> {
                        Appsee.pause()
                        return
                    }
                }
            }
        }

        Appsee.resume()
    }

    // endregion Suppressed Components Tracking

    companion object : SingletonHolder<AppseeAnalyticService, Application>(::AppseeAnalyticService)

    internal inner class AppseeFragmentLifecycleCallbacks : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            if (f is LoginDialogFragment) suppressComponent(f)
        }

        override fun onFragmentPaused(fm: FragmentManager, f: Fragment) = removeSuppressedComponent(f)
    }
}

package org.cru.godtools.analytics.appsflyer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.appsflyer.AFLogger
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.karumi.weak.weak
import javax.inject.Inject
import javax.inject.Singleton
import org.cru.godtools.analytics.BuildConfig
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

private const val TAG = "AppsFlyerAnalytics"

@VisibleForTesting
internal const val AF_DP = "af_dp"
private const val AF_STATUS = "af_status"
private const val IS_FIRST_LAUNCH = "is_first_launch"

private const val STATUS_NON_ORGANIC = "Non-organic"

@Singleton
class AppsFlyerAnalyticsService @VisibleForTesting internal constructor(
    private val app: Application,
    eventBus: EventBus,
    private val deepLinkResolvers: Set<@JvmSuppressWildcards AppsFlyerDeepLinkResolver>,
    private val appsFlyer: AppsFlyerLib
) : Application.ActivityLifecycleCallbacks {
    @Inject
    internal constructor(
        app: Application,
        eventBus: EventBus,
        deepLinkResolvers: Set<@JvmSuppressWildcards AppsFlyerDeepLinkResolver>
    ) : this(app, eventBus, deepLinkResolvers, AppsFlyerLib.getInstance())

    // region Analytics Events
    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onAnalyticsScreenEvent(event: AnalyticsScreenEvent) {
        if (event.isForSystem(AnalyticsSystem.APPSFLYER))
            appsFlyer.logEvent(app, event.screen, emptyMap())
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onAnalyticsActionEvent(event: AnalyticsActionEvent) {
        if (event.isForSystem(AnalyticsSystem.APPSFLYER))
            appsFlyer.logEvent(app, event.action, emptyMap())
    }
    // endregion Analytics Events

    // region Application.ActivityLifecycleCallbacks
    private var activeActivity: Activity? by weak()

    override fun onActivityResumed(activity: Activity) {
        activeActivity = activity
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
    // endregion Application.ActivityLifecycleCallbacks

    // region AppsFlyerConversionListener
    @VisibleForTesting
    internal val conversionListener = object : AppsFlyerConversionListener {
        override fun onConversionDataSuccess(data: Map<String, Any?>) {
            Timber.tag(TAG).d("onConversionDataSuccess($data)")
            if (data[IS_FIRST_LAUNCH] == true && data[AF_STATUS] == STATUS_NON_ORGANIC) {
                onAppOpenAttribution(mapOf(AF_DP to data[AF_DP] as? String))
            }
        }

        override fun onAppOpenAttribution(data: Map<String, String?>) {
            Timber.tag(TAG).d("onAppOpenAttribution($data)")
            val uri = data[AF_DP]?.let { Uri.parse(it) }
            deepLinkResolvers.asSequence().mapNotNull { it.resolve(app, uri, data) }.firstOrNull()?.let { intent ->
                activeActivity?.startActivity(intent)
            }
        }

        override fun onConversionDataFail(error: String) = Unit
        override fun onAttributionFailure(error: String) = Unit
    }
    // endregion AppsFlyerConversionListener

    init {
        appsFlyer.apply {
            if (BuildConfig.DEBUG) setLogLevel(AFLogger.LogLevel.DEBUG)
            setOneLinkCustomDomain("get.godtoolsapp.com")
            init(BuildConfig.APPSFLYER_DEV_KEY, conversionListener, app)
            start(app)
        }
        app.registerActivityLifecycleCallbacks(this)
        eventBus.register(this)
    }
}

interface AppsFlyerDeepLinkResolver {
    fun resolve(context: Context, uri: Uri?, data: Map<String, String?>): Intent?
}

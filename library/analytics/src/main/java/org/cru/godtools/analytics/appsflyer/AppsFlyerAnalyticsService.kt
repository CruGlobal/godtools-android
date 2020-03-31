package org.cru.godtools.analytics.appsflyer

import android.app.Application
import androidx.annotation.WorkerThread
import com.adobe.mobile.Visitor
import com.appsflyer.AFLogger
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cru.godtools.analytics.BuildConfig
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AppsFlyerAnalytics"

@Singleton
class AppsFlyerAnalyticsService @Inject internal constructor(private val app: Application, eventBus: EventBus) {
    private val appsFlyer: AppsFlyerLib = AppsFlyerLib.getInstance()

    init {
        appsFlyer.apply {
            init(BuildConfig.APPSFLYER_DEV_KEY, GodToolsAppsFlyerConversionListener, app)
            if (BuildConfig.DEBUG) setLogLevel(AFLogger.LogLevel.DEBUG)
        }

        GlobalScope.launch(Dispatchers.Default) {
            val mcId = Visitor.getMarketingCloudId()
            withContext(Dispatchers.Main) {
                appsFlyer.setAdditionalData(hashMapOf("marketingCloudID" to mcId))
                appsFlyer.startTracking(app)
                eventBus.register(this@AppsFlyerAnalyticsService)
            }
        }
    }

    // region Analytics Events
    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onAnalyticsScreenEvent(event: AnalyticsScreenEvent) {
        if (event.isForSystem(AnalyticsSystem.APPSFLYER))
            appsFlyer.trackEvent(app, event.screen, emptyMap())
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onAnalyticsActionEvent(event: AnalyticsActionEvent) {
        if (event.isForSystem(AnalyticsSystem.APPSFLYER))
            appsFlyer.trackEvent(app, event.action, emptyMap())
    }
    // endregion Analytics Events
}

private object GodToolsAppsFlyerConversionListener : AppsFlyerConversionListener {
    override fun onAppOpenAttribution(data: Map<String, String>?) {
        data?.map { (key, value) ->
            Timber.tag(TAG).d("AppsFlyer onAppOpenAttribution Attribute: %s = %s", key, value)
        }
    }

    override fun onAttributionFailure(error: String?) {
        Timber.tag(TAG).e("AppsFlyer onAttributionFailure: %s", error)
    }

    override fun onConversionDataSuccess(data: Map<String, Any>?) {
        data?.map { (key, value) ->
            Timber.tag(TAG).d("AppsFlyer onConversionDataSuccess Attribute: %s = %s", key, value)
        }
    }

    override fun onConversionDataFail(error: String?) {
        Timber.tag(TAG).e("AppsFlyer onConversionDataFail: %s", error)
    }
}

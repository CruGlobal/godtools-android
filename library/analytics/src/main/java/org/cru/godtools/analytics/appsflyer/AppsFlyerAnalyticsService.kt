package org.cru.godtools.analytics.appsflyer

import android.app.Application
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import org.cru.godtools.analytics.BuildConfig
import org.cru.godtools.base.util.SingletonHolder
import timber.log.Timber

private const val TAG = "AppsFlyerAnalyticsSrvc"

class AppsFlyerAnalyticsService private constructor(app: Application) {
    companion object : SingletonHolder<AppsFlyerAnalyticsService, Application>(::AppsFlyerAnalyticsService)

    init {
        AppsFlyerLib.getInstance()
            .init(BuildConfig.APPSFLYER_DEV_KEY, GodToolsAppsFlyerConversionListener, app.applicationContext)
            .startTracking(app)
    }
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

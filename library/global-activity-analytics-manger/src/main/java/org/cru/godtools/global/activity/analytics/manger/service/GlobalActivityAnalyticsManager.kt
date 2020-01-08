package org.cru.godtools.global.activity.analytics.manger.service

import android.content.Context
import androidx.lifecycle.LiveData
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiConverterFactory
import org.ccci.gto.android.common.okhttp3.util.attachGlobalInterceptors
import org.ccci.gto.android.common.util.DynamicSSLSocketFactory
import org.cru.godtools.global.activity.analytics.manger.api.GlobalActivityAnalyticsApi
import org.cru.godtools.global.activity.analytics.manger.db.GlobalActivityAnalyticsDatabase
import org.cru.godtools.global.activity.analytics.manger.model.GlobalActivityAnalytics
import retrofit2.Retrofit
import timber.log.Timber
import java.security.NoSuchAlgorithmException
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory

class GlobalActivityAnalyticsManager(context: Context, private val url: String) {

    // region database

    private val globalActivityDatabase = GlobalActivityAnalyticsDatabase.getInstance(context)

    private fun getGlobalActivity() : GlobalActivityAnalytics {
        return globalActivityDatabase.globalActivityDao().getGlobalActivity()
    }

    private fun updateOrAddGlobalActivity(globalActivityAnalytics: GlobalActivityAnalytics) {
        globalActivityAnalytics.lastUpdated = Date()
        globalActivityDatabase.globalActivityRepository().addOrUpdateGlobalActivity(globalActivityAnalytics)
    }

    //endregion database

    // region global access

    fun getGlobalActivityLiveData(): LiveData<GlobalActivityAnalytics> {

        if (getGlobalActivity().lastUpdated?.time ?: 0 < Date().time) {
            globalActivityApi.download().execute().body()?.data?.get(0)?.let {
                updateOrAddGlobalActivity(it)
            }
        }

        return globalActivityDatabase.globalActivityDao().getGlobalActivityLiveData()
    }

    // endregion global access

    // region api

    private val globalActivityApi: GlobalActivityAnalyticsApi = buildGlobalActivityApi()

    private fun buildGlobalActivityApi(): GlobalActivityAnalyticsApi {
        val okHttp = buildOkHttpClient()

        // create RetroFit API
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(
                JsonApiConverterFactory.create(
                    JsonApiConverter.Builder()
                        .addClasses(GlobalActivityAnalytics::class.java)
                        .build()
                )
            )
            .callFactory(okHttp)
            .build()
            .create(GlobalActivityAnalyticsApi::class.java)
    }

    private fun buildOkHttpClient(): OkHttpClient {
        // create OkHttp client
        val builder = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
        try {
            // customize the SSLSocketFactory for this OkHttpClient
            // enable TLS 1.1 and TLS 1.2 for older versions of android that support it but don't enable it
            val factory: SSLSocketFactory = DynamicSSLSocketFactory.create()
                .addEnabledProtocols(TlsVersion.TLS_1_1.javaName(), TlsVersion.TLS_1_2.javaName())
                .build()
            builder.sslSocketFactory(factory)
        } catch (e: IllegalStateException) {
            Timber.tag("GlobalActivityApi").d(e, "Error creating the DynamicSSLSocketFactory")
        } catch (e: NoSuchAlgorithmException) {
            Timber.tag("GlobalActivityApi").d(e, "Error creating the DynamicSSLSocketFactory")
        }
        return builder.attachGlobalInterceptors().build()
    }

    // endregion api
}

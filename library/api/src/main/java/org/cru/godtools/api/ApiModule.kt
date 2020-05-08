package org.cru.godtools.api

import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.internal.Util
import org.ccci.gto.android.common.api.retrofit2.converter.JSONObjectConverterFactory
import org.ccci.gto.android.common.api.retrofit2.converter.LocaleConverterFactory
import org.ccci.gto.android.common.dagger.okhttp3.InterceptorType
import org.ccci.gto.android.common.dagger.okhttp3.InterceptorType.Type.NETWORK_INTERCEPTOR
import org.ccci.gto.android.common.dagger.okhttp3.OkHttp3Module
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiConverterFactory
import org.ccci.gto.android.common.util.DynamicSSLSocketFactory
import org.cru.godtools.api.model.ToolViews
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Followup
import org.cru.godtools.model.GlobalActivityAnalytics
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.jsonapi.ToolTypeConverter
import retrofit2.Retrofit
import retrofit2.create
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [OkHttp3Module::class])
object ApiModule {
    @Provides
    @Singleton
    fun okhttp(
        @InterceptorType(NETWORK_INTERCEPTOR) networkInterceptors: Set<@JvmSuppressWildcards Interceptor>
    ) = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .apply {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                try {
                    // customize the SSLSocketFactory for OkHttpClient
                    val factory = DynamicSSLSocketFactory.create()
                        // enable TLS 1.1 and TLS 1.2 for older versions of android that support it but don't enable it
                        .addEnabledProtocols(TlsVersion.TLS_1_1.javaName(), TlsVersion.TLS_1_2.javaName())
                        .build()
                    sslSocketFactory(factory, Util.platformTrustManager())
                } catch (e: Exception) {
                    Timber.tag("ApiModule").e(e, "Error creating the DynamicSSLSocketFactory for OkHttp")
                }
            }
        }
        .apply { networkInterceptors.forEach { addNetworkInterceptor(it) } }
        .build()

    @Provides
    @Reusable
    fun jsonApiConverter() = JsonApiConverter.Builder()
        .addClasses(Language::class.java)
        .addClasses(Tool::class.java, ToolViews::class.java)
        .addClasses(Attachment::class.java)
        .addClasses(Translation::class.java)
        .addClasses(Followup::class.java)
        .addClasses(GlobalActivityAnalytics::class.java)
        .addConverters(ToolTypeConverter)
        .addConverters(LocaleTypeConverter)
        .build()

    // region mobile-content-api APIs
    const val MOBILE_CONTENT_API_URL = "MOBILE_CONTENT_API_BASE_URL"
    private const val MOBILE_CONTENT_API = "MOBILE_CONTENT_API"

    @Provides
    @Reusable
    @Named(MOBILE_CONTENT_API)
    fun mobileContentApiRetrofit(
        @Named(MOBILE_CONTENT_API_URL) baseUrl: String,
        jsonApiConverter: JsonApiConverter,
        okhttp: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(LocaleConverterFactory())
        .addConverterFactory(JsonApiConverterFactory.create(jsonApiConverter))
        .callFactory(okhttp)
        .build()

    @Provides
    @Reusable
    fun analyticsApi(@Named(MOBILE_CONTENT_API) retrofit: Retrofit): AnalyticsApi = retrofit.create()

    @Provides
    @Reusable
    fun attachmentsApi(@Named(MOBILE_CONTENT_API) retrofit: Retrofit): AttachmentsApi = retrofit.create()

    @Provides
    @Reusable
    fun followupApi(@Named(MOBILE_CONTENT_API) retrofit: Retrofit): FollowupApi = retrofit.create()

    @Provides
    @Reusable
    fun languagesApi(@Named(MOBILE_CONTENT_API) retrofit: Retrofit): LanguagesApi = retrofit.create()

    @Provides
    @Reusable
    fun toolsApi(@Named(MOBILE_CONTENT_API) retrofit: Retrofit): ToolsApi = retrofit.create()

    @Provides
    @Reusable
    fun translationsApi(@Named(MOBILE_CONTENT_API) retrofit: Retrofit): TranslationsApi = retrofit.create()

    @Provides
    @Reusable
    fun viewsApi(@Named(MOBILE_CONTENT_API) retrofit: Retrofit): ViewsApi = retrofit.create()
    // region mobile-content-api APIs

    // region Adobe APIs
    @Provides
    @Reusable
    fun campaignFormsApi(okhttp: OkHttpClient) =
        Retrofit.Builder().baseUrl(BuildConfig.CAMPAIGN_FORMS_API)
            .addConverterFactory(JSONObjectConverterFactory())
            .callFactory(okhttp)
            .build().create<CampaignFormsApi>()
    // endregion Adobe APIs
}

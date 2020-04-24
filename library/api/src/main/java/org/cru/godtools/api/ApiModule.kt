package org.cru.godtools.api

import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.OkHttpClient
import org.ccci.gto.android.common.api.retrofit2.converter.JSONObjectConverterFactory
import org.ccci.gto.android.common.okhttp3.util.attachGlobalInterceptors
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
object ApiModule {
    const val MOBILE_CONTENT_API_BASE_URI = "MOBILE_CONTENT_API_BASE_URI"

    @Provides
    @Singleton
    fun okhttp() = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .attachGlobalInterceptors()
        .build()

    @Provides
    @Reusable
    fun godToolsApi(okhttp: OkHttpClient, @Named(MOBILE_CONTENT_API_BASE_URI) baseUri: String) =
        GodToolsApi(baseUri, okhttp)

    // region mobile-content-api APIs
    @Provides
    @Reusable
    fun analyticsApi(godToolsApi: GodToolsApi) = godToolsApi.analytics

    @Provides
    @Reusable
    fun followupApi(godToolsApi: GodToolsApi) = godToolsApi.followups

    @Provides
    @Reusable
    fun languagesApi(godToolsApi: GodToolsApi) = godToolsApi.languages

    @Provides
    @Reusable
    fun toolsApi(godToolsApi: GodToolsApi) = godToolsApi.tools

    @Provides
    @Reusable
    fun viewsApi(godToolsApi: GodToolsApi) = godToolsApi.views
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

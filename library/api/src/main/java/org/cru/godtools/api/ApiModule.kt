package org.cru.godtools.api

import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.OkHttpClient
import org.ccci.gto.android.common.okhttp3.util.attachGlobalInterceptors
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
}

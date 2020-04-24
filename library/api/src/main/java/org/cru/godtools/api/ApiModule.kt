package org.cru.godtools.api

import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.OkHttpClient
import org.ccci.gto.android.common.api.retrofit2.converter.JSONObjectConverterFactory
import org.ccci.gto.android.common.api.retrofit2.converter.LocaleConverterFactory
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiConverterFactory
import org.ccci.gto.android.common.okhttp3.util.attachGlobalInterceptors
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
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
object ApiModule {
    @Provides
    @Singleton
    fun okhttp() = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .attachGlobalInterceptors()
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
        .addConverters(LocaleTypeConverter())
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
    ) = Retrofit.Builder()
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

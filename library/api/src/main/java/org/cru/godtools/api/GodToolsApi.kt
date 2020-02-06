package org.cru.godtools.api

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

class GodToolsApi private constructor(mobileContentApiUrl: String) {
    private val jsonApiConverter: JsonApiConverter by lazy {
        JsonApiConverter.Builder()
            .addClasses(Language::class.java)
            .addClasses(Tool::class.java, ToolViews::class.java)
            .addClasses(Attachment::class.java)
            .addClasses(Translation::class.java)
            .addClasses(Followup::class.java)
            .addClasses(GlobalActivityAnalytics::class.java)
            .addConverters(ToolTypeConverter)
            .addConverters(LocaleTypeConverter())
            .build()
    }

    private val okhttp: OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .attachGlobalInterceptors()
            .build()

    private val mobileContentRetrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(mobileContentApiUrl) // attach the various converter factories
            .addConverterFactory(LocaleConverterFactory())
            .addConverterFactory(JsonApiConverterFactory.create(jsonApiConverter))
            .callFactory(okhttp)
            .build()

    // Mobile Content APIs
    @JvmField
    val languages: LanguagesApi = mobileContentRetrofit.create()
    @JvmField
    val tools: ToolsApi = mobileContentRetrofit.create()
    @JvmField
    val translations: TranslationsApi = mobileContentRetrofit.create()
    @JvmField
    val attachments: AttachmentsApi = mobileContentRetrofit.create()
    @JvmField
    val followups: FollowupApi = mobileContentRetrofit.create()
    @JvmField
    val views: ViewsApi = mobileContentRetrofit.create()
    val analytics: AnalyticsApi = mobileContentRetrofit.create()

    // Adobe Campaign Forms API
    val campaignForms by lazy {
        Retrofit.Builder().baseUrl(BuildConfig.CAMPAIGN_FORMS_API)
            .addConverterFactory(JSONObjectConverterFactory())
            .callFactory(okhttp)
            .build().create<CampaignFormsApi>()
    }

    companion object {
        private lateinit var INSTANCE: GodToolsApi

        @JvmStatic
        @Synchronized
        fun configure(apiUri: String) {
            check(::INSTANCE.isInitialized) { "Attempted to configure GodToolsApi multiple times" }
            INSTANCE = GodToolsApi(apiUri)
        }

        @JvmStatic
        @Synchronized
        fun getInstance(): GodToolsApi {
            check(!::INSTANCE.isInitialized) { "Attempted to use GodTools API before it was configured" }
            return INSTANCE
        }
    }
}

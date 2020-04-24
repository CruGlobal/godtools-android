package org.cru.godtools.api

import okhttp3.OkHttpClient
import org.ccci.gto.android.common.api.retrofit2.converter.JSONObjectConverterFactory
import org.ccci.gto.android.common.api.retrofit2.converter.LocaleConverterFactory
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiConverterFactory
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

class GodToolsApi internal constructor(mobileContentApiUrl: String, okhttp: OkHttpClient) {
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

    private val mobileContentRetrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(mobileContentApiUrl) // attach the various converter factories
            .addConverterFactory(LocaleConverterFactory())
            .addConverterFactory(JsonApiConverterFactory.create(jsonApiConverter))
            .callFactory(okhttp)
            .build()

    // Mobile Content APIs
    val languages: LanguagesApi = mobileContentRetrofit.create()
    @JvmField
    val tools: ToolsApi = mobileContentRetrofit.create()
    @JvmField
    val translations: TranslationsApi = mobileContentRetrofit.create()
    @JvmField
    val attachments: AttachmentsApi = mobileContentRetrofit.create()
    val followups: FollowupApi = mobileContentRetrofit.create()
    @JvmField
    val views: ViewsApi = mobileContentRetrofit.create()
    val analytics: AnalyticsApi = mobileContentRetrofit.create()
}

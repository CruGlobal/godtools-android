package org.cru.godtools.api

import android.app.Application
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.ccci.gto.android.common.dagger.okhttp3.InterceptorType
import org.ccci.gto.android.common.dagger.okhttp3.InterceptorType.Type.NETWORK_INTERCEPTOR
import org.ccci.gto.android.common.dagger.okhttp3.OkHttp3Module
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiConverterFactory
import org.ccci.gto.android.common.jsonapi.scarlet.JsonApiMessageAdapterFactory
import org.ccci.gto.android.common.retrofit2.converter.JSONObjectConverterFactory
import org.ccci.gto.android.common.retrofit2.converter.LocaleConverterFactory
import org.ccci.gto.android.common.scarlet.ReferenceLifecycle
import org.ccci.gto.android.common.scarlet.actioncable.ActionCableMessageAdapterFactory
import org.ccci.gto.android.common.scarlet.actioncable.okhttp3.ActionCableRequestFactory
import org.cru.godtools.api.model.NavigationEvent
import org.cru.godtools.api.model.PublisherInfo
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

@Module(includes = [OkHttp3Module::class])
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun okhttp(
        @InterceptorType(NETWORK_INTERCEPTOR) networkInterceptors: Set<@JvmSuppressWildcards Interceptor>
    ) = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
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
        .addClasses(PublisherInfo::class.java, NavigationEvent::class.java)
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
        .addConverterFactory(LocaleConverterFactory)
        .addConverterFactory(JsonApiConverterFactory.create(jsonApiConverter))
        .callFactory(okhttp)
        .build()

    @Provides
    @Reusable
    fun @receiver:Named(MOBILE_CONTENT_API) Retrofit.analyticsApi() = create<AnalyticsApi>()

    @Provides
    @Reusable
    fun @receiver:Named(MOBILE_CONTENT_API) Retrofit.attachmentsApi() = create<AttachmentsApi>()

    @Provides
    @Reusable
    fun @receiver:Named(MOBILE_CONTENT_API) Retrofit.followupApi() = create<FollowupApi>()

    @Provides
    @Reusable
    fun @receiver:Named(MOBILE_CONTENT_API) Retrofit.languagesApi() = create<LanguagesApi>()

    @Provides
    @Reusable
    fun @receiver:Named(MOBILE_CONTENT_API) Retrofit.toolsApi() = create<ToolsApi>()

    @Provides
    @Reusable
    fun @receiver:Named(MOBILE_CONTENT_API) Retrofit.translationsApi() = create<TranslationsApi>()

    @Provides
    @Reusable
    fun @receiver:Named(MOBILE_CONTENT_API) Retrofit.viewsApi() = create<ViewsApi>()

    @Provides
    @Singleton
    fun scarletReferenceLifecycle() = ReferenceLifecycle()

    @Provides
    @Reusable
    fun actionCableScarlet(
        @Named(MOBILE_CONTENT_API_URL) baseUrl: String,
        app: Application,
        jsonApi: JsonApiConverter,
        okhttp: OkHttpClient,
        referenceLifecycle: ReferenceLifecycle
    ) = Scarlet.Builder()
        .webSocketFactory(okhttp.newWebSocketFactory(ActionCableRequestFactory("${baseUrl}cable")))
        .addMessageAdapterFactory(
            ActionCableMessageAdapterFactory.Builder()
                .addMessageAdapterFactory(JsonApiMessageAdapterFactory(jsonApi))
                .build()
        )
        .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
        .lifecycle(AndroidLifecycle.ofApplicationForeground(app).combineWith(referenceLifecycle))
        .build()

    @Provides
    @Singleton
    fun tractShareService(scarlet: Scarlet): TractShareService = scarlet.create()
    // endregion mobile-content-api APIs

    // region Adobe APIs
    @Provides
    @Reusable
    fun campaignFormsApi(okhttp: OkHttpClient) =
        Retrofit.Builder().baseUrl(BuildConfig.CAMPAIGN_FORMS_API)
            .addConverterFactory(JSONObjectConverterFactory)
            .callFactory(okhttp)
            .build().create<CampaignFormsApi>()
    // endregion Adobe APIs
}

package org.cru.godtools.api

import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Inject
import javax.inject.Named

class GodToolsApi @Inject internal constructor(@Named(ApiModule.MOBILE_CONTENT_API) retrofit: Retrofit) {
    // Mobile Content APIs
    val languages: LanguagesApi = retrofit.create()
    @JvmField
    val tools: ToolsApi = retrofit.create()
    @JvmField
    val translations: TranslationsApi = retrofit.create()
    @JvmField
    val attachments: AttachmentsApi = retrofit.create()
    val followups: FollowupApi = retrofit.create()
    @JvmField
    val views: ViewsApi = retrofit.create()
    val analytics: AnalyticsApi = retrofit.create()
}

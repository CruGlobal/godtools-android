package org.cru.godtools.api

import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Inject
import javax.inject.Named

class GodToolsApi @Inject internal constructor(@Named(ApiModule.MOBILE_CONTENT_API) retrofit: Retrofit) {
    @JvmField
    val translations: TranslationsApi = retrofit.create()
    @JvmField
    val attachments: AttachmentsApi = retrofit.create()
}

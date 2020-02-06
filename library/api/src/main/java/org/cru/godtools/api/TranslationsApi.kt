package org.cru.godtools.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

private const val PATH_TRANSLATIONS = "translations"

interface TranslationsApi {
    @Streaming
    @GET("$PATH_TRANSLATIONS/{id}")
    fun download(@Path("id") id: Long): Call<ResponseBody>
}

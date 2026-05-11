package org.cru.godtools.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface CdnApi {
    @Streaming
    @GET("translations/files/{filename}")
    suspend fun downloadPublishedFile(@Path("filename") name: String): Response<ResponseBody>
}

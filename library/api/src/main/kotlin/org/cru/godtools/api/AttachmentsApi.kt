package org.cru.godtools.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

private const val PATH_ATTACHMENTS = "attachments"

interface AttachmentsApi {
    @Streaming
    @GET("$PATH_ATTACHMENTS/{id}/download")
    suspend fun download(@Path("id") id: Long): Response<ResponseBody>
}

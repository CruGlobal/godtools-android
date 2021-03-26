package org.cru.godtools.article.aem.api

import android.net.Uri
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface AemApi {
    @GET
    suspend fun getJson(@Url uri: Uri, @Query("_") timestamp: Long): Response<JSONObject?>

    @GET
    suspend fun downloadArticle(@Url uri: Uri): Response<String?>

    @GET
    @Streaming
    suspend fun downloadResource(@Url uri: Uri): Response<ResponseBody?>
}

package org.cru.godtools.article.aem.api

import android.net.Uri
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface AemApi {
    @GET
    fun getJson(@Url uri: Uri, @Query("_") timestamp: Long): Call<JSONObject?>

    @GET
    fun downloadArticle(@Url uri: Uri): Call<String?>

    @GET
    @Streaming
    fun downloadResource(@Url uri: Uri): Call<ResponseBody?>
}

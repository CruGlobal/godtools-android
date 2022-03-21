package org.cru.godtools.api

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams
import org.cru.godtools.model.Tool
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

private const val PATH_RESOURCES = "resources"
private const val PARAM_FILTER_SYSTEM = "filter[system]"

interface ToolsApi {
    @GET("$PATH_RESOURCES?$PARAM_FILTER_SYSTEM=${BuildConfig.MOBILE_CONTENT_SYSTEM}")
    suspend fun list(@QueryMap params: JsonApiParams): Response<JsonApiObject<Tool>>

    @GET("$PATH_RESOURCES/{id}")
    fun get(@Path("id") id: Long, @QueryMap params: JsonApiParams): Call<JsonApiObject<Tool>>
}

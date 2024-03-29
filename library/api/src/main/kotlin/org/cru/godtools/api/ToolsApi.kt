package org.cru.godtools.api

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams
import org.cru.godtools.model.Tool
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

private const val PATH_RESOURCES = "resources"
private const val PARAM_FILTER_SYSTEM = "filter[system]"
private const val PARAM_FILTER_CODE = "filter[abbreviation]"

interface ToolsApi {
    @GET("$PATH_RESOURCES?$PARAM_FILTER_SYSTEM=${BuildConfig.MOBILE_CONTENT_SYSTEM}")
    suspend fun list(@QueryMap params: JsonApiParams): Response<JsonApiObject<Tool>>

    @GET(PATH_RESOURCES)
    suspend fun getTool(
        @Query(PARAM_FILTER_CODE) code: String,
        @QueryMap params: JsonApiParams
    ): Response<JsonApiObject<Tool>>
}

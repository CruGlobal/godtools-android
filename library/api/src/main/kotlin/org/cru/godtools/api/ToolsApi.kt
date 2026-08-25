package org.cru.godtools.api

import java.util.Locale
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
private const val PARAM_FILTER_LANGUAGE = "filter[lang]"
private const val PARAM_FILTER_COUNTRY = "filter[country]"

interface ToolsApi {
    @GET("$PATH_RESOURCES?$PARAM_FILTER_SYSTEM=${BuildConfig.MOBILE_CONTENT_SYSTEM}")
    suspend fun list(@QueryMap params: JsonApiParams): Response<JsonApiObject<Tool>>

    @GET(PATH_RESOURCES)
    suspend fun getTool(
        @Query(PARAM_FILTER_CODE) code: String,
        @QueryMap params: JsonApiParams,
    ): Response<JsonApiObject<Tool>>

    @GET("$PATH_RESOURCES/featured")
    suspend fun getFeaturedTools(
        @Query(PARAM_FILTER_LANGUAGE) locale: Locale,
        @Query(PARAM_FILTER_COUNTRY) country: String,
        @QueryMap params: JsonApiParams,
    ): Response<JsonApiObject<Tool>>

    @GET("$PATH_RESOURCES/default_order")
    suspend fun getToolOrder(
        @Query(PARAM_FILTER_LANGUAGE) locale: Locale,
        @Query(PARAM_FILTER_COUNTRY) country: String? = null,
        @QueryMap params: JsonApiParams,
    ): Response<JsonApiObject<Tool>>
}

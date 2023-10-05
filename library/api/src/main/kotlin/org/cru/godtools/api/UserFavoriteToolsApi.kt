package org.cru.godtools.api

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams
import org.ccci.gto.android.common.jsonapi.retrofit2.annotation.JsonApiFields
import org.cru.godtools.model.Tool
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface UserFavoriteToolsApi {
    companion object {
        internal const val PATH_FAVORITE_TOOLS = "$PATH_USER/relationships/favorite-tools"
    }

    @POST(PATH_FAVORITE_TOOLS)
    suspend fun addFavoriteTools(
        @QueryMap params: JsonApiParams = JsonApiParams(),
        @Body
        @JsonApiFields(Tool.JSONAPI_TYPE)
        tools: List<Tool>,
    ): Response<JsonApiObject<Tool>>

    @HTTP(method = "DELETE", path = PATH_FAVORITE_TOOLS, hasBody = true)
    suspend fun removeFavoriteTools(
        @QueryMap params: JsonApiParams = JsonApiParams(),
        @Body
        @JsonApiFields(Tool.JSONAPI_TYPE)
        tools: List<Tool>,
    ): Response<JsonApiObject<Tool>>
}

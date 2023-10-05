package org.cru.godtools.api

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams
import org.ccci.gto.android.common.jsonapi.retrofit2.model.JsonApiRetrofitObject
import org.cru.godtools.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.QueryMap

internal const val PATH_USER = "users/me"

interface UserApi {
    @GET(PATH_USER)
    suspend fun getUser(@QueryMap params: JsonApiParams = JsonApiParams()): Response<JsonApiObject<User>>

    @PATCH(PATH_USER)
    suspend fun updateUser(@Body user: JsonApiRetrofitObject<User>): Response<JsonApiObject<User>>
}

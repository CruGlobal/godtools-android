package org.cru.godtools.api

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.model.User
import retrofit2.Response
import retrofit2.http.GET

internal const val PATH_USER = "users/me"

interface UserApi {
    @GET(PATH_USER)
    suspend fun getUser(): Response<JsonApiObject<User>>
}

package org.cru.godtools.api

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.api.model.AuthToken
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

private const val PATH_AUTHENTICATE = "auth"

interface AuthApi {
    @POST(PATH_AUTHENTICATE)
    suspend fun authenticate(@Body request: AuthToken.Request): Response<JsonApiObject<AuthToken>>
}

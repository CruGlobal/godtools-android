package org.cru.godtools.api

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.api.model.AuthToken
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

private const val PATH_AUTHENTICATE = "auth"

interface AuthApi {
    @POST(PATH_AUTHENTICATE)
    fun authenticate(@Body request: AuthToken.Request): Call<JsonApiObject<AuthToken>>
}

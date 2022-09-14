package org.cru.godtools.api

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.model.UserCounter
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

private const val PATH_USER_COUNTERS = "users/me/counters"

private const val PARAM_COUNTER_ID = "counter_id"

interface UserCountersApi {
    @GET(PATH_USER_COUNTERS)
    suspend fun getCounters(): Response<JsonApiObject<UserCounter>>

    @PATCH("$PATH_USER_COUNTERS/{$PARAM_COUNTER_ID}")
    suspend fun updateCounter(
        @Path(PARAM_COUNTER_ID) counterId: String,
        @Body counter: UserCounter
    ): Response<JsonApiObject<UserCounter>>
}

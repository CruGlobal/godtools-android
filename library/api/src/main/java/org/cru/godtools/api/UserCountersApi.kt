package org.cru.godtools.api

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.model.UserCounter
import retrofit2.Response
import retrofit2.http.GET

private const val PATH_USER_COUNTERS = "user/counters"

interface UserCountersApi {
    @GET(PATH_USER_COUNTERS)
    suspend fun getCounters(): Response<JsonApiObject<UserCounter>>
}

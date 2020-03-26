package org.cru.godtools.api

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.model.Followup
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

private const val PATH_FOLLOWUPS = "follow_ups"

interface FollowupApi {
    @POST(PATH_FOLLOWUPS)
    suspend fun subscribe(@Body followup: Followup): Response<JsonApiObject<Followup>>
}

package org.cru.godtools.api

import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CampaignFormsApi {
    @FormUrlEncoded
    @POST("forms")
    fun signup(
        @Field("id") id: String?,
        @Field("email_address") email: String?,
        @Field("first_name") firstName: String?,
        @Field("last_name") lastName: String?
    ): Call<JSONObject>
}

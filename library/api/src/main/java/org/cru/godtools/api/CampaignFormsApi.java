package org.cru.godtools.api;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface CampaignFormsApi {
    String FIELD_ID = "id";
    String FIELD_EMAIL = "email_address";
    String FIELD_FIRST_NAME = "first_name";
    String FIELD_LAST_NAME = "last_name";

    @POST("forms")
    @FormUrlEncoded
    Call<JSONObject> signup(@Field(FIELD_ID) String id,
                            @Field(FIELD_EMAIL) String email,
                            @Field(FIELD_FIRST_NAME) String firstName,
                            @Field(FIELD_LAST_NAME) String lastName);
}

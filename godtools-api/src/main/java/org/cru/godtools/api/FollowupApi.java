package org.cru.godtools.api;

import android.support.annotation.NonNull;

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.cru.godtools.model.Followup;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FollowupApi {
    String PATH_FOLLOWUPS = "follow_ups";

    @POST(PATH_FOLLOWUPS)
    Call<JsonApiObject<Followup>> create(@Body @NonNull JsonApiObject<Followup> followup);
}

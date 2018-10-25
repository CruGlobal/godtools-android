package org.cru.godtools.api;

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.cru.godtools.model.Followup;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FollowupApi {
    String PATH_FOLLOWUPS = "follow_ups";

    @POST(PATH_FOLLOWUPS)
    Call<JsonApiObject<Followup>> subscribe(@Body @NonNull Followup followup);
}

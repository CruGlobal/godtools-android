package org.cru.godtools.api;

import android.support.annotation.NonNull;

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.cru.godtools.api.model.ToolViews;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ViewsApi {
    String PATH_VIEWS = "views";

    @POST(PATH_VIEWS)
    Call<JsonApiObject<ToolViews>> submitViews(@Body @NonNull ToolViews views);
}

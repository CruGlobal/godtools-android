package org.keynote.godtools.android.api;

import android.support.annotation.NonNull;

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams;
import org.keynote.godtools.android.model.Resource;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface ResourcesApi {
    String PATH_RESOURCES = "resources";

    @GET(PATH_RESOURCES)
    Call<JsonApiObject<Resource>> list(@QueryMap @NonNull JsonApiParams params);

    @GET(PATH_RESOURCES + "/{id}")
    Call<JsonApiObject<Resource>> get(@Path("id") long id, @QueryMap @NonNull JsonApiParams params);
}

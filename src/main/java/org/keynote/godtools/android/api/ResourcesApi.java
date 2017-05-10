package org.keynote.godtools.android.api;

import android.support.annotation.NonNull;

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams;
import org.keynote.godtools.android.model.Tool;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import static org.keynote.godtools.android.BuildConfig.MOBILE_CONTENT_SYSTEM;

public interface ResourcesApi {
    String PATH_RESOURCES = "resources";
    String FILTER_SYSTEM = "filter[system]";

    @GET(PATH_RESOURCES + "?" + FILTER_SYSTEM + "=" + MOBILE_CONTENT_SYSTEM)
    Call<JsonApiObject<Tool>> list(@QueryMap @NonNull JsonApiParams params);

    @GET(PATH_RESOURCES + "/{id}")
    Call<JsonApiObject<Tool>> get(@Path("id") long id, @QueryMap @NonNull JsonApiParams params);
}

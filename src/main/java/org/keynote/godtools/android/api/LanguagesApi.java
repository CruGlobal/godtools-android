package org.keynote.godtools.android.api;

import android.support.annotation.NonNull;

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams;
import org.keynote.godtools.android.model.Language;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface LanguagesApi {
    String PATH_LANGUAGES = "languages";

    @GET(PATH_LANGUAGES)
    Call<JsonApiObject<Language>> list(@QueryMap @NonNull JsonApiParams params);
}

package org.keynote.godtools.android.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface TranslationsApi {
    String PATH_TRANSLATIONS = "translations";

    @Streaming
    @GET(PATH_TRANSLATIONS + "/{id}")
    Call<ResponseBody> download(@Path("id") long id);
}

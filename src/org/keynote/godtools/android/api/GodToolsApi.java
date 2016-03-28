package org.keynote.godtools.android.api;

import org.keynote.godtools.android.BuildConfig;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.POST;

public interface GodToolsApi {
    String AUTH = "auth";

    GodToolsApi INSTANCE = new Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL_V2)
            .build()
            .create(GodToolsApi.class);

    @POST(AUTH)
    Call<ResponseBody> getAuthToken();
}

package org.keynote.godtools.android.api;

import org.keynote.godtools.android.BuildConfig;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

public interface GodToolsApi {
    String AUTH = "auth";

    GodToolsApi INSTANCE = new Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL_V2)
            .build()
            .create(GodToolsApi.class);

    @POST(AUTH + "/{code}")
    Call<ResponseBody> getAuthToken(@Path("code") String code);

    @GET(AUTH + "/status")
    Call<ResponseBody> verifyAuthToken(@Header(AUTHORIZATION) String token);
}

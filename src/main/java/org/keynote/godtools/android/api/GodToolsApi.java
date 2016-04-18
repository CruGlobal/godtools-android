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
    String V2 = "v2";
    String AUTH = V2 + "/auth";
    String NOTIFICATION = "notification";

    GodToolsApi INSTANCE = new Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .build()
            .create(GodToolsApi.class);

    @POST(AUTH + "/{code}")
    Call<ResponseBody> getAuthToken(@Path("code") String code, @Header("interpreter") String interpreter);

    @GET(AUTH + "/status")
    Call<ResponseBody> verifyAuthToken(@Header(AUTHORIZATION) String token, @Header("interpreter") String interpreter);

    @POST(NOTIFICATION + "/{registrationId}")
    Call<ResponseBody> registerDeviceForNotifications(@Path("registrationId") String regId,
                                                      @Header("deviceId") String deviceId,
                                                      @Header("interpreter") String interpreter,
                                                      @Header("notificationsOn") boolean enableNotifications);
}

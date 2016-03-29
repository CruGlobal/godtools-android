package org.keynote.godtools.android.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by dsgoers on 3/29/16.
 */
public interface GrowthSpacesApi {
    String V1 = "v1";
    String BASE_URL = "https://www.growthspaces.org/api/";

    GrowthSpacesApi INSTANCE = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GrowthSpacesApi.class);

    @POST("/subscribers")
    Call<ResponseBody> createSubscriber(@Body Object parameters);
}

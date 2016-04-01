package org.keynote.godtools.android.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import org.ccci.gto.android.common.gson.GsonIgnoreExclusionStrategy;
import org.keynote.godtools.android.BuildConfig;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by dsgoers on 3/29/16.
 */
public interface GrowthSpacesApi {
    GrowthSpacesApi INSTANCE = new Retrofit.Builder()
            .baseUrl(BuildConfig.GROWTH_SPACES_URL)
            .addConverterFactory(
                    GsonConverterFactory.create(
                            new GsonBuilder()
                                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                    .setExclusionStrategies(new GsonIgnoreExclusionStrategy())
                                    .create()))
            .build()
            .create(GrowthSpacesApi.class);

    @POST("subscribers")
    Call<GSSubscriber> createSubscriber(@Header("Access-Id") String accessId,
                                      @Header("Access-Secret") String accessSecret, @Body GSSubscriber gsSubscriber);
}

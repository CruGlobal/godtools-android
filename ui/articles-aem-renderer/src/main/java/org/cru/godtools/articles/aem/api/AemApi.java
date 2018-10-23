package org.cru.godtools.articles.aem.api;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.api.retrofit2.converter.JSONObjectConverterFactory;
import org.cru.godtools.articles.aem.service.OkHttpClientProvider;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface AemApi {
    @GET
    Call<JSONObject> getJson(@Url String uri, @Query("_") long timestamp);

    @GET
    Call<String> downloadArticle(@Url String uri);

    @GET
    @Streaming
    Call<ResponseBody> downloadResource(@Url Uri uri);

    static AemApi buildInstance(@NonNull final String uri) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (Build.VERSION.SDK_INT <= 21) {
           builder = OkHttpClientProvider.getEnabledTLSSupportedBuilder(builder);
        }

        final OkHttpClient okHttp = builder.build();
            // create RetroFit API
            return new Retrofit.Builder().baseUrl(uri)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(new JSONObjectConverterFactory())
                    .callFactory(okHttp)
                    .build().create(AemApi.class);
    }
}

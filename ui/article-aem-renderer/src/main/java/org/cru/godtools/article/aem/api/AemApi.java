package org.cru.godtools.article.aem.api;

import android.net.Uri;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface AemApi {
    @GET
    Call<JSONObject> getJson(@Url Uri uri, @Query("_") long timestamp);

    @GET
    Call<String> downloadArticle(@Url Uri uri);

    @GET
    @Streaming
    Call<ResponseBody> downloadResource(@Url Uri uri);
}

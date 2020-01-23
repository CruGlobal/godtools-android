package org.cru.godtools.api;

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.cru.godtools.model.GlobalActivityAnalytics;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;

public interface GlobalActivityAnalyticsApi {
    String PATH_ANALYTICS_GLOBAL = "analytics/global";

    @Streaming
    @GET(PATH_ANALYTICS_GLOBAL)
    Call<JsonApiObject<GlobalActivityAnalytics>> getAnalytics();
}

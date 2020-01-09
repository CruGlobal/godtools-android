package org.cru.godtools.api;

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.cru.godtools.api.model.GlobalActivityAnalytics;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GlobalActivityAnalyticsApi {
    String PATH_GLOBAL_ACTIVITY = "/analytics/global";

    @GET(PATH_GLOBAL_ACTIVITY)
    Call<JsonApiObject<GlobalActivityAnalytics>> download();
}

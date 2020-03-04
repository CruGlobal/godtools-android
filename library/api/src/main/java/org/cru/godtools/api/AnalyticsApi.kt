package org.cru.godtools.api

import org.cru.godtools.model.GlobalActivityAnalytics
import retrofit2.Call
import retrofit2.http.GET

private const val PATH_ANALYTICS = "analytics"

interface AnalyticsApi {
    @GET("$PATH_ANALYTICS/global")
    fun getGlobalActivity(): Call<GlobalActivityAnalytics>
}

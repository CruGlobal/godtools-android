package org.cru.godtools.api

import org.cru.godtools.model.GlobalActivityAnalytics
import retrofit2.Response
import retrofit2.http.GET

private const val PATH_ANALYTICS = "analytics"

interface AnalyticsApi {
    @GET("$PATH_ANALYTICS/global")
    suspend fun getGlobalActivity(): Response<GlobalActivityAnalytics>
}

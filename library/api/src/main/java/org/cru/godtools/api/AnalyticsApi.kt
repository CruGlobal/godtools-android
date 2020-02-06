package org.cru.godtools.api

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.model.GlobalActivityAnalytics
import retrofit2.Call
import retrofit2.http.GET

private const val PATH_ANALYTICS = "analytics"

interface AnalyticsApi {
    @GET("$PATH_ANALYTICS/global")
    fun getGlobalActivity(): Call<JsonApiObject<GlobalActivityAnalytics>>
}

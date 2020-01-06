package org.cru.godtools.global.activity.analytics.manger.api

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.global.activity.analytics.manger.model.GlobalActivityAnalytics
import retrofit2.Call
import retrofit2.http.GET

const val PATH_GLOBAL_ACTIVITY = "/analytics/global"

interface GlobalActivityAnalyticsApi {
    @GET(PATH_GLOBAL_ACTIVITY)
    fun download(): Call<JsonApiObject<GlobalActivityAnalytics>>
}

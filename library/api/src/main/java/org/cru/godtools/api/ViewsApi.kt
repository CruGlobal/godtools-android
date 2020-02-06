package org.cru.godtools.api

import okhttp3.ResponseBody
import org.cru.godtools.api.model.ToolViews
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

private const val PATH_VIEWS = "views"

interface ViewsApi {
    @POST(PATH_VIEWS)
    fun submitViews(@Body views: ToolViews): Call<ResponseBody>
}

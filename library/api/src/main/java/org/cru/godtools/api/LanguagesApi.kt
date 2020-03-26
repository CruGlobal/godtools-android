package org.cru.godtools.api

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams
import org.cru.godtools.model.Language
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

private const val PATH_LANGUAGES = "languages"

interface LanguagesApi {
    @GET(PATH_LANGUAGES)
    suspend fun list(@QueryMap params: JsonApiParams): Response<JsonApiObject<Language>>
}

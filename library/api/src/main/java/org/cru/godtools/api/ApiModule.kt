package org.cru.godtools.api

import dagger.Module
import dagger.Provides
import dagger.Reusable
import javax.inject.Named

@Module
object ApiModule {
    const val MOBILE_CONTENT_API_BASE_URI = "MOBILE_CONTENT_API_BASE_URI"

    @Provides
    @Reusable
    fun godToolsApi(@Named(MOBILE_CONTENT_API_BASE_URI) baseUri: String) = GodToolsApi(baseUri)

    @Provides
    @Reusable
    fun analyticsApi(godToolsApi: GodToolsApi) = godToolsApi.analytics

    @Provides
    @Reusable
    fun followupApi(godToolsApi: GodToolsApi) = godToolsApi.followups

    @Provides
    @Reusable
    fun languagesApi(godToolsApi: GodToolsApi) = godToolsApi.languages

    @Provides
    @Reusable
    fun toolsApi(godToolsApi: GodToolsApi) = godToolsApi.tools

    @Provides
    @Reusable
    fun viewsApi(godToolsApi: GodToolsApi) = godToolsApi.views
}

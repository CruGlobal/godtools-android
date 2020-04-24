package org.cru.godtools.api

import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module
abstract class ApiModule {
    companion object {
        @Provides
        @Reusable
        fun godToolsApi() = GodToolsApi.getInstance()

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
}

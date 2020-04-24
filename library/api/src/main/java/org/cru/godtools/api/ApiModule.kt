package org.cru.godtools.api

import dagger.Module
import dagger.Provides

@Module
abstract class ApiModule {
    companion object {
        @Provides
        fun godToolsApi() = GodToolsApi.getInstance()
    }
}

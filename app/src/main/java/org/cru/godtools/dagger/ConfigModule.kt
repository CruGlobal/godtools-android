package org.cru.godtools.dagger

import dagger.Module
import dagger.Provides
import org.cru.godtools.BuildConfig
import org.cru.godtools.api.ApiModule
import javax.inject.Named

@Module
class ConfigModule {
    @get:Provides
    @get:Named(ApiModule.MOBILE_CONTENT_API_BASE_URI)
    val mobileContentApiBaseUri = BuildConfig.MOBILE_CONTENT_API
}

package org.cru.godtools.dagger

import dagger.Module
import dagger.Provides
import org.cru.godtools.BuildConfig
import org.cru.godtools.api.ApiModule
import javax.inject.Named

@Module
class ConfigModule {
    @get:Provides
    @get:Named(ApiModule.MOBILE_CONTENT_API_URL)
    val mobileContentApiBaseUrl = BuildConfig.MOBILE_CONTENT_API
}

package org.cru.godtools.dagger

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.cru.godtools.BuildConfig
import org.cru.godtools.api.ApiConfig

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    @get:Provides
    val apiConfig = ApiConfig(
        mobileContentApiUrl = BuildConfig.MOBILE_CONTENT_API,
        cdnUrl = BuildConfig.MOBILE_CONTENT_CDN
    )
}

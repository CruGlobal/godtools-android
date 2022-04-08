package org.cru.godtools.dagger

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import org.cru.godtools.BuildConfig
import org.cru.godtools.api.ApiModule
import org.cru.godtools.base.DAGGER_HOST_CUSTOM_URI

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    @get:Provides
    @get:Named(ApiModule.MOBILE_CONTENT_API_URL)
    val mobileContentApiBaseUrl = BuildConfig.MOBILE_CONTENT_API

    @get:Provides
    @get:Named(DAGGER_HOST_CUSTOM_URI)
    val godtoolsCustomUriHost = BuildConfig.HOST_GODTOOLS_CUSTOM_URI
}

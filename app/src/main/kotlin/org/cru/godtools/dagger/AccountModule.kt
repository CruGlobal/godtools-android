package org.cru.godtools.dagger

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.cru.godtools.BuildConfig
import org.cru.godtools.account.provider.google.GoogleBuildConfig

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {
    @Provides
    @Reusable
    fun googleBuildConfig() = GoogleBuildConfig(
        serverClientId = BuildConfig.GOOGLE_SERVER_CLIENT_ID
    )
}

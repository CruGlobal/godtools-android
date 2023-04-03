package org.cru.godtools.dagger

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.cru.godtools.BuildConfig
import org.cru.godtools.account.provider.google.GoogleBuildConfig
import org.cru.godtools.account.provider.okta.OktaBuildConfig

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {
    @Provides
    @Reusable
    fun googleBuildConfig() = GoogleBuildConfig(
        serverClientId = BuildConfig.GOOGLE_SERVER_CLIENT_ID
    )

    @Provides
    @Reusable
    fun oktaBuildConfig() = OktaBuildConfig(
        clientId = BuildConfig.OKTA_CLIENT_ID,
        discoveryUrl = "${BuildConfig.OKTA_DISCOVERY_URI}/.well-known/openid-configuration".toHttpUrl(),
        appUriScheme = BuildConfig.OKTA_AUTH_SCHEME
    )
}

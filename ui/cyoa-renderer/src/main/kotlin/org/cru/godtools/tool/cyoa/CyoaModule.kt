package org.cru.godtools.tool.cyoa

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.cru.godtools.analytics.appsflyer.AppsFlyerDeepLinkResolver
import org.cru.godtools.tool.cyoa.analytics.appsflyer.CyoaAppsFlyerDeepLinkResolver

@Module
@InstallIn(SingletonComponent::class)
object CyoaModule {
    @IntoSet
    @Provides
    @Reusable
    fun cyoaAppsFlyerDeepLinkResolver(): AppsFlyerDeepLinkResolver = CyoaAppsFlyerDeepLinkResolver
}

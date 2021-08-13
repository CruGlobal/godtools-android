package org.cru.godtools.ui.dashboard

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.cru.godtools.analytics.appsflyer.AppsFlyerDeepLinkResolver

@Module
@InstallIn(SingletonComponent::class)
object DashboardModule {
    @IntoSet
    @Provides
    @Reusable
    fun dashboardAppsFlyerDeepLinkResolver(): AppsFlyerDeepLinkResolver = DashboardAppsFlyerDeepLinkResolver
}

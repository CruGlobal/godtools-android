package org.cru.godtools.analytics.appsflyer

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import org.ccci.gto.android.common.dagger.eager.EagerSingleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppsFlyerModule {
    @Binds
    @IntoSet
    @EagerSingleton(threadMode = EagerSingleton.ThreadMode.MAIN)
    abstract fun AppsFlyerAnalyticsService.eagerSingleton(): Any

    @Multibinds
    abstract fun deepLinkResolvers(): Set<AppsFlyerDeepLinkResolver>
}

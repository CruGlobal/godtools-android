package org.cru.godtools.analytics

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DebugAnalyticsModule {
    @Binds
    @IntoSet
    @EagerSingleton(threadMode = EagerSingleton.ThreadMode.ASYNC)
    abstract fun timberAnalyticsServiceEagerSingleton(timberAnalyticsService: TimberAnalyticsService): Any
}

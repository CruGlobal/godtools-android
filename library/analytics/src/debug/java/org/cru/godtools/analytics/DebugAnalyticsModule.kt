package org.cru.godtools.analytics

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton

@Module
abstract class DebugAnalyticsModule {
    @Binds
    @IntoSet
    @EagerSingleton(threadMode = EagerSingleton.ThreadMode.BACKGROUND)
    abstract fun timberAnalyticsServiceEagerSingleton(timberAnalyticsService: TimberAnalyticsService): Any
}

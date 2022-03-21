package org.cru.godtools.base.tool

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.cru.godtools.base.tool.service.TimberContentEventLogger

@Module
@InstallIn(SingletonComponent::class)
abstract class DebugBaseToolModule {
    @Binds
    @IntoSet
    @EagerSingleton(threadMode = EagerSingleton.ThreadMode.ASYNC)
    abstract fun timberAnalyticsServiceEagerSingleton(contentEventLogger: TimberContentEventLogger): Any
}

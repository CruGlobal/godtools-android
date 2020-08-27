package org.cru.godtools.download.manager

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.greenrobot.eventbus.meta.SubscriberInfoIndex

@Module
@InstallIn(SingletonComponent::class)
abstract class DownloadManagerModule {
    @Binds
    @IntoSet
    @EagerSingleton(on = EagerSingleton.LifecycleEvent.ACTIVITY_CREATED, threadMode = EagerSingleton.ThreadMode.ASYNC)
    abstract fun downloadManagerEagerSingleton(downloadManager: GodToolsDownloadManager): Any

    companion object {
        @IntoSet
        @Provides
        @Reusable
        fun downloadManagerEventBusIndex(): SubscriberInfoIndex = DownloadManagerEventBusIndex()
    }
}

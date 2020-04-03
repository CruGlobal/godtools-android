package org.cru.godtools.download.manager

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton

@Module
abstract class DownloadManagerModule {
    @Binds
    @IntoSet
    @EagerSingleton(threadMode = EagerSingleton.ThreadMode.BACKGROUND)
    abstract fun downloadManagerEagerSingleton(downloadManager: GodToolsDownloadManager): Any

    companion object {
        // TODO: make GodToolsDownloadManager a standalone singleton
        @Provides
        fun downloadManager(context: Context) = GodToolsDownloadManager.getInstance(context)
    }
}

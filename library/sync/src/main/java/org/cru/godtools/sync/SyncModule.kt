package org.cru.godtools.sync

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.Multibinds
import org.ccci.gto.android.common.dagger.workmanager.AssistedWorkerFactory
import org.ccci.gto.android.common.dagger.workmanager.WorkerKey
import org.cru.godtools.sync.task.BaseSyncTasks
import org.cru.godtools.sync.work.SyncFollowupWorker
import org.cru.godtools.sync.work.SyncToolSharesWorker

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    @Multibinds
    abstract fun syncTasks(): Map<Class<out BaseSyncTasks>, BaseSyncTasks>

    @Binds
    @IntoMap
    @WorkerKey(SyncFollowupWorker::class)
    abstract fun syncFollowupWorker(factory: SyncFollowupWorker.Factory): AssistedWorkerFactory<SyncFollowupWorker>

    @Binds
    @IntoMap
    @WorkerKey(SyncToolSharesWorker::class)
    abstract fun syncToolSharesWorker(factory: SyncToolSharesWorker.Factory):
        AssistedWorkerFactory<SyncToolSharesWorker>
}

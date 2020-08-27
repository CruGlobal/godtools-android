package org.cru.godtools.sync

import com.squareup.inject.assisted.dagger2.AssistedModule
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
@AssistedModule
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    @Multibinds
    abstract fun syncTasks(): Map<Class<out BaseSyncTasks>, BaseSyncTasks>

    @Binds
    @IntoMap
    @WorkerKey(SyncFollowupWorker::class)
    abstract fun syncFollowupWorker(syncFollowupWorker: SyncFollowupWorker.Factory): AssistedWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncToolSharesWorker::class)
    abstract fun syncToolSharesWorker(syncFollowupWorker: SyncToolSharesWorker.Factory): AssistedWorkerFactory
}

package org.cru.godtools.sync

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import org.cru.godtools.sync.task.BaseSyncTasks

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    @Multibinds
    abstract fun syncTasks(): Map<Class<out BaseSyncTasks>, BaseSyncTasks>
}

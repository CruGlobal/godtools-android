package org.cru.godtools.sync

import dagger.Module
import dagger.multibindings.Multibinds
import org.cru.godtools.sync.task.BaseSyncTasks
import org.cru.godtools.sync.task.SyncTaskModule

@Module(includes = [SyncTaskModule::class])
abstract class SyncModule {
    @Multibinds
    abstract fun syncTasks(): Map<Class<out BaseSyncTasks>, BaseSyncTasks>
}

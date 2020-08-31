package org.cru.godtools.sync.task

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@MapKey
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SyncTaskKey(val value: KClass<out BaseSyncTasks>)

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncTaskModule {
    @Binds
    @IntoMap
    @SyncTaskKey(AnalyticsSyncTasks::class)
    abstract fun analyticsSyncTasks(tasks: AnalyticsSyncTasks): BaseSyncTasks

    @Binds
    @IntoMap
    @SyncTaskKey(FollowupSyncTasks::class)
    abstract fun followupSyncTasks(tasks: FollowupSyncTasks): BaseSyncTasks

    @Binds
    @IntoMap
    @SyncTaskKey(LanguagesSyncTasks::class)
    abstract fun languagesSyncTasks(tasks: LanguagesSyncTasks): BaseSyncTasks

    @Binds
    @IntoMap
    @SyncTaskKey(ToolSyncTasks::class)
    abstract fun toolSyncTasks(tasks: ToolSyncTasks): BaseSyncTasks
}

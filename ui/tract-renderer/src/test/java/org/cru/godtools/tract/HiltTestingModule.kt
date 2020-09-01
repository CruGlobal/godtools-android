package org.cru.godtools.tract

import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ccci.gto.android.common.scarlet.ReferenceLifecycle
import org.cru.godtools.api.TractShareService
import org.cru.godtools.base.Settings
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.sync.task.ToolSyncTasks
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.GodToolsDao

@Module
@InstallIn(SingletonComponent::class)
class ExternalSingletonsModule {
    @get:Provides
    val dao by lazy { mock<GodToolsDao>() }
    @get:Provides
    val downloadManager by lazy { mock<GodToolsDownloadManager>() }
    @get:Provides
    val eventBus by lazy { mock<EventBus>() }
    @get:Provides
    val referenceLifecycle by lazy { mock<ReferenceLifecycle>() }
    @get:Provides
    val settings by lazy { mock<Settings>() }
    @get:Provides
    val syncService by lazy { mock<GodToolsSyncService>() }
    @get:Provides
    val toolSyncTasks by lazy { mock<ToolSyncTasks>() }
    @get:Provides
    val tractShareService by lazy { mock<TractShareService>() }
}

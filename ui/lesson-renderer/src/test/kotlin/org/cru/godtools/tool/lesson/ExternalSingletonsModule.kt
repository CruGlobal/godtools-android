package org.cru.godtools.tool.lesson

import androidx.lifecycle.MutableLiveData
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.cru.godtools.base.Settings
import org.cru.godtools.download.manager.DownloadManagerModule
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.sync.task.SyncTaskModule
import org.cru.godtools.sync.task.ToolSyncTasks
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.GodToolsDao
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        DownloadManagerModule::class,
        SyncTaskModule::class
    ]
)
class ExternalSingletonsModule {
    @get:Provides
    val dao by lazy {
        mock<GodToolsDao> {
            on { getLatestTranslationLiveData(any(), any(), any(), any(), any()) } doAnswer { MutableLiveData(null) }
        }
    }
    @get:Provides
    val downloadManager by lazy { mock<GodToolsDownloadManager>() }
    @get:Provides
    val eventBus by lazy { mock<EventBus>() }
    @get:Provides
    val picasso by lazy { mock<Picasso>(defaultAnswer = RETURNS_DEEP_STUBS) }
    @get:Provides
    val settings by lazy { mock<Settings>() }
    @get:Provides
    val toolSyncTasks by lazy { mock<ToolSyncTasks>() }
}

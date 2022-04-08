package org.cru.godtools.article

import androidx.lifecycle.MutableLiveData
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.every
import io.mockk.mockk
import javax.inject.Named
import kotlinx.coroutines.Job
import org.cru.godtools.base.DAGGER_HOST_CUSTOM_URI
import org.cru.godtools.base.Settings
import org.cru.godtools.download.manager.DownloadManagerModule
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.sync.task.SyncTaskModule
import org.cru.godtools.sync.task.ToolSyncTasks
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.GodToolsDao

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
    @get:Named(DAGGER_HOST_CUSTOM_URI)
    val hostCustomUri = "org.cru.godtools.test"

    @get:Provides
    val dao by lazy {
        mockk<GodToolsDao> {
            every { getLatestTranslationLiveData(any(), any(), any(), any(), any()) } answers { MutableLiveData(null) }
            every { updateSharesDeltaAsync(any(), any()) } returns Job().apply { complete() }
        }
    }
    @get:Provides
    val downloadManager by lazy {
        mockk<GodToolsDownloadManager> {
            every { getDownloadProgressLiveData(any(), any()) } answers { MutableLiveData() }
        }
    }
    @get:Provides
    val eventBus by lazy { mockk<EventBus>(relaxUnitFun = true) }
    @get:Provides
    val picasso by lazy { mockk<Picasso>() }
    @get:Provides
    val settings by lazy {
        mockk<Settings>(relaxUnitFun = true) {
            every { isFeatureDiscovered(any()) } returns false
        }
    }
    @get:Provides
    val toolSyncTasks by lazy { mockk<ToolSyncTasks>() }
}

package org.cru.godtools.tool.article

import androidx.lifecycle.MutableLiveData
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import org.cru.godtools.base.Settings
import org.cru.godtools.base.tool.service.FollowupService
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.DownloadManagerModule
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.user.activity.UserActivityManager
import org.greenrobot.eventbus.EventBus

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        DownloadManagerModule::class,
    ]
)
class ExternalSingletonsModule {
    @get:Provides
    val downloadManager by lazy {
        mockk<GodToolsDownloadManager> {
            every { downloadLatestPublishedTranslationAsync(any(), any()) } returns CompletableDeferred(true)
        }
    }
    @get:Provides
    val eventBus by lazy { mockk<EventBus>(relaxUnitFun = true) }
    @get:Provides
    val followupService: FollowupService by lazy { mockk() }
    @get:Provides
    val manifestManager by lazy {
        mockk<ManifestManager> {
            every { getLatestPublishedManifestLiveData(any(), any()) } answers { MutableLiveData() }
        }
    }
    @get:Provides
    val picasso by lazy { mockk<Picasso>() }
    @get:Provides
    val settings by lazy {
        mockk<Settings>(relaxUnitFun = true) {
            every { isFeatureDiscovered(any()) } returns false
        }
    }
    @get:Provides
    val syncService by lazy {
        mockk<GodToolsSyncService> {
            coEvery { syncTool(any(), any()) } returns true
        }
    }
    @get:Provides
    val userActivityManager: UserActivityManager by lazy { mockk() }
}

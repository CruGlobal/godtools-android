package org.cru.godtools.tract

import androidx.lifecycle.MutableLiveData
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.ccci.gto.android.common.db.findAsFlow
import org.ccci.gto.android.common.scarlet.ReferenceLifecycle
import org.cru.godtools.analytics.AnalyticsModule
import org.cru.godtools.api.ApiModule
import org.cru.godtools.api.TractShareService
import org.cru.godtools.base.Settings
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.DownloadManagerModule
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Tool
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.sync.SyncModule
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
        AnalyticsModule::class,
        ApiModule::class,
        DownloadManagerModule::class,
        SyncModule::class,
        SyncTaskModule::class
    ]
)
class ExternalSingletonsModule {
    @get:Provides
    val dao by lazy {
        mockk<GodToolsDao> {
            every { findAsFlow<Tool>(any<String>()) } returns flowOf(null)
            every { getLatestTranslationLiveData(any(), any(), any(), any(), any()) } answers { MutableLiveData(null) }
            every { updateSharesDeltaAsync(any(), any()) } returns mockk()
        }
    }
    @get:Provides
    val downloadManager by lazy {
        mock<GodToolsDownloadManager> {
            on { getDownloadProgressLiveData(any(), any()) } doAnswer { ImmutableLiveData(null) }
        }
    }
    @get:Provides
    val eventBus by lazy { mock<EventBus>() }
    @get:Provides
    val manifestManager by lazy {
        mock<ManifestManager> {
            on { getLatestPublishedManifestLiveData(any(), any()) } doAnswer { ImmutableLiveData(null) }
        }
    }
    @get:Provides
    val picasso by lazy { mock<Picasso>(defaultAnswer = RETURNS_DEEP_STUBS) }
    @get:Provides
    val referenceLifecycle by lazy { mock<ReferenceLifecycle>() }
    @get:Provides
    val settings by lazy {
        mock<Settings> {
            on { isFeatureDiscoveredLiveData(any()) } doAnswer { ImmutableLiveData(true) }
        }
    }
    @get:Provides
    val syncService by lazy { mock<GodToolsSyncService>(defaultAnswer = RETURNS_DEEP_STUBS) }
    @get:Provides
    val toolSyncTasks by lazy { mock<ToolSyncTasks>() }
    @get:Provides
    val tractShareService by lazy { mock<TractShareService>() }
}

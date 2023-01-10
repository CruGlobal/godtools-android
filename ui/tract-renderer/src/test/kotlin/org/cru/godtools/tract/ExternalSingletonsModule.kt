package org.cru.godtools.tract

import androidx.lifecycle.MutableLiveData
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
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
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.repository.TranslationsRepository

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        AnalyticsModule::class,
        ApiModule::class,
        DownloadManagerModule::class,
        SyncModule::class,
    ]
)
class ExternalSingletonsModule {
    @get:Provides
    val dao by lazy {
        mockk<GodToolsDao> {
            every { findAsFlow<Tool>(any<String>()) } returns flowOf(null)
            every { updateSharesDeltaAsync(any(), any()) } returns mockk()
        }
    }
    @get:Provides
    val downloadManager: GodToolsDownloadManager by lazy { mockk() }
    @get:Provides
    val eventBus by lazy { mockk<EventBus>(relaxUnitFun = true) }
    @get:Provides
    val manifestManager by lazy {
        mockk<ManifestManager> {
            every { getLatestPublishedManifestLiveData(any(), any()) } answers { ImmutableLiveData(null) }
        }
    }
    @get:Provides
    val picasso by lazy { mockk<Picasso>(relaxed = true) }
    @get:Provides
    val referenceLifecycle = ReferenceLifecycle()
    @get:Provides
    val settings by lazy {
        mockk<Settings> {
            every { setFeatureDiscovered(any()) } just Runs
            every { isFeatureDiscovered(any()) } returns true
            every { isFeatureDiscoveredLiveData(any()) } answers { ImmutableLiveData(true) }
        }
    }
    @get:Provides
    val syncService by lazy { mockk<GodToolsSyncService>() }
    @get:Provides
    val tractShareService by lazy { mockk<TractShareService>() }
    @get:Provides
    val translationsRepository by lazy {
        mockk<TranslationsRepository> {
            every { getLatestTranslationLiveData(any(), any(), any(), any()) } answers { MutableLiveData(null) }
        }
    }
}

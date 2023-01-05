package org.cru.godtools.article

import androidx.lifecycle.MutableLiveData
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import javax.inject.Named
import kotlinx.coroutines.Job
import org.cru.godtools.base.DAGGER_HOST_CUSTOM_URI
import org.cru.godtools.base.Settings
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.DownloadManagerModule
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.sync.GodToolsSyncService
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.repository.TranslationsRepository

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        DownloadManagerModule::class,
    ]
)
class ExternalSingletonsModule {
    @get:Provides
    @get:Named(DAGGER_HOST_CUSTOM_URI)
    val hostCustomUri = "org.cru.godtools.test"

    @get:Provides
    val dao by lazy {
        mockk<GodToolsDao> {
            every { updateSharesDeltaAsync(any(), any()) } returns Job().apply { complete() }
        }
    }
    @get:Provides
    val downloadManager by lazy {
        mockk<GodToolsDownloadManager> {
            every { downloadLatestPublishedTranslationAsync(any(), any()) } returns Job().apply { complete() }
        }
    }
    @get:Provides
    val eventBus by lazy { mockk<EventBus>(relaxUnitFun = true) }
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
    val translationsRepository by lazy {
        mockk<TranslationsRepository> {
            every { getLatestTranslationLiveData(any(), any(), any(), any()) } answers { MutableLiveData(null) }
        }
    }
}

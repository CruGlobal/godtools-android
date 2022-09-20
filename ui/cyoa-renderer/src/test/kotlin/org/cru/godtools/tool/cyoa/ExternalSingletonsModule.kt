package org.cru.godtools.tool.cyoa

import androidx.lifecycle.MutableLiveData
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.every
import io.mockk.mockk
import javax.inject.Named
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.cru.godtools.analytics.AnalyticsModule
import org.cru.godtools.base.DAGGER_HOST_CUSTOM_URI
import org.cru.godtools.base.Settings
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.sync.GodToolsSyncService
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.repository.TranslationsRepository
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AnalyticsModule::class]
)
class ExternalSingletonsModule {
    @get:Provides
    @get:Named(DAGGER_HOST_CUSTOM_URI)
    val hostCustomUri = "org.cru.godtools.test"

    @get:Provides
    val dao by lazy { mock<GodToolsDao>() }
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
    val settings by lazy { mock<Settings>() }
    @get:Provides
    val syncService by lazy { mock<GodToolsSyncService>() }
    @get:Provides
    val translationsRepository by lazy {
        mockk<TranslationsRepository> {
            every { getLatestTranslationLiveData(any(), any(), any(), any()) } answers { MutableLiveData(null) }
        }
    }
}

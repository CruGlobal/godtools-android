package org.cru.godtools.tool.cyoa

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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.ccci.gto.android.common.db.Query
import org.cru.godtools.analytics.AnalyticsModule
import org.cru.godtools.base.Settings
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.user.activity.UserActivityManager
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.repository.TranslationsRepository

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AnalyticsModule::class]
)
class ExternalSingletonsModule {
    @get:Provides
    val dao by lazy {
        mockk<GodToolsDao> {
            every { findAsFlow(Tool::class.java, any()) } returns flowOf(null)
            every { getLiveData(any<Query<Language>>()) } returns MutableLiveData()
            every { updateSharesDeltaAsync(any(), any()) } returns Job().apply { complete() }
        }
    }
    @get:Provides
    val downloadManager by lazy {
        mockk<GodToolsDownloadManager> {
            every { getDownloadProgressFlow(any(), any()) } returns flowOf(null)
            every { downloadLatestPublishedTranslationAsync(any(), any()) } returns CompletableDeferred(true)
        }
    }
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
    val settings by lazy {
        mockk<Settings>(relaxUnitFun = true) {
            every { isFeatureDiscovered(any()) } returns true
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
    @get:Provides
    val userActivityManager: UserActivityManager by lazy { mockk(relaxUnitFun = true) }
}

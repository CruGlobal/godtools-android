package org.cru.godtools

import androidx.work.WorkManager
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import org.ccci.gto.android.common.dagger.eager.EagerModule
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.analytics.AnalyticsModule
import org.cru.godtools.dagger.EventBusModule
import org.cru.godtools.dagger.FlipperModule
import org.cru.godtools.dagger.ServicesModule
import org.cru.godtools.db.DatabaseModule
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.DownloadedFilesRepository
import org.cru.godtools.db.repository.FollowupsRepository
import org.cru.godtools.db.repository.GlobalActivityRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.LastSyncTimeRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TrainingTipsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.repository.UserCountersRepository
import org.cru.godtools.db.repository.UserRepository
import org.cru.godtools.downloadmanager.DownloadManagerModule
import org.cru.godtools.downloadmanager.GodToolsDownloadManager
import org.cru.godtools.sync.GodToolsSyncService
import org.greenrobot.eventbus.EventBus

@Module(includes = [EagerModule::class])
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        AnalyticsModule::class,
        DatabaseModule::class,
        DownloadManagerModule::class,
        EventBusModule::class,
        FlipperModule::class,
        ServicesModule::class,
    ]
)
class ExternalSingletonsModule {
    @get:Provides
    val accountManager by lazy {
        mockk<GodToolsAccountManager> {
            every { isAuthenticatedFlow } returns flowOf(false)
            every { prepareForLogin(any()) } returns mockk()
        }
    }
    @get:Provides
    val coroutineScope: CoroutineScope by lazy { TestScope() }
    @get:Provides
    val eventbus by lazy { mockk<EventBus>(relaxUnitFun = true) }
    @get:Provides
    val picasso by lazy { mockk<Picasso>() }
    @get:Provides
    val syncService: GodToolsSyncService by lazy {
        mockk {
            coEvery { syncTools(any()) } returns true
            coEvery { syncFavoriteTools(any()) } returns true
            every { syncFollowupsAsync() } returns CompletableDeferred(true)
            every { syncToolSharesAsync() } returns CompletableDeferred(true)
        }
    }
    @get:Provides
    val workManager by lazy { mockk<WorkManager>() }

    // region DatabaseModule
    @get:Provides
    val attachmentsRepository: AttachmentsRepository by lazy { mockk() }
    @get:Provides
    val downloadedFilesRepository: DownloadedFilesRepository by lazy { mockk() }
    @get:Provides
    val followupsRepository: FollowupsRepository by lazy { mockk() }
    @get:Provides
    val globalActivityRepository: GlobalActivityRepository by lazy { mockk() }
    @get:Provides
    val languagesRepository: LanguagesRepository by lazy {
        mockk {
            every { getLanguagesFlow() } returns flowOf(emptyList())
        }
    }
    @get:Provides
    val lastSyncTimeRepository: LastSyncTimeRepository by lazy { mockk() }
    @get:Provides
    val toolsRepository: ToolsRepository by lazy {
        mockk {
            every { getFavoriteToolsFlow() } returns flowOf(emptyList())
            every { getLessonsFlow() } returns flowOf(emptyList())
            every { getNormalToolsFlow() } returns flowOf(emptyList())
            every { getMetaToolsFlow() } returns flowOf(emptyList())
        }
    }
    @get:Provides
    val trainingTipsRepository: TrainingTipsRepository by lazy { mockk() }
    @get:Provides
    val translationsRepository: TranslationsRepository by lazy { mockk() }
    @get:Provides
    val userRepository: UserRepository by lazy { mockk() }
    @get:Provides
    val userCountersRepository: UserCountersRepository by lazy { mockk() }
    // endregion DatabaseModule

    // region DownloadManagerModule
    @get:Provides
    val downloadManager: GodToolsDownloadManager by lazy { mockk() }
    // endregion DownloadManagerModule
}

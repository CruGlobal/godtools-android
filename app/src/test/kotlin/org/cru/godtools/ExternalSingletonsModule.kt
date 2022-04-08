package org.cru.godtools

import androidx.work.WorkManager
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.every
import io.mockk.mockk
import org.ccci.gto.android.common.dagger.eager.EagerModule
import org.ccci.gto.android.common.sync.SyncRegistry
import org.ccci.gto.android.common.sync.SyncTask
import org.cru.godtools.dagger.EventBusModule
import org.cru.godtools.dagger.ServicesModule
import org.cru.godtools.sync.GodToolsSyncService
import org.greenrobot.eventbus.EventBus
import org.mockito.Mockito
import org.mockito.kotlin.mock

@Module(includes = [EagerModule::class])
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [EventBusModule::class, ServicesModule::class]
)
class ExternalSingletonsModule {
    @get:Provides
    val eventbus by lazy { mockk<EventBus>(relaxUnitFun = true) }
    @get:Provides
    val picasso by lazy { mock<Picasso>(defaultAnswer = Mockito.RETURNS_DEEP_STUBS) }
    @get:Provides
    val syncService by lazy {
        val completedSyncTask = object : SyncTask {
            override fun sync(): Int {
                val id = SyncRegistry.startSync()
                SyncRegistry.finishSync(id)
                return id
            }
        }

        mockk<GodToolsSyncService> {
            every { syncFollowups() } returns completedSyncTask
            every { syncTools(any()) } returns completedSyncTask
            every { syncToolShares() } returns completedSyncTask
        }
    }
    @get:Provides
    val workManager by lazy { mock<WorkManager>() }
}

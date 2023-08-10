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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import org.ccci.gto.android.common.dagger.eager.EagerModule
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.analytics.AnalyticsModule
import org.cru.godtools.dagger.EventBusModule
import org.cru.godtools.dagger.FlipperModule
import org.cru.godtools.dagger.ServicesModule
import org.cru.godtools.sync.GodToolsSyncService
import org.greenrobot.eventbus.EventBus

@Module(includes = [EagerModule::class])
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        AnalyticsModule::class,
        EventBusModule::class,
        FlipperModule::class,
        ServicesModule::class,
    ]
)
class ExternalSingletonsModule {
    @get:Provides
    val accountManager by lazy {
        mockk<GodToolsAccountManager> {
            every { isAuthenticatedFlow() } returns flowOf(false)
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
        }
    }
    @get:Provides
    val workManager by lazy { mockk<WorkManager>() }
}

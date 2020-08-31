package org.cru.godtools.dagger

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton
import me.thekey.android.TheKey
import me.thekey.android.core.TheKeyImpl
import me.thekey.android.eventbus.EventBusEventsManager
import org.ccci.gto.android.common.androidx.work.TimberLogger
import org.ccci.gto.android.common.dagger.eager.EagerModule
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.dagger.workmanager.DaggerWorkerFactory
import org.ccci.gto.android.common.dagger.workmanager.WorkManagerModule
import org.cru.godtools.account.BuildConfig
import org.cru.godtools.service.AccountListRegistrationService
import org.greenrobot.eventbus.EventBus

@Module(
    includes = [
        EagerModule::class,
        WorkManagerModule::class
    ]
)
@InstallIn(SingletonComponent::class)
abstract class ServicesModule {
    @Binds
    @IntoSet
    @EagerSingleton(threadMode = EagerSingleton.ThreadMode.ASYNC)
    abstract fun eagerAccountListRegistrationService(service: AccountListRegistrationService): Any

    @Binds
    @IntoSet
    @EagerSingleton(threadMode = EagerSingleton.ThreadMode.MAIN)
    abstract fun eagerWorkManager(workManager: WorkManager): Any

    companion object {
        @Provides
        @Singleton
        fun theKey(@ApplicationContext context: Context, eventBus: EventBus): TheKey {
            TheKeyImpl.configure(
                TheKeyImpl.Configuration.base()
                    .accountType(BuildConfig.ACCOUNT_TYPE)
                    .clientId(BuildConfig.THEKEY_CLIENTID)
                    .service(EventBusEventsManager(eventBus))
            )
            return TheKey.getInstance(context)
        }

        @Provides
        @Singleton
        fun workManager(@ApplicationContext context: Context, workerFactory: DaggerWorkerFactory): WorkManager {
            WorkManager.initialize(context, Configuration.Builder().setWorkerFactory(workerFactory).build())
            TimberLogger(Log.ERROR).install()
            return WorkManager.getInstance(context)
        }
    }
}

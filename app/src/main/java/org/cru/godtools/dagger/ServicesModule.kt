package org.cru.godtools.dagger

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import javax.inject.Singleton
import me.thekey.android.TheKey
import me.thekey.android.core.TheKeyImpl
import me.thekey.android.eventbus.EventBusEventsManager
import org.ccci.gto.android.common.androidx.work.TimberLogger
import org.ccci.gto.android.common.dagger.eager.EagerModule
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelModule
import org.ccci.gto.android.common.dagger.workmanager.DaggerWorkerFactory
import org.ccci.gto.android.common.dagger.workmanager.WorkManagerModule
import org.cru.godtools.account.BuildConfig
import org.cru.godtools.api.ApiModule
import org.cru.godtools.service.AccountListRegistrationService
import org.cru.godtools.shortcuts.ShortcutModule
import org.cru.godtools.sync.SyncModule
import org.greenrobot.eventbus.EventBus

@Module(
    includes = [
        ApiModule::class,
        EagerModule::class,
        EventBusModule::class,
        ShortcutModule::class,
        SyncModule::class,
        ViewModelModule::class,
        WorkManagerModule::class
    ]
)
abstract class ServicesModule {
    // TODO: TheKey doesn't need to be Eager once TheKey is only accessed via Dagger
    @Binds
    @IntoSet
    @EagerSingleton(threadMode = EagerSingleton.ThreadMode.MAIN)
    abstract fun eagerTheKey(theKey: TheKey): Any

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
        fun theKey(context: Context, eventBus: EventBus): TheKey {
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
        fun workManager(context: Context, workerFactory: DaggerWorkerFactory): WorkManager {
            WorkManager.initialize(context, Configuration.Builder().setWorkerFactory(workerFactory).build())
            TimberLogger(Log.ERROR).install()
            return WorkManager.getInstance(context)
        }
    }
}

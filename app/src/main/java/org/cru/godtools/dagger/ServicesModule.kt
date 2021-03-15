package org.cru.godtools.dagger

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
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
import org.ccci.gto.android.common.androidx.work.TimberLogger
import org.ccci.gto.android.common.dagger.eager.EagerModule
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.dagger.splitinstall.SplitInstallModule
import org.cru.godtools.service.AccountListRegistrationService

@Module(
    includes = [
        EagerModule::class,
        SplitInstallModule::class
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
        fun workManager(@ApplicationContext context: Context, workerFactory: HiltWorkerFactory): WorkManager {
            WorkManager.initialize(context, Configuration.Builder().setWorkerFactory(workerFactory).build())
            TimberLogger(Log.ERROR).install()
            return WorkManager.getInstance(context)
        }
    }
}

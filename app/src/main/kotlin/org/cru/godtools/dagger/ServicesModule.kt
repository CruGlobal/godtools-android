package org.cru.godtools.dagger

import android.content.Context
import androidx.work.WorkManager
import com.squareup.picasso.Picasso
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.github.aakira.napier.Napier
import javax.inject.Singleton
import org.ccci.gto.android.common.dagger.eager.EagerModule
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.dagger.splitinstall.SplitInstallModule
import org.ccci.gto.android.common.napier.TimberAntilog
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

    companion object {
        @Provides
        @Singleton
        fun picasso() = Picasso.get()

        @Provides
        @IntoSet
        @Singleton
        @EagerSingleton(threadMode = EagerSingleton.ThreadMode.MAIN)
        fun napierConfig(): Any = Napier.apply { base(TimberAntilog) }

        @Provides
        @Reusable
        fun workManager(@ApplicationContext context: Context) = WorkManager.getInstance(context)
    }
}

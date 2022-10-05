package org.cru.godtools.account

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import javax.inject.Singleton
import org.cru.godtools.account.provider.AccountProvider
import org.cru.godtools.api.MobileContentApiSessionInterceptor

@Module
@InstallIn(SingletonComponent::class)
abstract class AccountModule {
    @Multibinds
    internal abstract fun accountProviders(): Set<AccountProvider>

    companion object {
        @Provides
        @Singleton
        fun mobileContentApiSessionInterceptor(
            @ApplicationContext context: Context,
            accountManager: GodToolsAccountManager
        ) = object : MobileContentApiSessionInterceptor(context) {
            override suspend fun userId() = accountManager.userId()
            override suspend fun authenticate() = accountManager.authenticateWithMobileContentApi()
        }
    }
}

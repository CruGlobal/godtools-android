package org.cru.godtools.account

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import org.cru.godtools.account.provider.AccountProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class AccountModule {
    @Multibinds
    internal abstract fun accountProviders(): Set<AccountProvider>
}

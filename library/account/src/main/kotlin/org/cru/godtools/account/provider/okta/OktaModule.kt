package org.cru.godtools.account.provider.okta

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.cru.godtools.account.provider.AccountProvider

@Module
@InstallIn(SingletonComponent::class)
internal abstract class OktaModule {
    @Binds
    @IntoSet
    abstract fun oktaAccountProvider(okta: OktaAccountProvider): AccountProvider
}

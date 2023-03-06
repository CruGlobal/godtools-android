package org.cru.godtools.account.provider.facebook

import com.facebook.AccessTokenManager
import com.facebook.login.LoginManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.cru.godtools.account.provider.AccountProvider

@Module
@InstallIn(SingletonComponent::class)
internal abstract class FacebookModule {
    @Binds
    @IntoSet
    abstract fun facebookAccountProvider(facebook: FacebookAccountProvider): AccountProvider

    companion object {
        @Provides
        @Reusable
        fun accessTokenManager() = AccessTokenManager.getInstance()

        @Provides
        @Reusable
        fun loginManager() = LoginManager.getInstance()
    }
}

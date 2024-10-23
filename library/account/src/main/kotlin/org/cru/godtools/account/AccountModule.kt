package org.cru.godtools.account

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import org.ccci.gto.android.common.kotlin.coroutines.flow.net.isConnectedFlow
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
            override fun userId() = accountManager.userId
            override suspend fun authenticate() = accountManager.authenticateWithMobileContentApi().getOrNull()
        }

        const val IS_CONNECTED_STATE_FLOW_ACCOUNTS = "STATE_FLOW_IS_CONNECTED_ACCOUNTS"

        @Provides
        @Reusable
        @Named(IS_CONNECTED_STATE_FLOW_ACCOUNTS)
        fun isConnectedStateFlow(@ApplicationContext context: Context, scope: CoroutineScope) =
            context.isConnectedFlow()
                .stateIn(scope, WhileSubscribed(5_000), false)
    }
}

package org.cru.godtools.account

import javax.inject.Inject
import javax.inject.Singleton
import org.ccci.gto.android.common.Ordered
import org.cru.godtools.account.provider.AccountProvider

@Singleton
class GodToolsAccountManager @Inject internal constructor(
    rawProviders: Set<@JvmSuppressWildcards AccountProvider>
) {
    private val providers = rawProviders.sortedWith(Ordered.COMPARATOR)

    private suspend fun activeProvider() = providers.firstOrNull { it.isAuthenticated() }

    suspend fun isAuthenticated() = activeProvider()?.isAuthenticated() ?: false
}

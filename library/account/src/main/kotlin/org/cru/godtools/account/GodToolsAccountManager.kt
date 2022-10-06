package org.cru.godtools.account

import androidx.annotation.VisibleForTesting
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import org.ccci.gto.android.common.Ordered
import org.cru.godtools.account.provider.AccountProvider

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsAccountManager @Inject internal constructor(
    rawProviders: Set<@JvmSuppressWildcards AccountProvider>
) {
    private val coroutineScope = CoroutineScope(SupervisorJob())
    private val providers = rawProviders.sortedWith(Ordered.COMPARATOR)

    @VisibleForTesting
    internal suspend fun activeProvider() = providers.firstOrNull { it.isAuthenticated() }
    @VisibleForTesting
    internal val activeProviderFlow =
        combine(providers.map { p -> p.isAuthenticatedFlow().map { p to it } }) {
            it.firstNotNullOfOrNull { (p, isAuthed) -> if (isAuthed) p else null }
        }.stateIn(coroutineScope, SharingStarted.Eagerly, null)

    suspend fun isAuthenticated() = activeProvider()?.isAuthenticated() ?: false
    fun isAuthenticatedFlow() = activeProviderFlow
        .flatMapLatest { it?.isAuthenticatedFlow() ?: flowOf(false) }
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), replay = 1)
        .distinctUntilChanged()
}

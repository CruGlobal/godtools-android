package org.cru.godtools.account

import androidx.activity.ComponentActivity
import androidx.annotation.VisibleForTesting
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.Ordered
import org.cru.godtools.account.provider.AccountProvider

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsAccountManager @VisibleForTesting internal constructor(
    @get:VisibleForTesting
    internal val providers: List<AccountProvider>,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob()),
) {
    @Inject
    internal constructor(providers: Set<@JvmSuppressWildcards AccountProvider>) :
        this(providers.sortedWith(Ordered.COMPARATOR))

    // region Active Provider
    @VisibleForTesting
    internal suspend fun activeProvider() = providers.firstOrNull { it.isAuthenticated() }

    @VisibleForTesting
    internal val activeProviderFlow =
        combine(providers.map { p -> p.isAuthenticatedFlow().map { p to it } }) {
            it.firstNotNullOfOrNull { (p, isAuthed) -> if (isAuthed) p else null }
        }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    // endregion Active Provider

    suspend fun isAuthenticated() = activeProvider()?.isAuthenticated() ?: false
    suspend fun userId() = activeProvider()?.userId()
    fun isAuthenticatedFlow() = activeProviderFlow
        .flatMapLatest { it?.isAuthenticatedFlow() ?: flowOf(false) }
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), replay = 1)
        .distinctUntilChanged()
    fun userIdFlow() = activeProviderFlow
        .flatMapLatest { it?.userIdFlow() ?: flowOf(null) }
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), replay = 1)
        .distinctUntilChanged()
    fun accountInfoFlow() = activeProviderFlow
        .flatMapLatest { it?.accountInfoFlow() ?: flowOf(null) }
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), replay = 1)
        .distinctUntilChanged()

    // region Login/Logout
    class LoginState internal constructor(internal val providerState: Map<AccountType, AccountProvider.LoginState?>)

    fun prepareForLogin(activity: ComponentActivity) =
        LoginState(providers.associate { it.type to it.prepareForLogin(activity) })
    suspend fun login(type: AccountType, state: LoginState) {
        val providerState = state.providerState[type] ?: return
        providers.first { it.type == type }.login(providerState)
    }
    suspend fun logout() = coroutineScope {
        // trigger a logout for any provider we happen to be logged into
        providers.forEach { launch { it.logout() } }
    }
    // endregion Login/Logout

    internal suspend fun authenticateWithMobileContentApi() = activeProvider()?.authenticateWithMobileContentApi()
}

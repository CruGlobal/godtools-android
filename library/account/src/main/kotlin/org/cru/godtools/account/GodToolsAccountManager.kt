package org.cru.godtools.account

import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.ActivityOptionsCompat
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
import org.cru.godtools.account.provider.AuthenticationException

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsAccountManager @VisibleForTesting internal constructor(
    @get:VisibleForTesting
    internal val providers: List<AccountProvider>,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob()),
) {
    @Inject
    internal constructor(providers: Set<@JvmSuppressWildcards AccountProvider>) :
        this(providers.sortedWith(Ordered.COMPARATOR))

    // region Active Provider
    @VisibleForTesting
    internal val activeProvider get() = providers.firstOrNull { it.isAuthenticated }

    @VisibleForTesting
    internal val activeProviderFlow =
        combine(providers.map { p -> p.isAuthenticatedFlow().map { p to it } }) {
            it.firstNotNullOfOrNull { (p, isAuthed) -> if (isAuthed) p else null }
        }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    // endregion Active Provider

    val isAuthenticated get() = activeProvider?.isAuthenticated ?: false
    val userId get() = activeProvider?.userId
    val isAuthenticatedFlow = activeProviderFlow
        .flatMapLatest { it?.isAuthenticatedFlow() ?: flowOf(false) }
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), replay = 1)
        .distinctUntilChanged()
    val authenticatedAccountTypeFlow = activeProviderFlow
        .map { it?.type }
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), replay = 1)
        .distinctUntilChanged()
    val userIdFlow = activeProviderFlow
        .flatMapLatest { it?.userIdFlow() ?: flowOf(null) }
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), replay = 1)
        .distinctUntilChanged()

    // region Login/Logout
    @Composable
    internal fun rememberLauncherForLogin(): ActivityResultLauncher<AccountType> {
        val launchers = providers.associate { it.type to it.rememberLauncherForLogin() }

        return remember(launchers) {
            object : ActivityResultLauncher<AccountType>() {
                override fun launch(input: AccountType, options: ActivityOptionsCompat?) {
                    launchers[input]?.launch(input, options)
                }

                override fun unregister() = TODO("Unsupported")
                override fun getContract() = TODO("Unsupported")
            }
        }
    }

    suspend fun logout() = coroutineScope {
        // trigger a logout for any provider we happen to be logged into
        providers.forEach { launch { it.logout() } }
    }
    // endregion Login/Logout

    internal suspend fun authenticateWithMobileContentApi() = activeProvider?.authenticateWithMobileContentApi()
        ?: Result.failure(AuthenticationException.NoActiveProvider)
}

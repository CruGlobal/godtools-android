package org.cru.godtools.account

import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.ActivityOptionsCompat
import dagger.Lazy
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.base.Ordered
import org.cru.godtools.account.AccountModule.Companion.IS_CONNECTED_STATE_FLOW_ACCOUNTS
import org.cru.godtools.account.provider.AccountProvider
import org.cru.godtools.account.provider.AuthenticationException
import org.cru.godtools.api.UserApi

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsAccountManager @VisibleForTesting internal constructor(
    @get:VisibleForTesting
    internal val providers: List<AccountProvider>,
    private val userApi: Lazy<UserApi>,
    @Named(IS_CONNECTED_STATE_FLOW_ACCOUNTS)
    private val isConnected: StateFlow<Boolean>,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob()),
) {
    @Inject
    internal constructor(
        providers: Set<@JvmSuppressWildcards AccountProvider>,
        userApi: Lazy<UserApi>,
        @Named(IS_CONNECTED_STATE_FLOW_ACCOUNTS)
        isConnected: StateFlow<Boolean>,
        coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob()),
    ) : this(providers.sortedWith(Ordered.COMPARATOR), userApi, isConnected)

    private val isConnectedFlow = isConnected.stateIn(coroutineScope, SharingStarted.Eagerly, false)

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
    internal fun rememberLauncherForLogin(
        createAccount: Boolean,
        onResponse: (LoginResponse) -> Unit,
    ): ActivityResultLauncher<AccountType> {
        val launchers = providers.associate {
            it.type to it.rememberLauncherForLogin(createAccount) { result ->
                val exceptionOrNull = result.exceptionOrNull()
                onResponse(
                    when {
                        result.isSuccess -> LoginResponse.Success
                        else -> when {
                            exceptionOrNull == AuthenticationException.UserNotFound -> {
                                LoginResponse.Error.UserNotFound
                            }

                            exceptionOrNull == AuthenticationException.UserAlreadyExists -> {
                                LoginResponse.Error.UserAlreadyExists
                            }

                            !isConnectedFlow.value -> LoginResponse.Error.NotConnected
                            else -> LoginResponse.Error()
                        }
                    }
                )
            }
        }

        return remember(launchers) {
            object : ActivityResultLauncher<AccountType>() {
                override fun launch(input: AccountType, options: ActivityOptionsCompat?) {
                    launchers[input]?.launch(input, options)
                }

                override val contract get() = TODO("Unsupported")
                override fun unregister() = TODO("Unsupported")
            }
        }
    }

    suspend fun logout() = coroutineScope {
        // trigger a logout for any provider we happen to be logged into
        providers.forEach { launch { it.logout() } }
    }

    suspend fun deleteAccount() = try {
        userApi.get().deleteUser()
            .also { if (it.isSuccessful) logout() }
            .isSuccessful
    } catch (_: IOException) {
        false
    }
    // endregion Login/Logout

    internal suspend fun authenticateWithMobileContentApi() = activeProvider?.authenticateWithMobileContentApi(false)
        ?: Result.failure(AuthenticationException.NoActiveProvider)
}

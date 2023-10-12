package org.cru.godtools.user.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.db.repository.UserRepository

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class UserManager @Inject internal constructor(
    accountManager: GodToolsAccountManager,
    private val userRepository: UserRepository
) {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    val userFlow = accountManager.userIdFlow
        .flatMapLatest { it?.let { userRepository.findUserFlow(it) } ?: flowOf(null) }
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), 1)
        .distinctUntilChanged()
}

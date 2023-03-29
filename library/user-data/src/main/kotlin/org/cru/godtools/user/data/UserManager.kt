package org.cru.godtools.user.data

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
import kotlinx.coroutines.flow.shareIn
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.account.model.AccountInfo
import org.cru.godtools.db.repository.UserRepository
import org.cru.godtools.model.User

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class UserManager @Inject internal constructor(
    accountManager: GodToolsAccountManager,
    private val userRepository: UserRepository
) {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    val userFlow = accountManager.userIdFlow()
        .flatMapLatest { it?.let { userRepository.findUserFlow(it) } ?: flowOf(null) }
        .combine(accountManager.accountInfoFlow()) { user, accountInfo -> user + accountInfo }
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), 1)
        .distinctUntilChanged()
}

private operator fun User?.plus(accountInfo: AccountInfo?): User? = when {
    accountInfo == null -> this
    this != null -> copy(
        ssoGuid = ssoGuid ?: accountInfo.ssoGuid,
        grMasterPersonId = grMasterPersonId ?: accountInfo.grMasterPersonId,
        name = name ?: accountInfo.name
    )
    else -> User(
        id = accountInfo.userId.orEmpty(),
        ssoGuid = accountInfo.ssoGuid,
        grMasterPersonId = accountInfo.grMasterPersonId,
        name = accountInfo.name
    )
}

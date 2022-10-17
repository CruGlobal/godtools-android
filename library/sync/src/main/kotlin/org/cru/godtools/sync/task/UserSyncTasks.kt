package org.cru.godtools.sync.task

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ccci.gto.android.common.base.TimeConstants.WEEK_IN_MS
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.api.UserApi
import org.cru.godtools.db.repository.LastSyncTimeRepository
import org.cru.godtools.db.repository.UserRepository

private const val SYNC_TIME_USER = "last_synced.user"
private const val STALE_DURATION_USER = WEEK_IN_MS

@Singleton
internal class UserSyncTasks @Inject constructor(
    private val accountManager: GodToolsAccountManager,
    private val lastSyncTimeRepository: LastSyncTimeRepository,
    private val userApi: UserApi,
    private val userRepository: UserRepository
) : BaseSyncTasks() {
    private val userMutex = Mutex()

    suspend fun syncUser(force: Boolean) = userMutex.withLock {
        if (!accountManager.isAuthenticated()) return true
        val userId = accountManager.userId().orEmpty()

        // short-circuit if we aren't forcing a sync and the data isn't stale
        if (!force &&
            !lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_USER, userId, staleAfter = STALE_DURATION_USER)
        ) return true

        val user = userApi.getUser().takeIf { it.isSuccessful }
            ?.body()?.takeUnless { it.hasErrors() }
            ?.dataSingle ?: return false

        userRepository.storeUserFromSync(user)
        lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_USER, user.id)

        true
    }
}

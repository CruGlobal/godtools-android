package org.cru.godtools.sync.task

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ccci.gto.android.common.base.TimeConstants
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.api.UserCountersApi
import org.cru.godtools.db.repository.LastSyncTimeRepository
import org.cru.godtools.db.repository.UserCountersRepository
import org.cru.godtools.model.UserCounter

@Singleton
internal class UserCounterSyncTasks @Inject internal constructor(
    private val accountManager: GodToolsAccountManager,
    private val countersApi: UserCountersApi,
    private val lastSyncTimeRepository: LastSyncTimeRepository,
    private val userCountersRepository: UserCountersRepository,
) : BaseSyncTasks() {
    companion object {
        const val SYNC_TIME_COUNTERS = "last_synced.user_counters"
        private const val STALE_DURATION_COUNTERS = TimeConstants.DAY_IN_MS
    }

    private val countersMutex = Mutex()
    private val countersUpdateMutex = Mutex()

    suspend fun syncCounters(force: Boolean): Boolean = countersMutex.withLock {
        if (!accountManager.isAuthenticated()) return true
        val userId = accountManager.userId().orEmpty()

        // short-circuit if we aren't forcing a sync and the data isn't stale
        if (!force &&
            !lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_COUNTERS, userId, staleAfter = STALE_DURATION_COUNTERS)
        ) {
            return true
        }

        val counters = countersApi.getCounters().takeIf { it.isSuccessful }
            ?.body()?.takeUnless { it.hasErrors() }
            ?.data ?: return false

        userCountersRepository.transaction {
            val existing = userCountersRepository.getCounters().associateBy { it.id }.toMutableMap()
            userCountersRepository.storeCountersFromSync(counters)
            counters.forEach { existing.remove(it.id) }
            userCountersRepository.resetCountersMissingFromSync(existing.values)
        }
        lastSyncTimeRepository.resetLastSyncTime(SYNC_TIME_COUNTERS, isPrefix = true)
        lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_COUNTERS, userId)

        true
    }

    suspend fun syncDirtyCounters(): Boolean = countersUpdateMutex.withLock {
        if (!accountManager.isAuthenticated()) return true

        coroutineScope {
            // process any counters that need to be updated
            userCountersRepository.getDirtyCounters()
                .filter { UserCounter.VALID_NAME.matches(it.id) }
                .map { counter ->
                    async {
                        val updated = countersApi.updateCounter(counter.id, counter).takeIf { it.isSuccessful }
                            ?.body()?.takeUnless { it.hasErrors() }
                            ?.dataSingle ?: return@async false

                        userCountersRepository.transaction {
                            userCountersRepository.updateCounter(counter.id, 0 - counter.delta)
                            userCountersRepository.storeCounterFromSync(updated)
                        }
                        true
                    }
                }
                .awaitAll().all { it }
        }
    }
}

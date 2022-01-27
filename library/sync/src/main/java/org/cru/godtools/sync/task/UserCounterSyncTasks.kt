package org.cru.godtools.sync.task

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.okta.oidc.clients.sessions.SessionClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.base.TimeConstants
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.get
import org.cru.godtools.api.UserCountersApi
import org.cru.godtools.model.UserCounter
import org.keynote.godtools.android.db.Contract.UserCounterTable
import org.keynote.godtools.android.db.GodToolsDao

private const val SYNC_TIME_COUNTERS = "last_synced.user_counters"
private const val STALE_DURATION_COUNTERS = TimeConstants.DAY_IN_MS

@VisibleForTesting
internal val QUERY_DIRTY_COUNTERS =
    Query.select<UserCounter>().where(UserCounterTable.FIELD_DELTA.gt(0))

@Singleton
class UserCounterSyncTasks @Inject internal constructor(
    private val dao: GodToolsDao,
    private val countersApi: UserCountersApi,
    private val sessionClient: SessionClient
) : BaseSyncTasks() {
    private val countersMutex = Mutex()
    private val countersUpdateMutex = Mutex()

    suspend fun syncCounters(args: Bundle = Bundle.EMPTY): Boolean = withContext(Dispatchers.IO) {
        countersMutex.withLock {
            if (!sessionClient.isAuthenticated) return@withContext true

            // short-circuit if we aren't forcing a sync and the data isn't stale
            if (!isForced(args) &&
                System.currentTimeMillis() - dao.getLastSyncTime(SYNC_TIME_COUNTERS) < STALE_DURATION_COUNTERS
            ) return@withContext true

            val counters = countersApi.getCounters().takeIf { it.isSuccessful }
                ?.body()?.takeUnless { it.hasErrors() }
                ?.data ?: return@withContext false

            dao.transaction { storeUserCounters(counters) }
            dao.updateLastSyncTime(SYNC_TIME_COUNTERS)

            true
        }
    }

    suspend fun syncDirtyCounters(): Boolean = withContext(Dispatchers.IO) {
        countersUpdateMutex.withLock {
            if (!sessionClient.isAuthenticated) return@withContext true

            // process any counters that need to be updated
            QUERY_DIRTY_COUNTERS.get(dao)
                .filter { UserCounter.VALID_NAME.matches(it.id) }
                .map { counter ->
                    async {
                        val updated = countersApi.updateCounter(counter.id, counter).takeIf { it.isSuccessful }
                            ?.body()?.takeUnless { it.hasErrors() }
                            ?.dataSingle ?: return@async false

                        dao.transaction {
                            dao.updateUserCounterDelta(counter.id, 0 - (counter.delta ?: 0))
                            storeUserCounter(updated)
                        }

                        true
                    }
                }
                .awaitAll().all { it }
        }
    }

    private fun storeUserCounters(counters: List<UserCounter>) = counters.forEach { storeUserCounter(it) }
    private fun storeUserCounter(counter: UserCounter) {
        dao.updateOrInsert(counter, UserCounterTable.COLUMN_COUNT, UserCounterTable.COLUMN_DECAYED_COUNT)
    }
}

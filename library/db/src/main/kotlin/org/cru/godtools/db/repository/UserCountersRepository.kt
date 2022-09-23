package org.cru.godtools.db.repository

import org.cru.godtools.model.UserCounter

interface UserCountersRepository {
    suspend fun <R> transaction(block: suspend () -> R): R

    suspend fun getCounters(): List<UserCounter>
    suspend fun updateCounter(name: String, delta: Int)

    // region Sync Methods
    suspend fun getDirtyCounters(): List<UserCounter>
    suspend fun storeCounterFromSync(counter: UserCounter) = storeCountersFromSync(listOf(counter))
    suspend fun storeCountersFromSync(counters: Collection<UserCounter>)
    suspend fun resetCountersMissingFromSync(counters: Collection<UserCounter>)
    // endregion Sync Methods
}

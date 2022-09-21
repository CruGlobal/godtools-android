package org.cru.godtools.db.repository

interface UserCountersRepository {
    suspend fun updateCounter(name: String, delta: Int)
}

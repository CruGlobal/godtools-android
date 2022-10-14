package org.cru.godtools.db.repository

interface LastSyncTimeRepository {
    suspend fun getLastSyncTime(vararg key: Any): Long
    suspend fun isLastSyncStale(vararg key: Any, staleAfter: Long): Boolean
    suspend fun updateLastSyncTime(vararg key: Any)
    suspend fun resetLastSyncTime(vararg key: Any, isPrefix: Boolean = false)
}

package org.cru.godtools.db.room.repository

import androidx.room.Dao
import org.cru.godtools.db.repository.LastSyncTimeRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.LastSyncTimeEntity

@Dao
internal abstract class LastSyncTimeRoomRepository(private val db: GodToolsRoomDatabase) : LastSyncTimeRepository {
    private val dao get() = db.lastSyncTimeDao

    override suspend fun getLastSyncTime(vararg key: Any) =
        dao.findLastSyncTime(LastSyncTimeEntity.flattenKey(key))?.time ?: 0

    override suspend fun isLastSyncStale(vararg key: Any, staleAfter: Long): Boolean {
        val lastSyncTime = dao.findLastSyncTime(LastSyncTimeEntity.flattenKey(key))?.time ?: return true
        return lastSyncTime + staleAfter < System.currentTimeMillis()
    }

    override suspend fun updateLastSyncTime(vararg key: Any) = dao.insertOrReplace(LastSyncTimeEntity(key))
}

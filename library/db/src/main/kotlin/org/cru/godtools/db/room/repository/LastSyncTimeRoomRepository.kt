package org.cru.godtools.db.room.repository

import androidx.room.Dao
import androidx.room.Transaction
import org.cru.godtools.db.repository.LastSyncTimeRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.LastSyncTimeEntity
import org.cru.godtools.db.room.entity.LastSyncTimeEntity.Companion.KEY_SEPARATOR

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

    @Transaction
    override suspend fun resetLastSyncTime(vararg key: Any, isPrefix: Boolean) {
        val flattened = LastSyncTimeEntity.flattenKey(key)
        dao.deleteLastSyncTime(flattened)
        if (isPrefix) {
            val prefix = "$flattened$KEY_SEPARATOR"
            dao.delete(
                dao.getLastSyncTimes("$prefix%")
                    .filter { it.id.startsWith(prefix, ignoreCase = true) }
            )
        }
    }
}

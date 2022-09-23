package org.cru.godtools.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.cru.godtools.db.room.entity.LastSyncTimeEntity

@Dao
internal interface LastSyncTimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(lastSyncTime: LastSyncTimeEntity)

    @Query("SELECT * FROM last_sync_times WHERE id = :key")
    suspend fun findLastSyncTime(key: String): LastSyncTimeEntity?
}

package org.cru.godtools.db.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import org.cru.godtools.db.room.entity.FollowupEntity

@Dao
internal interface FollowupsDao {
    @Insert
    suspend fun insert(followup: FollowupEntity)

    @Query("SELECT * FROM followups")
    suspend fun getFollowups(): List<FollowupEntity>

    @Delete
    suspend fun delete(followup: FollowupEntity)
}

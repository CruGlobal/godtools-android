package org.cru.godtools.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.cru.godtools.db.room.entity.UserCounterEntity

@Dao
internal interface UserCountersDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(counter: UserCounterEntity)

    @Query("UPDATE user_counters SET delta = delta + :delta WHERE name = :name")
    suspend fun updateUserCounterDelta(name: String, delta: Int)
}

package org.cru.godtools.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.db.room.entity.UserCounterEntity
import org.cru.godtools.db.room.entity.partial.MigrationUserCounter
import org.cru.godtools.db.room.entity.partial.SyncUserCounter

@Dao
internal interface UserCountersDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(counter: UserCounterEntity)

    @Query("SELECT * FROM user_counters")
    suspend fun getUserCounters(): List<UserCounterEntity>
    @Query("SELECT * FROM user_counters")
    fun getUserCountersFlow(): Flow<List<UserCounterEntity>>

    @Query("UPDATE user_counters SET delta = delta + :delta WHERE name = :name")
    suspend fun updateUserCounterDelta(name: String, delta: Int)

    // region Sync Methods
    @Query("SELECT * FROM user_counters WHERE delta != 0")
    suspend fun getDirtyCounters(): List<UserCounterEntity>

    @Update(entity = UserCounterEntity::class)
    suspend fun update(counters: Collection<SyncUserCounter>)
    @Upsert(entity = UserCounterEntity::class)
    suspend fun upsert(counters: Collection<SyncUserCounter>)
    // endregion Sync Methods

    // region Migration
    @Insert(entity = UserCounterEntity::class, onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(counter: MigrationUserCounter)
    @Update(entity = UserCounterEntity::class)
    fun update(counter: MigrationUserCounter)
    @Query("UPDATE user_counters SET delta = delta + :delta WHERE name = :name")
    fun migrateDelta(name: String, delta: Int)
    // endregion Migration
}
